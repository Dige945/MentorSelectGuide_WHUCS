import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Teachers from '../views/Teachers.vue'
import TeacherDetail from '../views/TeacherDetail.vue'
import ResearchDirection from '../views/ResearchDirection.vue'
import AreaDetail from '../views/AreaDetail.vue'
import TaskDetail from '../views/TaskDetail.vue'
import NotFound from '../views/NotFound.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Profile from '../views/Profile.vue'
import AIRecommend from '../views/AIRecommend.vue'
import store from '../store'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: '首页'
    }
  },
  {
    path: '/teachers',
    name: 'Teachers',
    component: Teachers,
    meta: {
      title: '教师列表'
    }
  },
  {
    path: '/teacher/:id',
    name: 'TeacherDetail',
    component: TeacherDetail,
    meta: {
      title: '教师详情'
    }
  },
  {
    path: '/research',
    name: 'ResearchDirection',
    component: ResearchDirection,
    meta: {
      title: '研究方向'
    }
  },
  {
    path: '/area/:id',
    name: 'AreaDetail',
    component: AreaDetail,
    meta: {
      title: '领域详情'
    }
  },
  {
    path: '/task-detail',
    name: 'TaskDetail',
    component: TaskDetail,
    meta: {
      title: '任务详情'
    }
  },
  {
    path: '/AIRecommend',
    name: 'AIRecommend',
    component: AIRecommend,
    meta: {
      title: 'AI推荐',
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '注册'
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: {
      title: '个人信息',
      requiresAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: '404'
    }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 设置页面标题
router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 武汉大学计算机学院导师选择指南` : '武汉大学计算机学院导师选择指南'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const isLoggedIn = await store.dispatch('checkLogin')
    if (!isLoggedIn) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }
  
  next()
})

export default router




