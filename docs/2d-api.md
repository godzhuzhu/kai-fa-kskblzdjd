# 2D 网页游戏接口文档

> v3.0 文本版完成后的重构目标：将文字冒险游戏改造为 2D 瓦片地图游戏。
> 键盘 WASD 实时移动，Canvas 渲染，保留原物品/战斗/存档系统。

---

## 整体架构

```
                                 WebSocket
┌──────────────┐    move/interact     ┌──────────────┐
│ 前端 Canvas  │ ←──────────────→    │ Spring Boot  │
│ (Vite+Vue)   │  playerPush/roomPush │   后端       │
└──────────────┘                      └──────┬───────┘
        │                                    │
  键盘输入                              碰撞检测
  Canvas渲染                            物品管理
  精灵动画                              坐标计算
        │                                    │
        └────── 60fps 渲染循环 ──────────────┘
```

---

## Issue #25 — Room 2D 地图系统（gmy）

### Room 新增字段

```java
// cn.edu.whut.sept.zuul.game.Room 新增
int width;                              // 地图宽度（建议 15）
int height;                             // 地图高度（建议 10）
int[][] tiles;                          // 瓦片数据
Map<Integer, Direction> doorTiles;      // 门瓦片位置 → 通往方向
Map<String, int[]> itemSpawns;          // 物品生成坐标 {name: [x,y]}
```

### 瓦片类型

| 值 | 常量 | 含义 | 可通行 |
|----|------|------|--------|
| 0 | FLOOR | 地板 | ✅ |
| 1 | WALL | 墙壁 | ❌ |
| 2 | DOOR | 门 | ✅ 碰到自动换房 |

### 方向枚举

```java
public enum Direction {
    NORTH, SOUTH, EAST, WEST
}
```

### 地图示例

```
##############    # = WALL
#............#    . = FLOOR
#.........▼..#    ▼ = DOOR (通下方房间)
#............#
##############
```

### 房间布局预设（5 个房间）

```
outside (起点)          theater               pub
###############      ###############      ###############
#.....#.......#      #.............#      #.#.#.#.#.#.#.#
#.....#.......#      #.....▼.......#      ##.##.##.##.##.#
#..@..#.......#      #.............#      #.#.#.#.#.#.#.#
#.....#.......#      ###############      ##.##.##.##.##.#
#...▼.#...▼...#                          #.#.#.#.#.#.#.#
######+#########                          ###############

        lab (传送房)          office
      ###############      ###############
      #▼............#      #.............#
      #.............#      #.............#
      #......##.....#      #..▼..........#
      #......##.....#      #.............#
      #.............#      ###############
      ###############
```

### 坐标与出口关系

| 房间 | 门坐标 | 通向 | 目标房间 | 目标进门坐标 |
|------|--------|------|---------|-------------|
| outside | (5,5) DOOR | south | lab | (1,0) DOOR |
| outside | (12,5) DOOR | east | theater | (0,5) DOOR |
| outside | (11,0) DOOR | west | pub | (13,0) DOOR |
| lab | (1,0) DOOR | north | outside | (5,5) DOOR |

### 对外接口（供 #26 #29 调用）

```java
// Room 新增方法
int getWidth() / getHeight()
int[][] getTiles()
boolean isWalkable(int x, int y)            // x,y 是否可通行
Direction getDoorDirection(int x, int y)    // 该位置是门则返回方向，否则 null
int[] getSpawnPoint()                       // 返回玩家出生坐标
boolean hasItemAt(int x, int y)             // 该坐标是否有物品
AbstractItem takeItemAt(int x, int y)       // 拾取该坐标物品
void placeItem(AbstractItem item, int x, int y) // 放置物品到坐标
```

---

## Issue #26 — Player 2D 坐标 + 碰撞检测（gmy）

### Player 新增字段

```java
// cn.edu.whut.sept.zuul.game.Player 新增
int posX;                               // 房间内 X 坐标
int posY;                               // 房间内 Y 坐标
```

### 移动逻辑（后端）

```java
// 在 GameWebSocketHandler 中处理
{
  "action": "move",
  "data": {"dx": 0, "dy": -1}       // dx/dy 取值 -1, 0, 1
}

// 后端处理流程：
public MoveResult movePlayer(Player p, int dx, int dy):
    int newX = p.posX + dx
    int newY = p.posY + dy
    Room room = p.getCurrentRoom()

    if (newX < 0 || newX >= room.width || newY < 0 || newY >= room.height):
        return BLOCKED

    int tile = room.tiles[newY][newX]
    if (tile == WALL):
        return BLOCKED

    if (tile == DOOR):
        Direction dir = room.getDoorDirection(newX, newY)
        Room targetRoom = room.getExitMap().get(dir.name().toLowerCase())
        if (targetRoom == null):
            return BLOCKED
        p.moveTo(targetRoom)
        p.posX, p.posY = targetRoom.getSpawnPoint()
        return ROOM_CHANGED

    // tile == FLOOR
    p.posX = newX
    p.posY = newY
    return MOVED
```

### WebSocket 协议更新

```javascript
// 客户端 → 服务端
{"action": "move", "data": {"dx": -1, "dy": 0}}   // 向左移动
{"action": "move", "data": {"dx": 1, "dy": 0}}    // 向右移动
{"action": "move", "data": {"dx": 0, "dy": -1}}   // 向上移动
{"action": "move", "data": {"dx": 0, "dy": 1}}    // 向下移动
```

---

## Issue #27 — 前端 Canvas 2D 渲染引擎（zy/gmy）

### 技术选型

```
Canvas API（原生）或 PixiJS
建议：原生 Canvas，轻量无依赖
```

### 渲染循环

```javascript
// 60fps 游戏循环
const TILE_SIZE = 32  // 每个瓦片 32×32 像素

function gameLoop() {
    clearCanvas()
    drawRoom(room.tiles, TILE_SIZE)               // 画地图
    drawItems(room.itemPositions, TILE_SIZE)       // 画物品
    drawOtherPlayers(room.players, TILE_SIZE)     // 画其他玩家
    drawPlayer(player, TILE_SIZE)                  // 画自己
    drawHUD(player)                                // 画血条/背包
    requestAnimationFrame(gameLoop)
}
```

### 瓦片绘制

| 瓦片类型 | 颜色/图片 |
|----------|----------|
| FLOOR | 灰色/深灰棋盘格 |
| WALL | 深棕色砖墙 |
| DOOR | `▼` `▲` `▶` `◀` 箭头标识 |
| 传送门 | 紫色漩涡动画 |

### 精灵绘制

| 对象 | 样式 |
|------|------|
| 玩家自己 | `@` 字符 + 绿色边框 |
| 其他玩家 | `@` 字符 + 蓝色边框 |
| Sword | 铁剑图标 |
| BloodVial | 红色药水图标 |
| MagicCookie | 饼干图标 |
| 其他物品 | 对应图标 |

### 键盘输入

```javascript
// 防重复发送，移动冷却 150ms
const MOVEMENT_COOLDOWN = 150
let lastMoveTime = 0

const keys = {}
window.addEventListener('keydown', e => keys[e.key] = true)
window.addEventListener('keyup', e => keys[e.key] = false)

function handleInput() {
    const now = Date.now()
    if (now - lastMoveTime < MOVEMENT_COOLDOWN) return

    if (keys['w'] || keys['ArrowUp'])    { sendMove(0, -1); lastMoveTime = now }
    if (keys['s'] || keys['ArrowDown'])  { sendMove(0, 1); lastMoveTime = now }
    if (keys['a'] || keys['ArrowLeft'])  { sendMove(-1, 0); lastMoveTime = now }
    if (keys['d'] || keys['ArrowRight']) { sendMove(1, 0); lastMoveTime = now }
    if (keys[' ']) { sendInteract(); lastMoveTime = now }  // 空格拾取
}

function sendMove(dx, dy) {
    ws.send(JSON.stringify({ action: "move", data: { dx, dy } }))
}
```

---

## Issue #28 — 键盘移动 + 房间间实时切换（gmy）

### 移动流程

```
玩家按 W → 前端发送 {action:"move", data:{dx:0,dy:-1}}
         → 后端碰撞检测
         → 如果撞墙 → 不更新
         → 如果地板 → 更新 posX, posY → 推送 playerPush
         → 如果是门 → 查方向 → 切换房间 → 推送 roomPush + playerPush
```

### 房间切换动画

```
当前房间 → 门触发 → 100ms 黑屏 → 新房间渲染 → 150ms 淡入
```

### 传送房间

```
门通向 lab（传送房）→ 后端随机选择目标房间 → 闪烁紫色特效 → 传送到随机房间
```

### playerPush 更新

```json
{
  "type": "playerPush",
  "data": {
    "userId": 1,
    "playerName": "Player1",
    "posX": 5,
    "posY": 8,
    "currentRoomName": "outside",
    "attack": 10,
    "defense": 5,
    "currentHealth": 100,
    "maxHealth": 100,
    "maxCapacity": 50,
    "currentLoad": 4,
    "bag": [{"name": "Sword", "weight": 8}]
  }
}
```

### roomPush 更新

```json
{
  "type": "roomPush",
  "data": {
    "roomName": "outside",
    "tiles": [[1,1,1,1,...],[1,0,0,0,...],...],
    "items": [
      {"name": "Sword", "x": 5, "y": 3, "weight": 8}
    ],
    "players": [
      {"userId": 2, "playerName": "Player2", "posX": 7, "posY": 6, "currentHealth": 80, "maxHealth": 100}
    ],
    "exits": ["east", "south", "west"],
    "portal": false
  }
}
```

---

## Issue #29 — 物品 2D 渲染 + 拾取/丢弃/使用（gmy）

### 物品交互方式

```
拾取：走到物品上 + 按空格 → 前端发送 {"action":"interact"}
      后端扫描脚下坐标 → 有物品 → takeItem → itemPositions 移除 → 推送更新

丢弃：按 I 打开背包 → 点击物品 → "Drop" → 后端计算身边空位 → 放置

使用：按 I 打开背包 → 点击道具 → "Use" → 后端使用效果
```

### 前端背包 UI

```
按 I 键 → 弹出背包面板（半透明黑底）
┌─────────────┐
│ 背包 (12/50)│
│             │
│ ⚔ Sword    │ [Drop]
│ 🩸BloodVial │ [Use]
│ 🍪Cookie    │ [Use]
│             │
│       [关闭]│
└─────────────┘
```

### 交互协议

```javascript
// 拾取脚下物品
ws.send(JSON.stringify({ action: "interact" }))

// 丢弃物品
ws.send(JSON.stringify({ action: "drop", data: "Sword" }))

// 使用物品
ws.send(JSON.stringify({ action: "use", data: "BloodVial" }))

// 攻击（保留）
ws.send(JSON.stringify({ action: "attack", data: "Player2" }))
```

---

## Issue #30 — 前端 HUD + UI 重构（zy/gmy）

### HUD 布局（Canvas 叠加）

```
┌──────────────────────────────────────────┐
│ ❤ 100/100  ⚔ 10  🛡 5   Room: outside  │ ← 顶栏 HUD
│                                          │
│                                          │
│            Canvas 游戏区域                │
│         (15×10 tiles × 32px)             │
│                                          │
│                                          │
├──────────────────────────────────────────┤
│ [1] Sword  [2] BloodVial  [I] 背包      │ ← 底栏快捷物品
│ 消息: You took the Sword.                │ ← 消息滚动
└──────────────────────────────────────────┘
```

### 快捷键

| 按键 | 功能 |
|------|------|
| WASD / 方向键 | 移动 |
| 空格 | 拾取脚下物品 |
| I | 打开/关闭背包 |
| 1-4 | 快捷使用物品 |
| Tab | 切换目标（攻击用） |
| Enter | 攻击当前目标 |
| F1 | 帮助 |

### 小地图

```
右上角小地图显示当前房间的缩略图（3px每格）
```

---

## Issue #31 — 房间过渡动画 + 传送特效（gmy）

### 房间切换

```
1. 玩家走到门上
2. 前端收到 roomPush（新房间名）
3. 100ms 黑屏 canvas.fillStyle = 'black'
4. 加载新房间地图
5. 200ms 淡入（opacity 0 → 1）
```

### 传送特效

```
1. 走进传送房间的门
2. 收到 backend 传送消息 → 紫色粒子爆发
3. 黑屏
4. 新房间淡入
5. 提示消息："A mysterious force transports you..."
```

### 受伤特效

```
1. 收到 playerPush（当前血量降低）
2. 屏幕闪烁红色
3. 玩家精灵闪动 3 次
```

### 战斗特效

```
攻击命中 → 目标闪动红色
使用道具 → 绿色粒子
拾取物品 → 物品缩小消失动画
```

---

## 开发依赖顺序

```
          ┌──────────────┐
          │ #25 Room 2D   │ (后端基础)
          └──────┬───────┘
                 │
          ┌──────▼───────┐
          │ #26 Player 2D │ (后端逻辑)
          └──────┬───────┘
                 │
     ┌───────────┼───────────┐
     │           │           │
┌────▼───┐ ┌────▼───┐ ┌────▼───┐
│ #27    │ │ #28    │ │ #29    │
│ 渲染    │ │ 移动   │ │ 物品   │
│ 引擎   │ │ 切换   │ │ 交互   │
└────┬───┘ └────┬───┘ └────┬───┘
     │          │          │
     └──────────┼──────────┘
                │
         ┌──────▼───────┐
         │ #30 HUD/UI   │
         └──────┬───────┘
                │
         ┌──────▼───────┐
         │ #31 动画特效  │
         └──────────────┘
```

---

## 跨 Issue 调用汇总

| 调用方 | 调用的类.方法 | 所属 Issue |
|--------|-------------|-----------|
| 前端键盘 | `GameWebSocketHandler.movePlayer(Player,dx,dy)` | #26 |
| 前端空格 | `Room.takeItemAt(x,y)` → `Player.takeItem()` | #25 #29 |
| 撞墙检测 | `Room.isWalkable(x,y)` | #25 |
| 门触发 | `Room.getDoorDirection(x,y)` | #25 |
| 房间切换 | `Player.moveTo(Room)` | #26 |
| 玩家位置推送 | `PlayerVO.from(Player)` 含 posX,posY | #26 |
| 物品位置推送 | `RoomVO.from(Room)` 含 items[{name,x,y}] | #25 |
| Canvas 渲染 | `Room.tiles[][]` `Room.itemPositions` | #27 |
| 攻击 | `AttackCommand` 现有逻辑 + 距离检测 | #11 |
