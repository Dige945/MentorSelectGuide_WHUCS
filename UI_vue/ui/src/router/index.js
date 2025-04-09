import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Teachers from '../views/Teachers.vue'
import TeacherDetail from '../views/TeacherDetail.vue'
import ResearchDirection from '../views/ResearchDirection.vue'
import AreaDetail from '../views/AreaDetail.vue'
import TaskDetail from '../views/TaskDetail.vue'
import NotFound from '../views/NotFound.vue'

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
    path: '/teachers/:id',
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
    path: '/area/:area',
    name: 'AreaDetail',
    component: AreaDetail,
    meta: {
      title: '研究领域详情'
    }
  },
  {
    path: '/tasks/:task',
    name: 'TaskDetail',
    component: TaskDetail,
    meta: {
      title: '研究方向详情'
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
    component: NotFound,
    meta: {
      title: '404 - 页面未找到'
    }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

// 设置页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '武汉大学教师评价系统'
  next()
})

export default router




