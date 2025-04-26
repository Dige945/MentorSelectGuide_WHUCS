<template>
  <div class="layout-container">
    <!-- 动态背景 -->
    <div class="background-animation"></div>

    <el-container>
      <!-- 顶部导航栏 -->
      <el-header>
        <div class="header-container glassmorphism">
          <div class="logo">
            <router-link to="/" class="logo-link">
              <h1>ChooseYourMentor_WHUCS</h1>
            </router-link>
          </div>
          <div class="nav-menu">
            <el-menu 
              mode="horizontal" 
              :ellipsis="false" 
              router 
              :default-active="activeMenu"
              class="transparent-menu"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/teachers">导师列表</el-menu-item>
              <el-menu-item index="/research">科研方向</el-menu-item>
              <el-menu-item index="/forum">论坛</el-menu-item>
              <el-menu-item index="/dataview">可视化</el-menu-item>
              <el-menu-item 
                v-if="hasNewsManagementPermission" 
                index="/news_management"
              >
                新闻管理
              </el-menu-item>
              <el-menu-item @click="handleAIRecommend">智能推荐</el-menu-item>
              <el-menu-item index="/about">关于我们</el-menu-item>
            </el-menu>
          </div>
          <div class="user-actions">
            <template v-if="!isLoggedIn">
              <el-button 
                type="primary" 
                class="gradient-button login-btn"
                @click="handleLogin"
              >
                登录
              </el-button>
              <el-button 
                type="success" 
                class="gradient-button register-btn"
                @click="handleRegister"
              >
                注册
              </el-button>
            </template>
            <template v-else>
              <el-dropdown @command="handleCommand">
                <span class="user-dropdown glassmorphism">
                  {{ currentUser.username }}
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu class="custom-dropdown">
                    <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                    <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </div>
        </div>
      </el-header>
      
      <!-- 主要内容区域 -->
      <el-main>
        <router-view></router-view>
      </el-main>
      
      <!-- 底部栏 -->
      <el-footer>
        <div class="footer-container glassmorphism">
          <div class="footer-links">
            <div class="footer-section">
              <h4>关于我们</h4>
              <ul>
                <li><a href="#">项目简介</a></li>
                <li><a href="#">联系我们</a></li>
                <li><a href="#">使用条款</a></li>
                <li><a href="#">隐私政策</a></li>
              </ul>
            </div>
            <div class="footer-section">
              <h4>快速导航</h4>
              <ul>
                <li><a href="#">首页</a></li>
                <li><a href="#">教师列表</a></li>
                <li><a href="#">课程列表</a></li>
                <li><a href="#">排行榜</a></li>
              </ul>
            </div>
            <div class="footer-section">
              <h4>帮助中心</h4>
              <ul>
                <li><a href="#">常见问题</a></li>
                <li><a href="#">评价指南</a></li>
                <li><a href="#">提交反馈</a></li>
              </ul>
            </div>
          </div>
          <div class="copyright">
            <p>&copy; 2023 武汉大学特软2班SETeam 版权所有</p>
          </div>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
import { ArrowDown } from '@element-plus/icons-vue'
import { mapState, mapGetters } from 'vuex'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

export default {
  name: 'AppLayout',
  components: {
    ArrowDown
  },
  setup() {
    const router = useRouter()
    const store = useStore()
    
    // 检查是否有新闻管理权限
    const hasNewsManagementPermission = computed(() => {
      const user = store.state.user
      if (!user) return false
      
      // 获取新闻管理路由的配置
      const newsManagementRoute = router.options.routes.find(route => route.name === 'NewsManagement')
      if (!newsManagementRoute || !newsManagementRoute.meta || !newsManagementRoute.meta.allowedRoles) {
        return false
      }
      
      // 检查用户是否在允许的角色列表中
      return newsManagementRoute.meta.allowedRoles.includes(user.username)
    })

    return {
      hasNewsManagementPermission
    }
  },
  computed: {
    ...mapState(['user']),
    ...mapGetters(['isLoggedIn', 'currentUser']),
    activeMenu() {
      return this.$route.path
    }
  },
  created() {
    // 组件创建时检查登录状态
    this.$store.dispatch('checkLogin')
  },
  methods: {
    handleLogin() {
      this.$router.push('/login')
    },
    handleRegister() {
      this.$router.push('/register')
    },
    handleCommand(command) {
      if (command === 'profile') {
        this.$router.push('/profile')
      } else if (command === 'logout') {
        this.$store.dispatch('logout')
        this.$router.push('/login')
      }
    },
    handleAIRecommend() {
      if (!this.isLoggedIn) {
        this.$message.warning('请先登录后再使用智能推荐功能')
        this.$router.push({
          path: '/login',
          query: { redirect: '/AIRecommend' }
        })
      } else {
        this.$router.push('/AIRecommend')
      }
    }
  }
}
</script>

<style scoped>
/* 动态背景 */
.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #fafafa;
  z-index: -1;
}

.background-animation::before {
  content: '';
  position: absolute;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(0,0,0,0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: backgroundMove 30s linear infinite;
}

@keyframes backgroundMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(-50%, -50%); }
}

.layout-container {
  min-height: 100vh;
  position: relative;
  background-color: #ffffff;
}

/* 毛玻璃效果改为简约白色 */
.glassmorphism {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.el-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 0;
  height: auto !important;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  border-radius: 0;
}

.logo-link {
  text-decoration: none;
}

.logo h1 {
  color: #333;
  font-size: 24px;
  margin: 0;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.nav-menu {
  flex: 1;
  margin: 0 40px;
}

/* 透明导航菜单 */
.transparent-menu {
  background: transparent;
  border: none;
}

.transparent-menu :deep(.el-menu-item) {
  color: #666;
  font-weight: 500;
  font-size: 16px;
  transition: all 0.3s;
  height: 50px;
  line-height: 50px;
}

.transparent-menu :deep(.el-menu-item)::before {
  display: none;
}

.transparent-menu :deep(.el-menu-item.is-active),
.transparent-menu :deep(.el-menu-item:hover) {
  color: #000;
  font-weight: 600;
  background: transparent;
}

.transparent-menu :deep(.el-menu-item):after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: #000;
  transition: all 0.3s;
}

.transparent-menu :deep(.el-menu-item:hover):after,
.transparent-menu :deep(.el-menu-item.is-active):after {
  left: 15%;
  width: 70%;
}

/* 用户操作按钮 */
.user-actions {
  display: flex;
  gap: 15px;
}

.gradient-button {
  background: #333;
  color: #fff;
  border: none;
  font-weight: 500;
  padding: 10px 20px;
  transition: all 0.3s;
  border-radius: 4px;
}

.gradient-button:hover {
  background: #000;
  transform: translateY(-2px);
}

.user-dropdown {
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s;
  color: #333;
}

.user-dropdown:hover {
  background: rgba(0, 0, 0, 0.05);
}

/* 主要内容区域 */
.el-main {
  padding: 20px 5%;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 60px - 300px);
}

/* 底部栏 */
.el-footer {
  background: transparent;
  padding: 40px 20px 20px;
  height: auto !important;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px;
  border-radius: 8px;
}

.footer-links {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-bottom: 30px;
}

.footer-section {
  margin-bottom: 20px;
  min-width: 200px;
}

.footer-section h4 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.footer-section ul {
  list-style: none;
  padding: 0;
}

.footer-section li {
  margin-bottom: 10px;
}

.footer-section a {
  color: #666;
  text-decoration: none;
  transition: all 0.3s;
}

.footer-section a:hover {
  color: #000;
}

.copyright {
  text-align: center;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  padding-top: 20px;
  color: #666;
}

/* 自定义下拉菜单 */
:deep(.custom-dropdown) {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.el-dropdown-menu__item) {
  color: #666;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(0, 0, 0, 0.05);
  color: #000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    padding: 10px;
  }
  
  .nav-menu {
    margin: 10px 0;
    width: 100%;
  }
  
  .footer-links {
    flex-direction: column;
  }
  
  .footer-section {
    width: 100%;
    text-align: center;
  }

  .transparent-menu :deep(.el-menu-item) {
    justify-content: center;
  }
}

/* 覆盖 Element Plus 默认的蓝色下划线 */
.transparent-menu :deep(.el-menu-item.is-active) {
  border-bottom: none !important;
}

.transparent-menu :deep(.el-menu--horizontal) {
  border-bottom: none;
}

/* 确保我们自定义的黑色下划线样式正确显示 */
.transparent-menu :deep(.el-menu-item.is-active):after {
  left: 15%;
  width: 70%;
  background: #000;
}
</style> 
