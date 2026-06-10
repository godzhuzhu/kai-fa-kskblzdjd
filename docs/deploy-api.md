# 部署版接口文档 — Milestone: deploy

> v3.0 本地全功能完成后，按此文档重构为远程可部署的多人在线版。
> 核心目标：Docker 一键启动，MySQL 持久化，Redis 跨实例广播，Nginx 反向代理。

---

## Issue #19 — MySQL 替代 H2（gmy）

### 改动范围

#### pom.xml 新增

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

#### application.yml（多环境 profile）

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:zuuldb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: update

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3306/zuul?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${MYSQL_USER:zuul}
    password: ${MYSQL_PASSWORD:zuul123}
  h2:
    console:
      enabled: false
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.MySQLDialect
```

### 新增文件

| 文件 | 说明 |
|------|------|
| `src/main/resources/application.yml` | 替代 application.properties，支持 dev/prod 多环境 |

### 数据库初始化

```sql
CREATE DATABASE IF NOT EXISTS zuul DEFAULT CHARACTER SET utf8mb4;
```

### 验证

- dev 模式：`mvn spring-boot:run` 仍用 H2 内存库
- prod 模式：`SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run` 连接 MySQL

---

## Issue #20 — Redis 会话 + PubSub 跨实例广播（gmy）

### 改动范围

#### pom.xml 新增

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### application.yml 新增（prod profile）

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: 6379
      password: ${REDIS_PASSWORD:}
```

### 新增类

| 类 | 包路径 | 说明 |
|----|--------|------|
| `RedisConfig` | `zuul.game.config` | RedisTemplate 序列化配置 |
| `RedisSessionManager` | `zuul.game.websocket` | 替代 ConcurrentHashMap，playerSession 存 Redis |
| `RedisPubSubService` | `zuul.game.websocket` | PubSub 频道：`room:*`，收消息后推给本实例 WebSocket |

### Redis 数据结构设计

| Key | Type | Value |
|-----|------|-------|
| `session:{userId}` | Hash | `{wsNode, online, lastHeartbeat, currentRoom}` |
| `room:{roomName}:players` | Set | `{userId1, userId2, ...}` |
| `channel:room:{roomName}` | PubSub | 房间内广播（playerPush/roomPush/messagePush） |

### RedisSessionManager 对外接口

```java
class RedisSessionManager:
    void playerOnline(Player player)            // 上线：写入 session，加入房间 Set
    void playerOffline(Player player)           // 下线：删除 session，移除房间 Set
    List<Integer> getPlayersInRoom(Room room)   // 获取房间内所有玩家 ID
    List<Integer> getOnlinePlayerIds()          // 获取所有在线玩家 ID
    void updateHeartbeat(Player player)         // 更新心跳时间
    long checkHeartbeats(long timeout)          // 清理超时会话
```

### RedisPubSubService

```java
class RedisPubSubService:
    void publish(Room room, String jsonPayload)  // 发布到频道
    // 订阅：收到消息后通过本实例 GameWebSocketHandler 推送给本地连接
```

### GameWebSocketHandler 改动

- `sessions` / `playerSessions` 两个 `ConcurrentHashMap` 改为使用 `RedisSessionManager`
- `roomPush()` 通过 `RedisPubSubService.publish()` 广播到所有实例
- 心跳检查改用 Redis 的 TTL 机制

### 验证

- dev 模式：不启用 Redis，仍用内存 Map（向后兼容）
- prod 模式：多实例启动，WebSocket 连不同实例，同房间消息互达

---

## Issue #21 — Docker + docker-compose 一键部署（gmy）

### 新增文件

#### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/zuul-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.yml

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: zuul
      MYSQL_USER: zuul
      MYSQL_PASSWORD: zuul123
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MYSQL_HOST: mysql
      MYSQL_USER: zuul
      MYSQL_PASSWORD: zuul123
      REDIS_HOST: redis
    depends_on:
      - mysql
      - redis

volumes:
  mysql_data:
```

### 操作流程

```bash
# 构建+启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f app

# 关闭
docker-compose down
```

---

## Issue #22 — Nginx 反向代理 + 前端静态托管（gmy）

### 新增文件

#### nginx/nginx.conf

```nginx
upstream backend {
    server app:8080;
}

upstream websocket {
    server app:8080;
}

server {
    listen 80;

    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # HTTP API
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket
    location /game/ {
        proxy_pass http://websocket;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

#### Dockerfile.nginx

```dockerfile
FROM nginx:alpine
COPY frontend/dist /usr/share/nginx/html
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf
```

#### docker-compose.yml 新增

```yaml
  nginx:
    build:
      context: .
      dockerfile: Dockerfile.nginx
    ports:
      - "80:80"
    depends_on:
      - app
```

### 部署后访问

```
http://服务器IP/       → 前端页面
http://服务器IP/api/   → 后端 API
ws://服务器IP/game/    → WebSocket
```

---

## Issue #23 — 配置外部化 + 环境变量（gmy）

### 改动范围

#### .env（不提交 Git）

```bash
MYSQL_ROOT_PASSWORD=root123
MYSQL_USER=zuul
MYSQL_PASSWORD=zuul123
JWT_SECRET=ProductionSecretKey2026
REDIS_PASSWORD=
```

#### .gitignore 新增

```
.env
docker-compose.override.yml
*.log
```

#### application.yml 用途

| 环境 | 数据库 | Redis | 用途 |
|------|--------|-------|------|
| dev | H2 内存 | 无（ConcurrentHashMap） | 本地开发 |
| prod | MySQL | Redis | 远程部署 |

### Spring Security 生产环境修正

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 生产环境：移除自动生成的 UserDetailsService
    // 添加 CORS 配置
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost", "http://服务器IP"));
        config.setAllowedMethods(List.of("GET", "POST"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

## Issue #24 — CI/CD 构建 Docker 镜像（gmy）

### .github/workflows/docker.yml

```yaml
name: Docker Image CI

on:
  push:
    branches: [master]
    tags: ['v*']

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build JAR
        run: mvn package -DskipTests -B -Pprod

      - name: Build Docker image
        run: docker build -t zuul-server:${{ github.sha }} .

      - name: Tag and push (optional)
        if: startsWith(github.ref, 'refs/tags/')
        run: |
          docker tag zuul-server:${{ github.sha }} zuul-server:latest
          # docker push zuul-server:latest  # 需要配置 Docker Hub / 镜像仓库
```

---

## 开发依赖顺序

```
          ┌────────────────────┐
          │ #19 MySQL 替代 H2   │ (数据库层)
          └────────┬───────────┘
                   │
          ┌────────▼───────────┐
          │ #20 Redis 会话+广播 │ (通信层)
          └────────┬───────────┘
                   │
          ┌────────▼───────────┐
          │ #23 配置外部化      │ (并行)
          └────────┬───────────┘
                   │
     ┌─────────────┼─────────────┐
     │             │             │
┌────▼────┐  ┌────▼────┐  ┌────▼────┐
│#21 Docker│ │#22 Nginx│ │#24 CI/CD│
│ compose │  │ 代理    │  │ 镜像     │
└─────────┘  └─────────┘  └─────────┘
```

---

## 完整部署命令（最终效果）

```bash
# 1. 克隆仓库
git clone https://github.com/wutcst/kai-fa-kskblzdjd.git
cd kai-fa-kskblzdjd

# 2. 一键启动
docker-compose up -d

# 3. 浏览器打开
http://localhost/

# 前端 → Nginx → Spring Boot → MySQL + Redis
# 全栈运行，无需手动配置任何环境
```
