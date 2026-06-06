# GitHub Issues 任务清单

---

## v1.0 — 单人游戏版

---

### #1 搭建 Spring Boot 项目骨架 + Maven 配置

**Assignee:** lfk
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
将现有基础 Maven 项目改造为 Spring Boot 项目。

## 子任务
- [ ] 修改 pom.xml，添加 spring-boot-starter-parent 3.x
- [ ] 添加依赖：spring-boot-starter-web
- [ ] 配置 Java 版本为 17
- [ ] 创建 ZuulServerApplication.java（@SpringBootApplication 入口）
- [ ] 创建 ServerMain.java（main 方法）
- [ ] 确保 mvn spring-boot:run 能成功启动

## 验收标准
- 项目启动不报错
- Spring Boot 应用正常加载
```

---

### #2 实现 Player 玩家类（属性/背包/负重）

**Assignee:** lfk
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
创建独立的 Player 类，表示游戏中的玩家。

## 属性
- int userId: 玩家唯一 ID
- String playerName: 玩家昵称
- Room currentRoom: 当前所在房间
- Stack<Room> previousRooms: 历史路径（用于 back 命令）
- List<AbstractItem> bag: 背包物品列表
- int currentLoad: 当前负重
- int maxCapacity: 最大负重上限（默认 50）
- int attack: 攻击力（默认 10）
- int defense: 防御力（默认 5）
- int currentHealth: 当前生命值（默认 100）
- int maxHealth: 最大生命值（默认 100）
- boolean isOnline: 是否在线

## 方法
- [ ] takeItem(AbstractItem item): 拾取物品（检查负重）
- [ ] dropItem(AbstractItem item): 丢弃物品
- [ ] useItem(AbstractItem item): 使用物品
- [ ] moveTo(Room room): 移动到新房间（记录历史）
- [ ] goBack(): 回到上一个房间
- [ ] hurtBy(int damage): 受到伤害
- [ ] getItemsWeight(): 计算背包总重量

## 验收标准
- 所有属性和方法正确实现
- 拾取超重物品时返回 false/提示
- 历史路径栈正确记录
```

---

### #3 重构 Room 类（物品/传送/多人适配）

**Assignee:** lfk
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
扩展 Room 类，支持物品存放、传送房间和多玩家。

## 新增属性
- List<AbstractItem> items: 房间内的物品列表
- boolean portal: 是否为传送房间

## 新增方法
- [ ] addItem(AbstractItem item): 添加物品到房间
- [ ] removeItem(String name): 按名称移除物品
- [ ] getItem(String name): 按名称获取物品
- [ ] getItems(): 获取所有物品
- [ ] addRandomItems(): 随机生成物品到房间
- [ ] isPortal(): 判断是否传送房间
- [ ] getExits(): 获取所有出口（已有）

## 修改内容
- description 增加物品列表信息
- 传送房间的 getExit 方法应随机返回出口

## 验收标准
- 房间可以存放多个物品
- 传送房间进入后随机跳转
- 与现有 Game.createRooms() 兼容
```

---

### #4 实现物品系统 AbstractItem 及装备/道具

**Assignee:** gmy
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
设计物品系统的抽象基类，并实现若干物品。

## AbstractItem 抽象类
- [ ] String name: 物品名称
- [ ] String description: 物品描述
- [ ] int weight: 重量
- [ ] abstract void takenBy(Player player): 被拾取时的效果
- [ ] abstract void droppedBy(Player player): 被丢弃时的效果
- [ ] abstract void usedBy(Player player): 被使用时的效果

## Items 物品目录类
- [ ] 维护所有物品类型的注册表
- [ ] generateRandomItem(): 随机生成一件物品

## 具体物品实现（至少 6 个）
### 装备类
- [ ] Sword 铁剑：拾取 +10 攻击力，丢弃 -10
- [ ] DragonscaleBulwark 龙鳞壁垒：拾取 +8 防御力，丢弃 -8
- [ ] StormCleaver 风暴斩刃：拾取 +15 攻击力、+3 破甲

### 道具类
- [ ] MagicCookie 魔法饼干：使用后 +20 负重上限
- [ ] BloodVial 血瓶：使用后 +30 生命值
- [ ] StonehideElixir 石化药剂：使用后 +10 防御（临时）

## 验收标准
- 物品可以正确拾取/丢弃/使用
- 物品效果正确作用于玩家属性
- Items 类能随机生成物品
```

---

### #5 实现 GoCommand / BackCommand / HelpCommand

**Assignee:** lfk
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
改造现有 GoCommand，新增 BackCommand，重构 HelpCommand。

## GoCommand
- [ ] 读取 secondWord 获取方向
- [ ] 无方向时提示 "Go where?"
- [ ] 传送房间则随机选择出口
- [ ] 无出口时提示 "There is no door!"
- [ ] 移动后调用 player.moveTo() 记录历史

## BackCommand（新增）
- [ ] 新建 BackCommand 类继承 Command
- [ ] 实现 execute(Game game, Player player)
- [ ] 从 player.previousRooms 栈弹出上一个房间
- [ ] 栈空则提示 "You are at the starting point!"
- [ ] 回退成功提示当前房间信息
- [ ] 支持连续回退（多次 back）

## HelpCommand
- [ ] 重构为继承 Command
- [ ] 列出所有可用命令及其说明

## 验收标准
- go <方向> 正确移动
- back 逐层回退直到起点
- help 列出所有命令
```

---

### #6 实现 TakeCommand / DropCommand / UseCommand

**Assignee:** gmy
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
新增拾取、丢弃、使用物品的三个命令。

## TakeCommand
- [ ] 无参数时提示 "Take what?"
- [ ] 物品不存在时提示 "There is no such item here."
- [ ] 超重时提示 "You cannot carry more items! Max capacity: XX"
- [ ] 成功后将物品从房间移到玩家背包
- [ ] 调用 item.takenBy(player)

## DropCommand
- [ ] 无参数时提示 "Drop what?"
- [ ] 物品不在背包时提示 "You don't have this item."
- [ ] 丢弃导致血量变负时拒绝并提示
- [ ] 成功后将物品从背包移到房间
- [ ] 调用 item.droppedBy(player)

## UseCommand
- [ ] 无参数时提示 "Use what?"
- [ ] 物品不在背包时提示 "You don't have this item."
- [ ] 道具使用后从背包移除
- [ ] 装备不能被 use
- [ ] 调用 item.usedBy(player)

## 验收标准
- take 物品后房间不再显示该物品
- drop 物品后房间出现该物品
- use 消耗品后物品消失、效果生效
```

---

### #7 实现传送房间 + 随机传送逻辑

**Assignee:** lfk
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
在游戏中增加传送房间，进入后随机传送到其他房间。

## 实现要求
- [ ] Room 类增加 portal 属性（已由 #3 完成）
- [ ] 在地图中标记 1-2 个房间为传送房间
- [ ] 玩家进入传送房间时触发随机传送
- [ ] 传送逻辑：
  - 从所有房间中随机选一个（排除自身）
  - 将玩家移到目标房间
  - 显示传送提示 "A mysterious force transports you..."
  - 将传送前房间和传送后房间都记入历史栈
- [ ] 传送房间不应该传送到另一个传送房间（防止无限传送）

## 验收标准
- 进入传送房间自动跳转
- 跳转有提示信息
- 不会传送到自身
```

---

### #8 实现消息系统（MessageBridge 等）

**Assignee:** gmy
**Milestone:** v1.0
**Labels:** enhancement

```
## 任务描述
设计消息系统，解耦游戏逻辑与消息输出方式。

## 接口与类
- [ ] IMessage 接口：定义消息类型和内容
- [ ] AbsMessageBridge 抽象类：消息输出桥接
  - send(IMessage message): 发送消息
- [ ] ConsoleMessageBridge：控制台输出实现
- [ ] GlobalMessage：广播消息（所有玩家可见）
- [ ] RoomWideMessage：房间内广播
- [ ] SinglePlayerMessage：单人消息
- [ ] GameMessageBridge：WebSocket 消息桥接（v2.0 使用）

## 使用方式
游戏中的 System.out.println 全部替换为 messageBridge.send(...)

## 验收标准
- 命令执行结果通过消息系统输出
- 支持控制台模式
- 预留 WebSocket 扩展点
```

---

## v2.0 — 联机游戏版

---

### #9 实现 WebSocket 多人通信

**Assignee:** lfk
**Milestone:** v2.0
**Labels:** enhancement

```
## 任务描述
基于 Spring WebSocket 实现多人在线实时通信。

## 后端
- [ ] WebSocketConfig 配置端点 /game/websocket
- [ ] GameWebSocketHandler：处理连接/消息/断开
- [ ] GameSession：每个 WebSocket 连接对应一个会话
  - 绑定 userId → Player
  - 维护心跳时间
- [ ] WebSocketIncomingPayload：接收消息 DTO
- [ ] WebSocketOutgoingPayload：发送消息 DTO
- [ ] GameMessageBridge：WebSocket 消息输出实现
- [ ] 玩家上线时将 isOnline 设为 true，加入游戏世界
- [ ] 推送当前玩家状态（playerPush）
- [ ] 推送当前房间状态（roomPush）
- [ ] 推送系统消息（messagePush）
- [ ] 指令执行后进行状态同步推送

## 心跳机制
- [ ] 前端每 30 秒发 heartbeat
- [ ] 后端 60 秒未收到心跳断开连接
- [ ] 断开时 isOnline 设为 false

## View Object
- [ ] ItemVO：物品展示（不含内部实现细节）
- [ ] PlayerVO：玩家展示（不含敏感信息）
- [ ] RoomVO：房间展示
- [ ] RoomPlayerVO：房间内其他玩家（仅展示名称和血量）

## 并发控制
- [ ] 对游戏操作加锁，避免并发问题

## 验收标准
- 两个浏览器窗口能同时登录并看到彼此
- 一个玩家移动后另一个能看到房间列表变化
- 掉线后被标记为离线
```

---

### #10 实现 HTTP API 用户系统（登录/注册/Token）

**Assignee:** lfk
**Milestone:** v2.0
**Labels:** enhancement

```
## 任务描述
实现基于 Spring Security + JWT 的用户认证系统。

## 数据层
- [ ] User 实体类（JPA）
  - id, username, password
- [ ] UserRepository（Spring Data JPA）

## Service 层
- [ ] IUserService 接口
- [ ] UserService 实现
  - register(username, password): 注册（密码加密）
  - login(username, password): 登录校验

## Controller 层
- [ ] UserController
  - POST /api/user/login → 返回 userId + token
  - POST /api/user/register → 返回 userId
- [ ] Result 统一响应类（success, message, data）
- [ ] LoginDTO / RegisterDTO / LoginVO 数据传输对象

## 认证
- [ ] SecurityConfig：放行 login/register，拦截其余请求
- [ ] JwtUtil：生成/校验 JWT Token
- [ ] JwtInterceptor：Token 拦截验证
- [ ] JwtConfig：密钥等配置

## 验收标准
- 注册后数据库有记录
- 登录返回 Token
- 未登录访问被拦截
```

---

### #11 实现 AttackCommand 对战系统

**Assignee:** gmy
**Milestone:** v2.0
**Labels:** enhancement

```
## 任务描述
实现 PvP 对战系统，玩家之间可以互相攻击。

## 攻击流程
1. 玩家输入 attack <目标玩家名>
2. 检查目标是否在同一房间
3. 检查攻击冷却（2 秒）
4. 阶段一：攻击者造成伤害
   - 伤害 = attack + extraAttack - 目标.defense
   - 若目标有破甲忽略部分防御
   - 调用 target.hurtBy(damage)
   - 触发观察者 onHurt 事件
5. 阶段二：被攻击者反击
   - 反击伤害 = 目标.attack - 攻击者.defense（×0.5 系数）
   - 调用 attacker.hurtBy(counterDamage)
6. 任意一方血量 ≤ 0：
   - 触发 onDeath 事件
   - 失败者物品掉落到房间
   - 失败者属性重置
   - 失败者传送回起点
   - 胜利者触发 onFightWin 事件

## 玩家事件系统
- [ ] IPlayerListener 接口
  - onHurt(AttackEvent) / onDeath(DeathEvent) / onFightWin(FightWinEvent)
- [ ] Player 类添加 List<IPlayerListener> listeners
- [ ] 受伤/死亡/胜利时依次调用所有监听器

## 装备事件订阅示例
- [ ] BerserkerTotem：订阅 onHurt，受伤时 +5 attack（持续 10 秒）
- [ ] BloodDagger：订阅 onFightWin，胜利时恢复 20 血量
- [ ] ImmortalCore：订阅 onDeath，死亡时保留 1 血量（一次性）
- [ ] ShadowbaneBallista：订阅 onHurt，额外造成 5 点伤害

## 验收标准
- 同房间玩家可以互相攻击
- 攻击/反击伤害计算正确
- 失败者物品掉落在房间
- 装备特效正确触发
```

---

### #12 实现数据库持久化（游戏存档）

**Assignee:** lfk
**Milestone:** v2.0
**Labels:** enhancement

```
## 任务描述
实现游戏状态的数据库保存与加载。

## 数据实体
- [ ] StoreText JPA 实体（id, name, data）
- [ ] StoreRepository（JPA Repository）

## Service 层
- [ ] IStoreService 接口
- [ ] StoreService 实现
  - save(name, jsonString): 保存
  - load(name): 读取 JSON 字符串

## 游戏序列化
- [ ] Store 类：封装所有 RoomSaver 和 PlayerSaver
- [ ] RoomSaver：Room 的可序列化版本（使用编号替代引用）
- [ ] PlayerSaver：Player 的可序列化版本
- [ ] StoreUtil：序列化/反序列化工具
- [ ] StoreManager：存档管理
  - saveGame(): 将当前游戏转为 JSON 存入数据库
  - loadGame(): 从数据库读取 JSON 还原游戏
- [ ] AbstractItemAdapter（Gson TypeAdapter）：
  - 序列化时写入物品类型名
  - 反序列化时根据类型名还原

## 为什么需要编号
Room 之间相互引用会形成循环依赖，无法直接用 Gson 序列化。
解决：序列化前将所有 Room 编号 → RoomSaver 中用编号替代引用 → 反序列化时用编号还原引用。

## 验收标准
- 游戏存档后可成功读档
- 存档后玩家位置、背包、房间物品正确恢复
- 存档后 Room 之间的连接正确恢复
```

---

## v3.0 — 网页联机版

---

### #13 前端：登录/注册页面（Home.vue）

**Assignee:** zy
**Milestone:** v3.0
**Labels:** frontend

```
## 任务描述
实现主页面的登录和注册功能。

## 页面元素
- [ ] 背景视频/动画效果
- [ ] 游戏标题展示
- [ ] 登录表单（用户名 + 密码 + 登录按钮）
- [ ] 注册表单（用户名 + 密码 + 确认密码 + 注册按钮）
- [ ] 登录/注册切换 Tab
- [ ] 表单校验（用户名不为空、密码长度≥6、两次密码一致）
- [ ] 错误提示（用户名已存在、密码错误等）

## 接口对接
- [ ] 登录：POST /api/user/login
- [ ] 注册：POST /api/user/register
- [ ] 登录成功后将 token 存入 sessionStorage
- [ ] 登录成功后跳转到 /game

## 验收标准
- 注册成功可登录
- 登录成功跳转游戏页
- 错误情况有提示
```

---

### #14 前端：游戏主界面（Game.vue + WebSocket）

**Assignee:** zy
**Milestone:** v3.0
**Labels:** frontend

```
## 任务描述
实现游戏主界面的完整交互。

## WebSocket 连接
- [ ] 进入页面时建立 WebSocket 连接（带 Token 鉴权）
- [ ] 连接 ws://localhost:8080/game/websocket
- [ ] 每 30 秒发送心跳包 {action: "heartbeat", data: null}
- [ ] 断线时提示并重连

## 消息接收
- [ ] playerPush：更新左侧玩家信息面板
- [ ] roomPush：更新房间信息面板（名称/描述/物品/出口/其他玩家）
- [ ] messagePush：更新聊天/消息记录区域

## 界面布局
左栏 | 中栏 | 右栏
玩家信息：名称、血量、攻击、防御 | 房间信息：房间名、描述、物品列表 | 其他玩家：同房间玩家列表
背包物品列表 | 出口按钮（go east...）| 消息历史区域
负重：XX/XX | 输入框 + 发送/快捷操作按钮 |

## 快捷操作
- [ ] 方向按钮（go east / west / south / north）
- [ ] back 按钮
- [ ] 物品操作：点击物品弹出 take/drop/use 选项
- [ ] 攻击：点击其他玩家弹出 attack 确认
- [ ] help / look / items 按钮

## 验收标准
- 登录后能看到自己的状态
- 能看到同房间的玩家
- 点击方向按钮可以移动
- 物品操作正常工作
```

---

### #15 前端：路由配置 + 整体框架

**Assignee:** zy
**Milestone:** v3.0
**Labels:** frontend

```
## 任务描述
搭建前端项目框架和路由。

## 项目初始化
- [ ] npm create vite@latest frontend -- --template vue
- [ ] npm install element-plus vue-router gsap
- [ ] 配置 vite.config.js（端口、代理）

## 路由配置
- [ ] / → HomeView.vue（登录/注册页）
- [ ] /game → GameView.vue（游戏主界面）
- [ ] 未登录时 /game 重定向到 /

## 页面组件结构
- [ ] App.vue：根组件 + GSAP 页面切换动画 + <router-view>
- [ ] HomeView.vue：组合 HomeBackground + Home
- [ ] HomeBackground.vue：背景视频
- [ ] GameView.vue：渲染 Game.vue

## 样式规范
- [ ] 使用 scoped CSS
- [ ] Element Plus 主题定制
- [ ] 响应式布局

## 验收标准
- 访问 / 显示登录页
- 登录后跳转 /game
- 页面切换有动画
```

---

### #16 配置 GitHub Actions CI

**Assignee:** gmy
**Milestone:** v1.0
**Labels:** devops

```
## 任务描述
配置 GitHub Actions 实现持续集成。

## 工作流文件：.github/workflows/maven.yml
- [ ] 触发条件：push + pull_request（main / develop 分支）
- [ ] JDK 17 环境
- [ ] 步骤：
  1. Checkout 代码
  2. Set up JDK 17
  3. mvn test -B（运行测试）
  4. mvn package -DskipTests -B（打包）
  5. 上传测试报告为 Artifact

## 工作流文件：.github/workflows/linter.yml（可选）
- [ ] 使用 super-linter 进行代码规范检查

## 验收标准
- Push 代码后 Actions 自动运行
- 测试全部通过（绿色勾）
- 失败时收到通知
```

---

### #17 编写单元测试

**Assignee:** gmy
**Milestone:** v2.0
**Labels:** test

```
## 任务描述
为游戏核心模块编写 JUnit 单元测试。

## 测试范围
- [ ] PlayerTest：属性初始化、背包增删、负重计算、移动历史
- [ ] RoomTest：物品管理、出口管理、传送判断
- [ ] GoCommandTest：正常移动、无出口处理、传送房间
- [ ] BackCommandTest：历史回退、多次回退、空栈处理
- [ ] TakeCommandTest：正常拾取、超重、物品不存在
- [ ] DropCommandTest：正常丢弃、背包空、空物品名
- [ ] UseCommandTest：道具效果、装备使用被拒
- [ ] MagicCookieTest：负重上限增加
- [ ] ImmortalCoreTest：死亡保护机制
- [ ] 消息系统测试：各种消息类型的构造和输出

## 测试要求
- 使用 JUnit 5
- 每个 public 方法至少 1 个测试用例
- 覆盖正常路径 + 边界条件 + 异常路径
- 测试覆盖率 ≥ 80%

## 验收标准
- mvn test 全部通过
- 测试报告无失败
```

---

### #18 编写 README.md + REPORT.md

**Assignee:** gmy
**Milestone:** v3.0
**Labels:** documentation

```
## 任务描述
编写完整的项目文档。

## README.md
- [ ] 项目简介
- [ ] 功能列表（≥10 项）
- [ ] 技术栈
- [ ] 快速开始（环境要求 + 后端启动 + 前端启动）
- [ ] 项目结构
- [ ] 成员分工表
- [ ] CI/CD 说明

## REPORT.md
- [ ] 功能实现情况
- [ ] 项目分工详情（每人负责的模块说明）
- [ ] GitHub Issues + Milestone 管理截图
- [ ] 分支管理策略说明
- [ ] Code Review 流程说明
- [ ] CI/CD 配置说明
- [ ] 后端详细设计（含 UML 图、属性和方法表）
  - Player 设计 / Room 设计 / 命令系统 / 物品系统
  - 用户系统 / WebSocket / 游戏存档 / 对战系统
- [ ] 前端详细设计（路由/组件/状态管理）
- [ ] 单元测试说明
- [ ] AI 辅助说明（使用的模型及辅助内容）

## docs/zuul-api.md
- [ ] HTTP API 文档（login / register）
- [ ] WebSocket 消息格式文档
- [ ] 所有数据模型定义（JSON + 字段表）

## 验收标准
- 文档完整、结构清晰
- API 文档可供前端参照对接
```

---

## Issue 汇总表

| 编号 | 标题 | 负责人 | 里程碑 |
|------|------|--------|--------|
| #1 | 搭建 Spring Boot 项目骨架 + Maven 配置 | lfk | v1.0 |
| #2 | 实现 Player 玩家类（属性/背包/负重） | lfk | v1.0 |
| #3 | 重构 Room 类（物品/传送/多人适配） | lfk | v1.0 |
| #4 | 实现物品系统 AbstractItem 及装备/道具 | gmy | v1.0 |
| #5 | 实现 GoCommand / BackCommand / HelpCommand | lfk | v1.0 |
| #6 | 实现 TakeCommand / DropCommand / UseCommand | gmy | v1.0 |
| #7 | 实现传送房间 + 随机传送逻辑 | lfk | v1.0 |
| #8 | 实现消息系统（MessageBridge 等） | gmy | v1.0 |
| #9 | 实现 WebSocket 多人通信 | lfk | v2.0 |
| #10 | 实现 HTTP API 用户系统（登录/注册/Token） | lfk | v2.0 |
| #11 | 实现 AttackCommand 对战系统 | gmy | v2.0 |
| #12 | 实现数据库持久化（游戏存档） | lfk | v2.0 |
| #13 | 前端：登录/注册页面（Home.vue） | zy | v3.0 |
| #14 | 前端：游戏主界面（Game.vue + WebSocket） | zy | v3.0 |
| #15 | 前端：路由配置 + 整体框架 | zy | v3.0 |
| #16 | 配置 GitHub Actions CI | gmy | v1.0 |
| #17 | 编写单元测试 | gmy | v2.0 |
| #18 | 编写 README.md + REPORT.md | gmy | v3.0 |
