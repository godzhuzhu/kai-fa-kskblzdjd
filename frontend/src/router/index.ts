import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

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
      component: () => import('../views/GameView.vue'),
      beforeEnter: (_to, _from, next) => {
        const token = sessionStorage.getItem('token')
        if (!token) {
          next('/')
          return
        }
        try {
          const payload = JSON.parse(atob(token.split('.')[1]))
          if (payload.exp * 1000 < Date.now()) {
            sessionStorage.removeItem('token')
            sessionStorage.removeItem('userId')
            next('/')
            return
          }
        } catch {
          next('/')
          return
        }
        next()
      }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
})

export default router
