import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import GameView from '../views/GameView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/game',
      name: 'game',
      component: GameView,
      beforeEnter: (_to, _from, next) => {
        const token = sessionStorage.getItem('token')
        if (!token) {
          next('/')
        } else {
          next()
        }
      }
    }
  ]
})

export default router
