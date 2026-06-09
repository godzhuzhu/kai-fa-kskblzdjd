<template>
  <div class="game2d-container">
    <div v-if="disconnected" class="reconnect-banner">
      Connection lost. <a href="#" @click.prevent="reconnect">Reconnect</a>
    </div>
    <div v-if="!connected" class="loading">Connecting...</div>
    <canvas ref="canvas" class="game-canvas" :style="{display: connected ? 'block' : 'none'}"></canvas>
    <div class="hud-top" v-if="connected">
      <span class="hud-item">❤ {{ player.currentHealth }}/{{ player.maxHealth }}</span>
      <span class="hud-item">⚔ {{ player.attack }}</span>
      <span class="hud-item">🛡 {{ player.defense }}</span>
      <span class="hud-item">Room: {{ room.roomName }}</span>
      <span class="hud-item">XY: {{ player.posX }},{{ player.posY }}</span>
    </div>
    <div class="hud-bottom" v-if="connected">
      <div class="cmd-row">
        <input v-model="cmdInput" class="cmd-input" placeholder="command: save/load/attack name/help" @keyup.enter="sendCmd" />
        <button class="cmd-btn" @click="sendCmd">Send</button>
      </div>
      <div class="bag-bar">
        <span v-for="(item, i) in player.bag" :key="i" class="bag-slot"
              :class="{ weapon: item.range > 0, consumable: item.range === 0 }"
              @click="useItem(item)"
              :title="item.name + (item.range ? ' ['+item.type+' rng:'+item.range+']' : ' [use]')">
          <span class="slot-icon">{{ item.range ? '\u2694' : '\u2668' }}</span>
          <span class="slot-name">{{ item.name }}</span>
          <span class="slot-weight">{{ item.weight }}kg</span>
        </span>
        <span v-if="!player.bag || player.bag.length === 0" class="bag-empty">Backpack empty. Walk to gold dots + Space</span>
      </div>
      <div class="key-hints">WASD=move J=atk Space=pick E/Q=use/drop F5=save F9=load</div>
      <div class="msg-bar">{{ lastMessage }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'

const TILE = 32
const WS_URL = 'ws://localhost:8080/game/websocket'

const canvas = ref<HTMLCanvasElement | null>(null)
const player = reactive<any>({
  userId: 0, playerName: '', attack: 10, defense: 5,
  currentHealth: 100, maxHealth: 100,
  maxCapacity: 50, currentLoad: 0, bag: [],
  posX: 1, posY: 1, currentRoomName: ''
})
const room = reactive<any>({
  roomName: '', tiles: null, items: [], players: [],
  description: '', portal: false
})
const lastMessage = ref('')
const disconnected = ref(false)
const connected = ref(false)
const cmdInput = ref('')

let ws: WebSocket | null = null
let ctx: CanvasRenderingContext2D | null = null
let keys: Record<string, boolean> = {}
let lastMove = 0
let animId = 0
let lastDx = 1
let lastDy = 0
let hitEffects: {x: number, y: number, time: number}[] = []
let attackEffects: {x: number, y: number, dx: number, dy: number, type: string, time: number}[] = []

const playerCols: Record<number, string> = {}
function getCol(uid: number) {
  if (!playerCols[uid]) {
    const c = ['#4fc3f7', '#f48fb1', '#a5d6a7', '#ffcc80', '#ce93d8']
    playerCols[uid] = c[Object.keys(playerCols).length % c.length]
  }
  return playerCols[uid]
}

function render() {
  if (!ctx || !canvas.value) { animId = requestAnimationFrame(render); return }
  const c = ctx
  c.clearRect(0, 0, canvas.value.width, canvas.value.height)

  if (!room.tiles || room.tiles.length === 0) {
    c.fillStyle = '#0a0a14'; c.fillRect(0, 0, canvas.value.width, canvas.value.height)
    c.fillStyle = '#f0d9a0'; c.font = '16px monospace'; c.textAlign = 'center'
    c.fillText('Loading map...', canvas.value.width / 2, canvas.value.height / 2)
    animId = requestAnimationFrame(render); return
  }

  for (let y = 0; y < room.tiles.length; y++) {
    for (let x = 0; x < room.tiles[y].length; x++) {
      const tile = room.tiles[y][x]
      const px = x * TILE, py = y * TILE
      if (tile === 1) {
        c.fillStyle = '#2d1f0e'; c.fillRect(px, py, TILE, TILE)
        c.strokeStyle = '#3d2d1a'; c.lineWidth = 1; c.strokeRect(px, py, TILE, TILE)
      } else {
        c.fillStyle = (x + y) % 2 ? '#16213e' : '#1a1a2e'; c.fillRect(px, py, TILE, TILE)
      }
      if (tile >= 2 && tile <= 5) {
        c.fillStyle = '#3a5a3a'; c.fillRect(px, py, TILE, TILE)
        c.fillStyle = '#7cff7c'; c.font = '20px monospace'; c.textAlign = 'center'; c.textBaseline = 'middle'
        const a = ['', '', '\u25B2', '\u25BC', '\u25C0', '\u25B6']
        c.fillText(a[tile] || '\u25A0', px + TILE / 2, py + TILE / 2)
      }
    }
  }

  if (room.items) {
    for (const item of room.items) {
      if (item.x != null && item.y != null && !isNaN(item.x) && !isNaN(item.y)) {
        c.fillStyle = '#ffd700'
        c.beginPath(); c.arc(item.x * TILE + TILE / 2, item.y * TILE + TILE / 2, 8, 0, Math.PI * 2); c.fill()
        c.fillStyle = '#000'; c.font = '7px monospace'; c.textAlign = 'center'
        c.fillText(String(item.name || '?').substring(0, 2), item.x * TILE + TILE / 2, item.y * TILE + TILE / 2 + 3)
      }
    }
  }

  if (room.players) {
    for (const p of room.players) {
      if (p.userId === player.userId) continue
      const px = (Number(p.posX) || 1) * TILE + TILE / 2, py = (Number(p.posY) || 1) * TILE + TILE / 2
      c.fillStyle = getCol(p.userId)
      c.beginPath(); c.arc(px, py, 10, 0, Math.PI * 2); c.fill()
      c.fillStyle = '#fff'; c.font = '10px monospace'; c.textAlign = 'center'; c.textBaseline = 'middle'
      c.fillText('@', px, py)
    }
  }

  const ppx = (Number(player.posX) || 1) * TILE + TILE / 2, ppy = (Number(player.posY) || 1) * TILE + TILE / 2
  c.fillStyle = '#4caf50'
  c.beginPath(); c.arc(ppx, ppy, 12, 0, Math.PI * 2); c.fill()
  c.fillStyle = '#fff'; c.font = '12px monospace'; c.textAlign = 'center'; c.textBaseline = 'middle'
  c.fillText('@', ppx, ppy)

  // direction indicator
  const arrows: Record<string, string> = { '-1:0': '\u25C0', '1:0': '\u25B6', '0:-1': '\u25B2', '0:1': '\u25BC' }
  const arr = arrows[lastDx + ':' + lastDy] || '\u25B6'
  c.fillStyle = '#ff6666'; c.font = '16px monospace'; c.textAlign = 'center'; c.textBaseline = 'middle'
  c.fillText(arr, ppx + lastDx * 16, ppy + lastDy * 16)

  // hit effects
  const now = Date.now()
  hitEffects = hitEffects.filter(h => now - h.time < 400)
  for (const h of hitEffects) {
    const age = now - h.time
    const alpha = 1 - age / 400
    c.fillStyle = `rgba(255,60,60,${alpha})`
    c.beginPath(); c.arc(h.x * TILE + TILE / 2, h.y * TILE + TILE / 2, 6 + age / 30, 0, Math.PI * 2); c.fill()
  }

  // attack slash effects
  attackEffects = attackEffects.filter(a => now - a.time < 500)
  for (const a of attackEffects) {
    const age = now - a.time
    const alpha = 1 - age / 500
    const sx = a.x * TILE + TILE / 2 + a.dx * TILE
    const sy = a.y * TILE + TILE / 2 + a.dy * TILE
    if (a.type === 'melee') {
      c.strokeStyle = `rgba(255,200,50,${alpha})`
      c.lineWidth = 3
      c.beginPath(); c.arc(sx, sy, 8 + age / 20, 0, Math.PI * 2); c.stroke()
    } else if (a.type === 'ranged') {
      c.strokeStyle = `rgba(100,200,255,${alpha})`
      c.lineWidth = 2
      c.beginPath()
      let cx = a.x * TILE + TILE / 2, cy = a.y * TILE + TILE / 2
      c.moveTo(cx, cy)
      for (let i = 0; i <= 3; i++) {
        cx += a.dx * TILE; cy += a.dy * TILE
        c.lineTo(cx, cy)
      }
      c.stroke()
    } else if (a.type === 'aoe') {
      c.strokeStyle = `rgba(255,150,50,${alpha})`
      c.lineWidth = 2
      c.beginPath(); c.arc(sx, sy, 6 + age / 15, 0, Math.PI * 2); c.stroke()
      c.beginPath()
      c.moveTo(sx - TILE, sy - TILE); c.lineTo(sx + TILE, sy + TILE)
      c.moveTo(sx - TILE, sy + TILE); c.lineTo(sx + TILE, sy - TILE)
      c.stroke()
    }
  }

  animId = requestAnimationFrame(render)
}

function inputLoop() {
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  const now = Date.now()
  if (now - lastMove < 120) return
  let dx = 0, dy = 0
  if (keys['w'] || keys['arrowup']) dy = -1
  else if (keys['s'] || keys['arrowdown']) dy = 1
  else if (keys['a'] || keys['arrowleft']) dx = -1
  else if (keys['d'] || keys['arrowright']) dx = 1
  if (dx || dy) {
    lastDx = dx; lastDy = dy
    ws.send(JSON.stringify({ action: 'move', data: JSON.stringify({ dx, dy }), token: sessionStorage.getItem('token') }))
    lastMove = now
  }
}

function useItem(item: any) {
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({ action: 'use', data: item.name, token: sessionStorage.getItem('token') }))
}

function dropItem(item: any) {
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  ws.send(JSON.stringify({ action: 'drop', data: item.name, token: sessionStorage.getItem('token') }))
}

function sendCmd() {
  if (!ws || ws.readyState !== WebSocket.OPEN) return
  const cmd = cmdInput.value.trim()
  if (!cmd) return
  ws.send(JSON.stringify({ action: 'command', data: cmd, token: sessionStorage.getItem('token') }))
  cmdInput.value = ''
}

function connect() {
  const token = sessionStorage.getItem('token')
  if (!token) return
  disconnected.value = false
  try {
    ws = new WebSocket(WS_URL + '?token=' + token)
  } catch {
    disconnected.value = true; return
  }
  ws.onopen = () => {
    connected.value = true
    setInterval(() => {
      if (ws?.readyState === WebSocket.OPEN)
        ws.send(JSON.stringify({ action: 'heartbeat', data: null, token: sessionStorage.getItem('token') }))
    }, 30000)
  }
  ws.onmessage = (e) => {
    try {
      const p = JSON.parse(e.data)
      if (p.type === 'playerPush') { Object.assign(player, p.data) }
      else if (p.type === 'roomPush') {
        Object.assign(room, p.data)
        if (p.data && p.data.players) {
          for (const rp of p.data.players) {
            if (rp.currentHealth < 100) hitEffects.push({ x: rp.posX || 1, y: rp.posY || 1, time: Date.now() })
          }
        }
      }
      else if (p.type === 'messagePush') { lastMessage.value = p.data }
    } catch { lastMessage.value = e.data }
  }
  ws.onclose = () => { connected.value = false; disconnected.value = true; setTimeout(connect, 3000) }
  ws.onerror = () => ws?.close()
}

function reconnect() {
  if (ws) ws.close()
  else connect()
}

onMounted(() => {
  nextTick(() => {
    if (canvas.value) {
      canvas.value.width = 15 * TILE; canvas.value.height = 10 * TILE
      ctx = canvas.value.getContext('2d')
      render()
    }
  })
  window.addEventListener('keydown', e => {
    keys[e.key.toLowerCase()] = true
    if (e.key === ' ') {
      e.preventDefault()
      if (ws?.readyState === WebSocket.OPEN)
        ws.send(JSON.stringify({ action: 'interact', data: null, token: sessionStorage.getItem('token') }))
    }
    if (e.key === 'e' && player.bag && player.bag.length > 0) {
      useItem(player.bag[player.bag.length - 1])
    }
    if (e.key === 'q' && player.bag && player.bag.length > 0) {
      dropItem(player.bag[player.bag.length - 1])
    }
    if (e.key === 'j') {
      e.preventDefault()
      if (ws?.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ action: 'attack', data: JSON.stringify({ dx: lastDx, dy: lastDy }), token: sessionStorage.getItem('token') }))
        // local attack effect
        let atkType = 'melee'
        if (player.bag) {
          for (const item of player.bag) {
            if (item.range && item.range > 0) { atkType = item.type || 'melee'; break }
          }
        }
        attackEffects.push({ x: player.posX || 1, y: player.posY || 1, dx: lastDx, dy: lastDy, type: atkType, time: Date.now() })
      }
    }
    if (e.key === 'F5') {
      e.preventDefault()
      if (ws?.readyState === WebSocket.OPEN)
        ws.send(JSON.stringify({ action: 'command', data: 'save', token: sessionStorage.getItem('token') }))
    }
    if (e.key === 'F9') {
      e.preventDefault()
      if (ws?.readyState === WebSocket.OPEN)
        ws.send(JSON.stringify({ action: 'command', data: 'load', token: sessionStorage.getItem('token') }))
    }
  })
  window.addEventListener('keyup', e => { keys[e.key.toLowerCase()] = false })
  setInterval(inputLoop, 60)
  connect()
})

onUnmounted(() => {
  cancelAnimationFrame(animId)
  ws?.close()
})
</script>

<style scoped>
.game2d-container {
  width: 100vw; height: 100vh; background: #0a0a14;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.game-canvas { border: 2px solid #333; image-rendering: pixelated; }
.reconnect-banner {
  position: absolute; top: 0; left: 0; right: 0; background: #f56c6c;
  color: #fff; text-align: center; padding: 6px; z-index: 100; font-size: 13px;
}
.reconnect-banner a { color: #fff; font-weight: bold; }
.loading { color: #f0d9a0; font-size: 18px; font-family: monospace; }
.hud-top {
  position: absolute; top: 5px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 16px; color: #f0d9a0; font-size: 13px; font-family: monospace;
  background: rgba(0,0,0,0.7); padding: 4px 16px; border-radius: 8px; z-index: 10;
}
.hud-item { white-space: nowrap; }
.hud-bottom {
  position: absolute; bottom: 5px; left: 50%; transform: translateX(-50%);
  display: flex; flex-direction: column; align-items: center; gap: 3px; z-index: 10;
}
.bag-bar { display: flex; gap: 3px; flex-wrap: wrap; max-width: 400px; justify-content: center; }
.bag-slot {
  background: rgba(0,0,0,0.5); color: #ccc; padding: 2px 8px; border-radius: 4px;
  cursor: pointer; font-size: 10px; font-family: monospace; display: flex; gap: 4px; align-items: center;
  border: 1px solid #333; transition: all 0.15s;
}
.bag-slot:hover { border-color: #888; }
.bag-slot.weapon { border-color: #5a3a2a; background: rgba(90,50,20,0.3); }
.bag-slot.consumable { border-color: #2a4a5a; background: rgba(20,50,90,0.3); }
.slot-icon { font-size: 12px; }
.slot-name { color: #ddd; }
.slot-weight { color: #666; }
.key-hints { color: #555; font-size: 9px; font-family: monospace; margin-top: 2px; }
.bag-empty { color: #666; font-size: 11px; font-family: monospace; }
.msg-bar {
  background: rgba(0,0,0,0.6); color: #aaa; font-size: 11px; padding: 2px 12px;
  border-radius: 4px; max-width: 400px; overflow: hidden; white-space: nowrap; font-family: monospace;
}
.cmd-row { display: flex; gap: 4px; }
.cmd-input {
  width: 260px; background: rgba(0,0,0,0.5); border: 1px solid #444;
  color: #ccc; padding: 2px 8px; font-size: 12px; font-family: monospace; border-radius: 4px;
}
.cmd-btn {
  background: rgba(255,255,255,0.1); border: 1px solid #555; color: #ccc;
  padding: 2px 10px; border-radius: 4px; cursor: pointer; font-size: 11px;
}
.cmd-btn:hover { background: rgba(255,255,255,0.2); }
</style>
