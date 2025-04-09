import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: '首页 - 武汉大学教师评价系统'
    }
  },
  {
    path: '/teachers',
    name: 'Teachers',
    // 使用懒加载，减少首屏加载时间
    component: () => import('../views/Teachers.vue'),
    meta: {
      title: '教师列表 - 武汉大学教师评价系统'
    }
  },
  {
    path: '/teacher/:id',
    name: 'TeacherDetail',
    component: () => import('../views/TeacherDetail.vue'),
    meta: {
      title: '教师详情 - 武汉大学教师评价系统'
    }
  },
  {
    path: '/rankings',
    name: 'Csranking',
    component: () => import('../views/Csranking.vue'),
    meta: {
      title: 'a'
    }
  },
  {
    path:'/deepseek',
    name: 'Deepseek',
    component: () => import('../views/DeepSeek.vue'),
    meta: {
      title: '你的专属寻找导师AI助手'
    }
  },
  // 404页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      title: '页面未找到 - 武汉大学教师评价系统'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由导航守卫，设置页面标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || '武汉大学教师评价系统'
  next()
})

export default router



