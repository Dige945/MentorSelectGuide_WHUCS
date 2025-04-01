<template>
  <div class="layout-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header>
        <div class="header-container">
          <div class="logo">
            <h1>ChooseYourMentor_WHUCS</h1>
          </div>
          <div class="nav-menu">
            <el-menu mode="horizontal" :ellipsis="false" router :default-active="activeMenu">
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/teachers">导师列表</el-menu-item>
              <el-menu-item index="/courses">科研方向</el-menu-item>
              <el-menu-item index="/rankings">csranking</el-menu-item>
              <el-menu-item index="/about">关于我们</el-menu-item>
            </el-menu>
          </div>
          <div class="user-actions">
            <el-button type="primary" plain>登录</el-button>
            <el-button type="success">注册</el-button>
          </div>
        </div>
      </el-header>
      
      <!-- 主要内容区域 -->
      <el-main>
        <slot></slot>
      </el-main>
      
      <!-- 底部栏 -->
      <el-footer>
        <div class="footer-container">
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
export default {
  name: 'AppLayout',
  computed: {
    activeMenu() {
      return this.$route.path
    }
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  padding: 0;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.logo h1 {
  color: #409EFF;
  font-size: 20px;
  margin: 0;
}

.nav-menu {
  flex: 1;
  margin: 0 20px;
}

.user-actions {
  display: flex;
  gap: 10px;
}

.el-main {
  padding: 20px 5%;
  max-width: 1400px;
  margin: 0 auto;
}

.el-footer {
  background-color: #303133;
  color: #fff;
  padding: 40px 20px 20px;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
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
  font-size: 18px;
}

.footer-section ul {
  list-style: none;
}

.footer-section li {
  margin-bottom: 8px;
}

.footer-section a {
  color: #c0c4cc;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-section a:hover {
  color: #409EFF;
}

.copyright {
  text-align: center;
  border-top: 1px solid #606266;
  padding-top: 20px;
  color: #909399;
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
}
</style> 