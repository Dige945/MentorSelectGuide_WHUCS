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
import About from '../views/About.vue'
import store from '../store'
import ForumView from '../views/Forum.vue'
import DataView from '../views/DataView.vue'
import PersonImage  from '../views/PersonImage.vue';
import { ElMessage } from 'element-plus'

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
    path: '/dataview',
    name: 'DateView',
    component: DataView,
    meta: {
      title: '可视化'
    }
  },
  {
    path: '/forum',
    name: 'ForumView',
    component: ForumView,
    meta: {
      title: '论坛'
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
    path: '/about',
    name: 'About',
    component: About,
    meta: {
      title: '关于我们'
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
    path: '/personimage',
    name: 'PersonImage',
    component: PersonImage,
    meta: {
      title: '个人画像',
      requiresAuth: true
    }
  },
  {
    path: '/news',
    name: 'News',
    component: () => import('@/views/News.vue')
  },
  {
    path: '/news_management',
    name: 'NewsManagement',
    component: () => import('@/views/NewsManagement.vue'),
    meta: {
      title: '新闻管理',
      requiresAuth: true,
      allowedRoles: ['ghy', 'admin']
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

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
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

    // 检查特定角色权限
    if (to.meta.allowedRoles) {
      const user = store.state.user
      if (!user || !to.meta.allowedRoles.includes(user.username)) {
        ElMessage.error('您没有权限访问此页面')
        next(from.path || '/')
        return
      }
    }
  }

  next()
})

export default router




