<template>
  <div class="game-container">
    <div v-if="disconnected" class="reconnect-banner">
      Connection lost. <a href="#" @click.prevent="reconnect">Click here to reconnect</a>
    </div>

    <div class="game-layout">
      <!-- Left Column: Player Info -->
      <div class="panel left-panel">
        <div class="panel-title">Player Info</div>
        <div class="player-stats">
          <div class="stat-row"><span class="stat-label">Name</span><span class="stat-value">{{ player.playerName || '-' }}</span></div>
          <div class="stat-row"><span class="stat-label">Health</span>
            <div class="hp-bar">
              <div class="hp-fill" :style="{ width: hpPercent + '%' }"></div>
              <span class="hp-text">{{ player.currentHealth }}/{{ player.maxHealth }}</span>
            </div>
          </div>
          <div class="stat-row"><span class="stat-label">Attack</span><span class="stat-value">{{ player.attack }}</span></div>
          <div class="stat-row"><span class="stat-label">Defense</span><span class="stat-value">{{ player.defense }}</span></div>
        </div>
        <el-divider style="border-color: rgba(255,255,255,0.1); margin: 12px 0" />
        <div class="panel-title">Inventory</div>
        <div class="weight-info">Weight: {{ player.currentLoad }}/{{ player.maxCapacity }}</div>
        <div class="item-list">
          <div v-for="item in player.bag" :key="item.name" class="item-row clickable" @click="handleItemAction(item)">
            <span>{{ item.name }}</span>
            <span class="item-weight">{{ item.weight }}kg</span>
          </div>
          <div v-if="!player.bag || player.bag.length === 0" class="empty-hint">Empty</div>
        </div>
      </div>

      <!-- Center Column: Room Info -->
      <div class="panel center-panel">
        <div class="panel-title">{{ room.roomName || 'Unknown Room' }}</div>
        <div class="room-desc">{{ room.description || '' }}</div>

        <el-divider style="border-color: rgba(255,255,255,0.1); margin: 12px 0" />
        <div class="section-label">Exits</div>
        <div class="exit-buttons">
          <el-button v-for="exit in room.exits" :key="exit" size="small" class="exit-btn" @click="sendCommand('go ' + exit)">
            Go {{ exit }}
          </el-button>
        </div>

        <el-divider style="border-color: rgba(255,255,255,0.1); margin: 12px 0" />
        <div class="section-label">Items in Room</div>
        <div class="item-list">
          <div v-for="item in room.items" :key="item.name" class="item-row clickable" @click="handleItemAction(item, true)">
            <span>{{ item.name }}</span>
            <span class="item-weight">{{ item.weight }}kg</span>
          </div>
          <div v-if="!room.items || room.items.length === 0" class="empty-hint">No items</div>
        </div>

        <el-divider style="border-color: rgba(255,255,255,0.1); margin: 12px 0" />
        <div class="action-buttons">
          <el-button size="small" class="action-btn" @click="sendCommand('back')">Back</el-button>
          <el-button size="small" class="action-btn" @click="sendCommand('help')">Help</el-button>
          <el-button size="small" class="action-btn" @click="sendCommand('look')">Look</el-button>
          <el-button size="small" class="action-btn" @click="sendCommand('items')">Items</el-button>
        </div>

        <!-- Command Input -->
        <el-divider style="border-color: rgba(255,255,255,0.1); margin: 12px 0" />
        <div class="command-row">
          <el-input v-model="commandInput" placeholder="Type a command..." class="command-input" @keyup.enter="sendCommand(commandInput)" />
          <el-button type="primary" size="small" class="send-btn" @click="sendCommand(commandInput)">Send</el-button>
        </div>
      </div>

      <!-- Right Column: Players + Messages -->
      <div class="panel right-panel">
        <div class="panel-title">Players Here</div>
        <div class="player-list">
          <div v-for="p in room.players" :key="p.userId" class="room-player-row" :class="{ 'is-self': p.userId === player.userId }">
            <div class="player-info">
              <span class="player-name">{{ p.playerName }}</span>
              <span v-if="p.userId === player.userId" class="self-tag">(you)</span>
              <div class="mini-hp">
                <div class="mini-hp-fill" :style="{ width: (p.currentHealth / (p.maxHealth || 100) * 100) + '%' }"></div>
              </div>
            </div>
            <el-button v-if="p.userId !== player.userId" size="small" class="attack-btn" @click="attackPlayer(p)">Attack</el-button>
          </div>
          <div v-if="!room.players || room.players.length === 0" class="empty-hint">No other players</div>
        </div>

        <el-divider style="border-color: rgba(255,255,255,0.1); margin: 12px 0" />
        <div class="panel-title">Messages</div>
        <div class="message-area" ref="messageArea">
          <div v-for="(msg, i) in messages" :key="i" class="message-line">{{ msg }}</div>
        </div>
      </div>
    </div>

    <!-- Item Action Dialog -->
    <el-dialog v-model="itemDialogVisible" :title="'Item: ' + (selectedItem ? selectedItem.name : '')" width="300px" top="30vh" class="item-dialog">
      <div class="dialog-actions">
        <el-button v-if="selectedItemInRoom" type="primary" @click="doItemAction('take')">Take</el-button>
        <el-button v-if="!selectedItemInRoom" type="primary" @click="doItemAction('drop')">Drop</el-button>
        <el-button v-if="!selectedItemInRoom" type="success" @click="doItemAction('use')">Use</el-button>
        <el-button @click="itemDialogVisible = false">Cancel</el-button>
      </div>
    </el-dialog>

    <!-- Attack Confirm Dialog -->
    <el-dialog v-model="attackDialogVisible" title="Confirm Attack" width="300px" top="30vh" class="item-dialog">
      <p style="color: #ccc; margin-bottom: 20px">Attack {{ attackTargetName }}?</p>
      <div class="dialog-actions">
        <el-button type="danger" @click="doAttack">Attack!</el-button>
        <el-button @click="attackDialogVisible = false">Cancel</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
const WS_URL = `ws://${window.location.hostname === 'localhost' ? 'localhost:8080' : window.location.host}/game/websocket`

const player = reactive<any>({
  userId: 0, playerName: '', attack: 0, defense: 0,
  currentHealth: 100, maxHealth: 100,
  currentLoad: 0, maxCapacity: 50, bag: []
})
const room = reactive<any>({ roomName: '', description: '', exits: [], items: [], players: [] })
const messages = ref<string[]>([])
const commandInput = ref('')
const disconnected = ref(false)
const messageArea = ref<any>(null)

let ws: WebSocket | null = null
let heartbeatTimer: any = null
let reconnectTimer: any = null

// Item dialog state
const itemDialogVisible = ref(false)
const selectedItem = ref<any>(null)
const selectedItemInRoom = ref(false)

// Attack dialog state
const attackDialogVisible = ref(false)
const attackTarget = ref<any>(null)
const attackTargetName = ref('')

const hpPercent = computed(() => {
  if (!player.maxHealth) return 100
  return Math.max(0, (player.currentHealth / player.maxHealth) * 100)
})

function connect() {
  const token = sessionStorage.getItem('token')
  if (!token) return
  disconnected.value = false

  try {
    ws = new WebSocket(WS_URL + '?token=' + token)
  } catch {
    disconnected.value = true
    return
  }

  ws.onopen = () => {
    ws?.send(JSON.stringify({ action: 'login', data: null, token }))
    startHeartbeat()
  }

  ws.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data)
      if (payload.type === 'playerPush') {
        Object.assign(player, payload.data)
      } else if (payload.type === 'roomPush') {
        Object.assign(room, payload.data)
      } else if (payload.type === 'messagePush') {
        messages.value.push(payload.data)
        scrollToBottom()
      }
    } catch {
      messages.value.push(event.data)
      scrollToBottom()
    }
  }

  ws.onclose = () => {
    stopHeartbeat()
    disconnected.value = true
    scheduleReconnect()
  }

  ws.onerror = () => {
    ws?.close()
  }
}

function startHeartbeat() {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ action: 'heartbeat', data: null, token: sessionStorage.getItem('token') }))
    }
  }, 30000)
}

function stopHeartbeat() {
  if (heartbeatTimer) { clearInterval(heartbeatTimer); heartbeatTimer = null }
}

function scheduleReconnect() {
  if (reconnectTimer) clearTimeout(reconnectTimer)
  reconnectTimer = setTimeout(() => connect(), 3000)
}

function reconnect() {
  if (reconnectTimer) clearTimeout(reconnectTimer)
  connect()
}

function sendCommand(cmd: string) {
  if (!cmd || !cmd.trim()) return
  if (ws?.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ action: 'command', data: cmd.trim(), token: sessionStorage.getItem('token') }))
  }
  commandInput.value = ''
}

function scrollToBottom() {
  nextTick(() => {
    if (messageArea.value) {
      messageArea.value.scrollTop = messageArea.value.scrollHeight
    }
  })
}

function handleItemAction(item: any, inRoom = false) {
  selectedItem.value = item
  selectedItemInRoom.value = inRoom
  itemDialogVisible.value = true
}

function doItemAction(action: string) {
  if (!selectedItem.value) return
  sendCommand(action + ' ' + selectedItem.value.name)
  itemDialogVisible.value = false
}

function attackPlayer(p: any) {
  attackTarget.value = p
  attackTargetName.value = p.playerName
  attackDialogVisible.value = true
}

function doAttack() {
  if (attackTarget.value) {
    sendCommand('attack ' + attackTarget.value.playerName)
  }
  attackDialogVisible.value = false
}

onMounted(() => {
  connect()
})

onUnmounted(() => {
  stopHeartbeat()
  if (reconnectTimer) clearTimeout(reconnectTimer)
  ws?.close()
})
</script>

<style scoped>
.game-container {
  width: 100%;
  height: 100vh;
  background: #0a0a14;
  color: #e0e0e0;
  display: flex;
  flex-direction: column;
  position: relative;
}
.reconnect-banner {
  background: #f56c6c;
  color: #fff;
  text-align: center;
  padding: 8px;
  font-size: 14px;
  z-index: 100;
}
.reconnect-banner a {
  color: #fff;
  font-weight: bold;
}
.game-layout {
  display: grid;
  grid-template-columns: 260px 1fr 280px;
  flex: 1;
  overflow: hidden;
}
.panel {
  padding: 16px;
  overflow-y: auto;
  background: rgba(255,255,255,0.02);
  border-right: 1px solid rgba(255,255,255,0.05);
}
.right-panel {
  border-right: none;
  border-left: 1px solid rgba(255,255,255,0.05);
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #f0d9a0;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.player-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.stat-label {
  color: #888;
}
.stat-value {
  color: #f0d9a0;
  font-weight: 500;
}
.hp-bar {
  width: 120px;
  height: 18px;
  background: rgba(255,255,255,0.1);
  border-radius: 9px;
  position: relative;
  overflow: hidden;
}
.hp-fill {
  height: 100%;
  background: linear-gradient(90deg, #f56c6c, #e6a23c);
  border-radius: 9px;
  transition: width 0.3s;
}
.hp-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 11px;
  color: #fff;
  font-weight: 600;
  white-space: nowrap;
}
.weight-info {
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
}
.item-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.item-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 13px;
  background: rgba(255,255,255,0.03);
}
.item-row.clickable {
  cursor: pointer;
  transition: background 0.2s;
}
.item-row.clickable:hover {
  background: rgba(240, 217, 160, 0.1);
}
.item-weight {
  color: #666;
  font-size: 12px;
}
.empty-hint {
  color: #555;
  font-size: 13px;
  font-style: italic;
  padding: 8px;
}
.center-panel {
  display: flex;
  flex-direction: column;
}
.room-desc {
  font-size: 14px;
  line-height: 1.6;
  color: #ccc;
  margin-bottom: 4px;
}
.section-label {
  font-size: 12px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 8px;
}
.exit-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.exit-btn {
  --el-button-bg-color: rgba(240, 217, 160, 0.1);
  --el-button-border-color: rgba(240, 217, 160, 0.3);
  --el-button-text-color: #f0d9a0;
  --el-button-hover-bg-color: rgba(240, 217, 160, 0.2);
  --el-button-hover-border-color: #f0d9a0;
  --el-button-hover-text-color: #f0d9a0;
}
.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.action-btn {
  --el-button-bg-color: rgba(255,255,255,0.05);
  --el-button-border-color: rgba(255,255,255,0.1);
  --el-button-text-color: #ccc;
  --el-button-hover-bg-color: rgba(255,255,255,0.1);
  --el-button-hover-text-color: #fff;
}
.command-row {
  display: flex;
  gap: 8px;
}
.command-input {
  flex: 1;
  --el-input-bg-color: rgba(255,255,255,0.05);
  --el-input-border-color: rgba(255,255,255,0.1);
  --el-input-text-color: #e0e0e0;
  --el-input-placeholder-color: rgba(255,255,255,0.3);
  --el-input-hover-border-color: #f0d9a0;
  --el-input-focus-border-color: #f0d9a0;
}
.command-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(255,255,255,0.1) inset;
}
.send-btn {
  --el-button-bg-color: #c4a35a;
  --el-button-border-color: #c4a35a;
  --el-button-hover-bg-color: #d4b36a;
  --el-button-hover-border-color: #d4b36a;
}
.player-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.room-player-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-radius: 8px;
  background: rgba(255,255,255,0.03);
}
.room-player-row.is-self {
  background: rgba(240, 217, 160, 0.05);
  border: 1px solid rgba(240, 217, 160, 0.15);
}
.player-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.player-name {
  font-size: 14px;
  font-weight: 500;
}
.self-tag {
  font-size: 11px;
  color: #888;
}
.mini-hp {
  width: 80px;
  height: 6px;
  background: rgba(255,255,255,0.1);
  border-radius: 3px;
  overflow: hidden;
}
.mini-hp-fill {
  height: 100%;
  background: #f56c6c;
  border-radius: 3px;
  transition: width 0.3s;
}
.attack-btn {
  --el-button-bg-color: rgba(245, 108, 108, 0.15);
  --el-button-border-color: rgba(245, 108, 108, 0.3);
  --el-button-text-color: #f56c6c;
  --el-button-hover-bg-color: rgba(245, 108, 108, 0.25);
  --el-button-hover-border-color: #f56c6c;
  --el-button-hover-text-color: #f56c6c;
}
.message-area {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: calc(100vh - 320px);
}
.message-line {
  font-size: 13px;
  padding: 4px 8px;
  background: rgba(255,255,255,0.02);
  border-radius: 4px;
  color: #aaa;
  line-height: 1.4;
}
.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
.item-dialog :deep(.el-dialog__header) {
  color: #f0d9a0;
}
.item-dialog :deep(.el-dialog__body) {
  padding: 20px;
}
</style>
