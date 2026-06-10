<template>
  <div class="home-container">
    <div class="title-section">
      <h1 class="game-title">World of Zuul</h1>
      <p class="subtitle">踏上史诗冒险之旅</p>
    </div>
    <div class="form-card">
      <div class="tabs">
        <button :class="['tab', { active: activeTab === 'login' }]" @click="activeTab = 'login'">登录</button>
        <button :class="['tab', { active: activeTab === 'register' }]" @click="activeTab = 'register'">注册</button>
      </div>

      <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="form">
        <el-input v-model="loginForm.username" placeholder="用户名" class="input" :prefix-icon="User" />
        <el-input v-model="loginForm.password" type="password" placeholder="密码" class="input" show-password :prefix-icon="Lock" />
        <p v-if="loginError" class="error">{{ loginError }}</p>
        <el-button type="primary" native-type="submit" class="submit-btn" :loading="loginLoading">登录</el-button>
      </form>

      <form v-else @submit.prevent="handleRegister" class="form">
        <el-input v-model="registerForm.username" placeholder="用户名" class="input" :prefix-icon="User" />
        <el-input v-model="registerForm.password" type="password" placeholder="密码 (至少6位)" class="input" show-password :prefix-icon="Lock" />
        <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" class="input" show-password :prefix-icon="Lock" />
        <p v-if="registerError" class="error">{{ registerError }}</p>
        <el-button type="primary" native-type="submit" class="submit-btn" :loading="registerLoading">注册</el-button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const activeTab = ref<'login' | 'register'>('login')

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', confirmPassword: '' })
const loginError = ref('')
const registerError = ref('')
const loginLoading = ref(false)
const registerLoading = ref(false)

async function handleLogin() {
  loginError.value = ''
  if (!loginForm.username.trim()) {       loginError.value = '用户名不能为空'; return }
  if (!loginForm.password) { loginError.value = '密码不能为空'; return }
  loginLoading.value = true
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 15000)
  try {
    const res = await fetch('/api/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: loginForm.username, password: loginForm.password }),
      signal: controller.signal
    })
    clearTimeout(timeout)
    if (!res.ok) {
      loginError.value = '服务器错误 (' + res.status + ')'
      loginLoading.value = false
      return
    }
    const data = await res.json()
    if (data.success) {
      sessionStorage.setItem('token', data.data.token)
      sessionStorage.setItem('userId', data.data.userId)
      ElMessage.success('登录成功')
      router.push('/game')
    } else {
      loginError.value = data.message || '登录失败'
    }
  } catch (e: any) {
    if (e.name === 'AbortError') {
      loginError.value = '请求超时，请重试'
    } else {
      loginError.value = '网络错误，请重试'
    }
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  registerError.value = ''
  if (!registerForm.username.trim()) { registerError.value = '用户名不能为空'; return }
  if (registerForm.password.length < 6) { registerError.value = '密码至少需要6位'; return }
  if (registerForm.password !== registerForm.confirmPassword) { registerError.value = '两次密码不一致'; return }
  registerLoading.value = true
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 15000)
  try {
    const res = await fetch('/api/user/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: registerForm.username, password: registerForm.password }),
      signal: controller.signal
    })
    clearTimeout(timeout)
    if (!res.ok) {
      registerError.value = 'Server error (' + res.status + ')'
      registerLoading.value = false
      return
    }
    const data = await res.json()
    if (data.success) {
      ElMessage.success('注册成功，请登录')
      loginForm.username = registerForm.username
      loginForm.password = registerForm.password
      activeTab.value = 'login'
    } else {
      registerError.value = data.message || '注册失败'
    }
  } catch (e: any) {
    if (e.name === 'AbortError') {
      registerError.value = 'Request timed out, please try again'
    } else {
      registerError.value = 'Network error, please try again'
    }
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.home-container {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
}
.title-section {
  text-align: center;
}
.game-title {
  font-size: 64px;
  font-weight: 700;
  color: #f0d9a0;
  text-shadow: 0 0 30px rgba(240, 217, 160, 0.3), 0 4px 8px rgba(0,0,0,0.5);
  letter-spacing: 4px;
}
.subtitle {
  font-size: 18px;
  color: rgba(255,255,255,0.6);
  margin-top: 8px;
  letter-spacing: 2px;
}
.form-card {
  background: rgba(20, 20, 30, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 32px;
  width: 380px;
  border: 1px solid rgba(255,255,255,0.1);
  box-shadow: 0 8px 32px rgba(0,0,0,0.4);
}
.tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.tab {
  flex: 1;
  padding: 12px;
  background: none;
  border: none;
  color: rgba(255,255,255,0.5);
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border-bottom: 2px solid transparent;
}
.tab.active {
  color: #f0d9a0;
  border-bottom-color: #f0d9a0;
}
.tab:hover {
  color: rgba(255,255,255,0.8);
}
.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.input {
  --el-input-bg-color: rgba(255,255,255,0.05);
  --el-input-border-color: rgba(255,255,255,0.15);
  --el-input-text-color: #fff;
  --el-input-placeholder-color: rgba(255,255,255,0.35);
  --el-input-hover-border-color: #f0d9a0;
  --el-input-focus-border-color: #f0d9a0;
}
.input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(255,255,255,0.1) inset;
}
.input :deep(.el-input__inner) {
  color: #fff;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
  --el-button-bg-color: #c4a35a;
  --el-button-border-color: #c4a35a;
  --el-button-hover-bg-color: #d4b36a;
  --el-button-hover-border-color: #d4b36a;
  font-size: 16px;
  padding: 12px;
  height: auto;
}
.error {
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
}
</style>
