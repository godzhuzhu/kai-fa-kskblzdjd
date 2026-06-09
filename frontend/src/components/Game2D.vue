<template>
  <div class="game2d-container">
    <div v-if="disconnected" class="reconnect-banner">
      Connection lost. <a href="#" @click.prevent="reconnect">Reconnect</a>
    </div>
    <canvas ref="canvas" class="game-canvas"></canvas>
    <div class="hud-top">
      <span class="hud-item">❤ {{ player.currentHealth }}/{{ player.maxHealth }}</span>
      <span class="hud-item">⚔ {{ player.attack }}</span>
      <span class="hud-item">🛡 {{ player.defense }}</span>
      <span class="hud-item">Room: {{ room.roomName }}</span>
    </div>
    <div class="hud-bottom">
      <div class="bag-bar">
        <span v-for="(item, i) in player.bag" :key="i" class="bag-slot" @click="useItem(item)" :title="item.name">
          {{ item.name.substring(0,2) }}
        </span>
        <span v-if="!player.bag || player.bag.length === 0" class="bag-empty">Bag empty. Walk onto items + Space to pick up</span>
      </div>
      <div class="msg-bar">{{ lastMessage }}</div>
    </div>
    <div class="info-panel" v-if="room.players && room.players.length > 1">
      <div v-for="p in room.players" :key="p.userId" v-if="p.userId !== player.userId" class="other-player">
        {{ p.playerName }} ❤{{ p.currentHealth }}/{{ p.maxHealth }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'

const WS_URL = 'ws://localhost:8080/game/websocket'
const TILE_SIZE = 32

const canvas = ref<HTMLCanvasElement | null>(null)
const player = reactive<any>({ userId: 0, playerName: '', attack: 0, defense: 0, currentHealth: 100, maxHealth: 100, maxCapacity: 50, currentLoad: 0, bag: [], posX: 1, posY: 1, currentRoomName: '' })
const room = reactive<any>({ roomName: '', description: '', tiles: [], items: [], players: [], portal: false })
const lastMessage = ref('')
const disconnected = ref(false)

let ws: WebSocket | null = null
let heartbeatTimer: any = null
let ctx: CanvasRenderingContext2D | null = null
let keys: Record<string, boolean> = {}
let lastMoveTime = 0

const playerColors: Record<number, string> = {}
function getPlayerColor(uid: number) {
  if (!playerColors[uid]) {
    const colors = ['#4fc3f7', '#f48fb1', '#a5d6a7', '#ffcc80', '#ce93d8', '#ef9a9a']
    playerColors[uid] = colors[Object.keys(playerColors).length % colors.length]
  }
  return playerColors[uid]
}

function draw() {
  if (!ctx || !canvas.value) return
  const c = ctx
  c.clearRect(0, 0, canvas.value.width, canvas.value.height)

  if (!room.tiles || room.tiles.length === 0) return

  for (let y = 0; y < room.tiles.length; y++) {
    for (let x = 0; x < room.tiles[y].length; x++) {
      const tile = room.tiles[y][x]
      const px = x * TILE_SIZE
      const py = y * TILE_SIZE

      if (tile === 1) {
        c.fillStyle = '#2d1f0e'
        c.fillRect(px, py, TILE_SIZE, TILE_SIZE)
        c.strokeStyle = '#1a0f05'
        c.lineWidth = 1
        c.strokeRect(px, py, TILE_SIZE, TILE_SIZE)
        c.fillStyle = '#3d2d1a'
        c.fillRect(px + 2, py + 2, TILE_SIZE - 4, TILE_SIZE - 4)
      } else {
        c.fillStyle = '#1a1a2e'
        c.fillRect(px, py, TILE_SIZE, TILE_SIZE)
        c.fillStyle = '#16213e'
        c.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2)
        if ((x + y) % 2 === 0) {
          c.fillStyle = '#1a1a3e'
          c.fillRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2)
        }
      }

      if (tile >= 2 && tile <= 5) {
        c.fillStyle = '#4a7c59'
        c.fillRect(px, py, TILE_SIZE, TILE_SIZE)
        c.fillStyle = '#6abf69'
        c.font = '20px monospace'
        c.textAlign = 'center'
        c.textBaseline = 'middle'
        const arrows = ['', '', '\u25B2', '\u25BC', '\u25C0', '\u25B6']
        c.fillText(arrows[tile] || '\u25A0', px + TILE_SIZE / 2, py + TILE_SIZE / 2)
      }
    }
  }

  if (room.items) {
    for (const item of room.items) {
      if (item.x !== undefined && item.y !== undefined) {
        const px = item.x * TILE_SIZE + TILE_SIZE / 2
        const py = item.y * TILE_SIZE + TILE_SIZE / 2
        c.fillStyle = '#ffd700'
        c.beginPath()
        c.arc(px, py, 8, 0, Math.PI * 2)
        c.fill()
        c.fillStyle = '#000'
        c.font = '8px monospace'
        c.textAlign = 'center'
        c.fillText(item.name.substring(0, 2), px, py + 3)
      }
    }
  }

  if (room.players) {
    for (const p of room.players) {
      if (p.userId === player.userId) continue
      const px = (p.posX !== undefined ? p.posX : 1) * TILE_SIZE + TILE_SIZE / 2
      const py = (p.posY !== undefined ? p.posY : 1) * TILE_SIZE + TILE_SIZE / 2
      c.fillStyle = getPlayerColor(p.userId)
      c.beginPath()
      c.arc(px, py, 10, 0, Math.PI * 2)
      c.fill()
      c.fillStyle = '#fff'
      c.font = '10px monospace'
      c.textAlign = 'center'
      c.fillText(p.playerName.substring(0, 2), px, py + 3)
    }
  }

  const ppx = (player.posX || 1) * TILE_SIZE + TILE_SIZE / 2
  const ppy = (player.posY || 1) * TILE_SIZE + TILE_SIZE / 2
  c.fillStyle = '#4caf50'
  c.beginPath()
  c.arc(ppx, ppy, 12, 0, Math.PI * 2)
  c.fill()
  c.fillStyle = '#fff'
  c.font = '12px monospace'
  c.textAlign = 'center'
  c.textBaseline = 'middle'
  c.fillText('@', ppx, ppy)

  requestAnimationFrame(draw)
}

function handleInput() {
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  const now = Date.now()
  if (now - lastMoveTime < 120) return

  let dx = 0, dy = 0
  if (keys['w'] || keys['arrowup']) dy = -1
  else if (keys['s'] || keys['arrowdown']) dy = 1
  else if (keys['a'] || keys['arrowleft']) dx = -1
  else if (keys['d'] || keys['arrowright']) dx = 1

  if (dx !== 0 || dy !== 0) {
    ws.send(JSON.stringify({ action: 'move', data: JSON.stringify({ dx, dy }), token: sessionStorage.getItem('token') }))
    lastMoveTime = now
  }
}

function useItem(item: any) {
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({ action: 'use', data: item.name, token: sessionStorage.getItem('token') }))
}

function connect() {
  const token = sessionStorage.getItem('token')
  if (!token) return
  disconnected.value = false

  try { ws = new WebSocket(WS_URL + '?token=' + token) } catch { disconnected.value = true; return }

  ws.onopen = () => {
    heartbeatTimer = setInterval(() => {
      if (ws?.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ action: 'heartbeat', data: null, token: sessionStorage.getItem('token') }))
      }
    }, 30000)
  }

  ws.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data)
      if (payload.type === 'playerPush') {
        Object.assign(player, payload.data)
      } else if (payload.type === 'roomPush') {
        Object.assign(room, payload.data)
      } else if (payload.type === 'messagePush') {
        lastMessage.value = payload.data
      }
    } catch { lastMessage.value = event.data }
  }

  ws.onclose = () => { clearInterval(heartbeatTimer); disconnected.value = true; setTimeout(connect, 3000) }
  ws.onerror = () => ws?.close()
}

function reconnect() { if (ws) { ws.close() } else { connect() } }

onMounted(() => {
  if (canvas.value) {
    canvas.value.width = 15 * TILE_SIZE
    canvas.value.height = 10 * TILE_SIZE
    ctx = canvas.value.getContext('2d')
  }
  window.addEventListener('keydown', (e) => { keys[e.key.toLowerCase()] = true; if (e.key === ' ') { e.preventDefault(); if (ws?.readyState === WebSocket.OPEN) ws.send(JSON.stringify({ action: 'interact', data: null, token: sessionStorage.getItem('token') })) } })
  window.addEventListener('keyup', (e) => { keys[e.key.toLowerCase()] = false })
  setInterval(handleInput, 50)
  connect()
  draw()
})

onUnmounted(() => {
  clearInterval(heartbeatTimer)
  ws?.close()
})
</script>

<style scoped>
.game2d-container {
  width: 100vw; height: 100vh; background: #0a0a14; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; overflow: hidden;
}
.game-canvas {
  border: 2px solid #333; image-rendering: pixelated;
}
.reconnect-banner {
  position: absolute; top: 0; left: 0; right: 0; background: #f56c6c; color: #fff; text-align: center; padding: 6px; z-index: 100; font-size: 13px;
}
.reconnect-banner a { color: #fff; font-weight: bold; }
.hud-top {
  position: absolute; top: 5px; left: 50%; transform: translateX(-50%); display: flex; gap: 20px; color: #f0d9a0; font-size: 14px; font-family: monospace; background: rgba(0,0,0,0.7); padding: 4px 16px; border-radius: 8px;
}
.hud-item { white-space: nowrap; }
.hud-bottom {
  position: absolute; bottom: 5px; left: 50%; transform: translateX(-50%); display: flex; flex-direction: column; align-items: center; gap: 4px;
}
.bag-bar {
  display: flex; gap: 4px;
}
.bag-slot {
  background: rgba(255,255,255,0.1); color: #ffd700; padding: 2px 8px; border-radius: 4px; cursor: pointer; font-size: 12px; font-family: monospace;
}
.bag-slot:hover { background: rgba(255,255,255,0.2); }
.bag-empty { color: #666; font-size: 11px; font-family: monospace; }
.msg-bar {
  background: rgba(0,0,0,0.6); color: #aaa; font-size: 11px; padding: 2px 12px; border-radius: 4px; max-width: 400px; overflow: hidden; white-space: nowrap; font-family: monospace;
}
.info-panel {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.7); padding: 8px; border-radius: 8px;
}
.other-player { color: #aaa; font-size: 12px; font-family: monospace; padding: 2px; }
</style>
