<template>
  <div class="home-container">
    <!-- 动态背景 -->
    <div class="background-animation"></div>
    
    <!-- 顶部横幅 -->
    <div class="hero-section">
      <kinesis-container>
        <kinesis-element :strength="20" type="depth">
          <div class="hero-content" data-aos="fade-up">
            <h1>基于大语言模型Agent的导师推荐系统</h1>
            <p class="gradient-text">发现最适合您的研究导师，开启科研之旅</p>
          </div>
        </kinesis-element>
      </kinesis-container>
    </div>

    <!-- 搜索框部分 -->
    <div class="search-section" data-aos="fade-up" data-aos-delay="200">
      <div class="search-container glassmorphism">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索导师姓名或研究方向"
          class="search-input"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon class="search-icon"><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 搜索结果下拉框 -->
        <div v-if="searchResults.length > 0" class="search-results glassmorphism">
          <div 
            v-for="teacher in searchResults" 
            :key="teacher.id" 
            class="search-result-item"
            @mouseenter="showButton(teacher.id)"
            @mouseleave="hideButton(teacher.id)"
          >
            <div class="teacher-info">
              <span class="teacher-name">{{ teacher.name }}</span>
              <span class="teacher-title">{{ teacher.title }}</span>
            </div>
            <el-button 
              v-if="hoveredTeacherId === teacher.id"
              type="primary" 
              size="small"
              class="view-button"
              @click="openTeacherProfile(teacher)"
            >
              查看主页
            </el-button>
          </div>
        </div>
      </div>
    </div>

<!--    &lt;!&ndash; 统计数据展示 &ndash;&gt;-->
<!--    <div class="stats-section" data-aos="fade-up" data-aos-delay="400">-->
<!--      <div class="stat-card glassmorphism" v-for="(stat, index) in stats" :key="index">-->
<!--        <el-icon class="stat-icon"><component :is="stat.icon" /></el-icon>-->
<!--        <div class="stat-number">{{ stat.number }}</div>-->
<!--        <div class="stat-label">{{ stat.label }}</div>-->
<!--      </div>-->
<!--    </div>-->

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 热门导师推荐 -->
      <div class="section-container">
        <div class="section-header">
          <h2 class="gradient-text">热门导师</h2>
        </div>
        <HotTeachers />
      </div>

      <!-- 新闻展示 -->
      <div class="section-container news-section">
        <div class="section-header">
          <h2 class="gradient-text">最新动态</h2>
        </div>
        <el-card class="news-card" :body-style="{ padding: '0' }">
          <News />
        </el-card>
      </div>

      <!-- 最近评论卡片 -->
      <div class="section-container reviews-section">
        <div class="section-header">
          <h2 class="gradient-text">最近评论</h2>
        </div>
        <el-card class="recent-reviews" :body-style="{ padding: '0' }">
          <div v-loading="loading" class="review-list">
            <div v-if="recentReviews.length === 0" class="empty-text">
              暂无评论
            </div>
            <div v-else v-for="review in recentReviews" :key="review.id" class="review-item">
              <div class="review-content">
                <div class="review-header">
                  <div class="review-header-left">
                    <span class="teacher-avatar">{{ review.teacherName.charAt(0) }}</span>
                    <span class="teacher-name">{{ review.teacherName }}</span>
                  </div>
                  <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                </div>
                <p class="review-text">{{ review.content }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 管理新闻按钮
      <div class="admin-actions" v-if="hasNewsManagementPermission">
        <el-button type="primary" @click="goToNewsManagement">
          <el-icon><Edit /></el-icon>
          管理新闻
        </el-button>
      </div> -->
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { Search, User, Collection, Medal, Edit } from '@element-plus/icons-vue'
import { useElementSize } from '@vueuse/core'
import gsap from 'gsap'
import HotTeachers from '@/components/HotTeachers.vue'
import News from '@/views/News.vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'Home',
  components: {
    HotTeachers,
    News,
    Search,
    User,
    Collection,
    Medal,
    Edit
  },
  setup() {
    const searchKeyword = ref('')
    const searchResults = ref([])
    const hoveredTeacherId = ref(null)
    let searchTimer = null
    const recentReviews = ref([])
    const loading = ref(false)
    const store = useStore()
    const router = useRouter()

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

    const handleSearch = () => {
      if (searchTimer) {
        clearTimeout(searchTimer)
      }
      
      if (!searchKeyword.value) {
        searchResults.value = []
        return
      }

      searchTimer = setTimeout(async () => {
        try {
          const res = await request.get(`/teachers/search?keyword=${searchKeyword.value}`)
          if (res.code === '0') {
            searchResults.value = res.data
          }
        } catch (error) {
          console.error('搜索失败:', error)
        }
      }, 300)
    }

    const showButton = (teacherId) => {
      hoveredTeacherId.value = teacherId
    }

    const hideButton = () => {
      hoveredTeacherId.value = null
    }

    const openTeacherProfile = (teacher) => {
      if (teacher.profileUrl) {
        window.open(teacher.profileUrl, '_blank')
      } else {
        ElMessage.warning('该导师暂无个人主页')
      }
    }

    const stats = ref([
      { icon: 'User', number: '50+', label: '优秀导师' },
      { icon: 'Collection', number: '10+', label: '研究方向' },
      { icon: 'Medal', number: '100+', label: '成功案例' }
    ])

    // 获取最近评论
    const fetchRecentReviews = async () => {
      try {
        loading.value = true
        const response = await request.get('/evaluations/recent')
        if (response.code === '0') {
          recentReviews.value = response.data
        } else {
          ElMessage.error(response.msg || '获取评论失败')
        }
      } catch (error) {
        console.error('获取评论失败:', error)
        ElMessage.error('获取评论失败')
      } finally {
        loading.value = false
      }
    }

    // 格式化时间
    const formatTime = (timestamp) => {
      try {
        if (!timestamp) return ''
        
        // 如果是简化的日期格式（如 25-04-18）
        if (typeof timestamp === 'string' && /^\d{2}-\d{2}-\d{2}$/.test(timestamp)) {
          const [day, month, year] = timestamp.split('-')
          // 年份是 25 表示 2025 年
          return `2025-${month}-${day}`
        }
        
        // 如果是字符串，尝试解析
        if (typeof timestamp === 'string') {
          // 处理可能的时间戳字符串格式
          if (timestamp.includes('T')) {
            timestamp = new Date(timestamp)
          } else {
            // 尝试解析数字时间戳
            const numTimestamp = Number(timestamp)
            if (!isNaN(numTimestamp)) {
              timestamp = new Date(numTimestamp)
            } else {
              timestamp = new Date(timestamp)
            }
          }
        }
        
        // 如果是数字，可能是时间戳
        if (typeof timestamp === 'number') {
          timestamp = new Date(timestamp)
        }
        
        // 确保是有效的日期对象
        if (!(timestamp instanceof Date) || isNaN(timestamp)) {
          return ''
        }
        
        const year = timestamp.getFullYear()
        const month = String(timestamp.getMonth() + 1).padStart(2, '0')
        const day = String(timestamp.getDate()).padStart(2, '0')
        const hours = String(timestamp.getHours()).padStart(2, '0')
        const minutes = String(timestamp.getMinutes()).padStart(2, '0')
        
        return `${year}-${month}-${day} ${hours}:${minutes}`
      } catch (error) {
        return ''
      }
    }

    // 跳转到新闻管理页面
    const goToNewsManagement = () => {
      router.push('/news_management')
    }

    onMounted(() => {
      // 添加数字增长动画
      stats.value.forEach((stat, index) => {
        const num = parseInt(stat.number)
        gsap.from(`stat-number-${index}`, {
          textContent: 0,
          duration: 2,
          ease: "power1.out",
          snap: { textContent: 1 },
          stagger: {
            each: 0.2,
            onUpdate: function() {
              this.targets()[0].innerHTML = Math.ceil(this.targets()[0].textContent) + '+'
            },
          }
        })
      })

      fetchRecentReviews()
    })

    return {
      searchKeyword,
      searchResults,
      hoveredTeacherId,
      handleSearch,
      showButton,
      hideButton,
      openTeacherProfile,
      stats,
      recentReviews,
      loading,
      formatTime,
      hasNewsManagementPermission,
      goToNewsManagement
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #ffffff;
  position: relative;
  overflow: hidden;
}

.background-animation {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #fafafa;
  z-index: 0;
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

.glassmorphism {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.hero-section {
  background: transparent;
  padding: 100px 20px;
  text-align: center;
  color: #333;
  position: relative;
  z-index: 1;
}

.gradient-text {
  color: #666;
  font-weight: 500;
}

.hero-content h1 {
  font-size: 3em;
  margin-bottom: 20px;
  font-weight: 700;
  color: #333;
}

.search-section {
  margin: -40px auto 40px;
  max-width: 600px;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

.search-container {
  position: relative;
  padding: 20px;
  border-radius: 15px;
  background: white;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  background: #fff;
  box-shadow: none !important;
  border: 1px solid #eee;
}

.search-input :deep(.el-input__inner) {
  color: #333;
  font-size: 16px;
  height: 50px;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: #999;
}

.search-icon {
  color: #666;
  font-size: 20px;
}

.search-results {
  position: absolute;
  top: calc(100% + 10px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 10px;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
}

.search-result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  transition: all 0.3s ease;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background: #f9f9f9;
}

.teacher-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.teacher-name {
  color: #333;
  font-size: 16px;
  font-weight: 500;
}

.teacher-title {
  color: #666;
  font-size: 14px;
}

.view-button {
  background: #333;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 5px;
  transition: all 0.3s ease;
}

.view-button:hover {
  background: #000;
  transform: translateY(-2px);
}

/* 自定义滚动条样式 */
.search-results::-webkit-scrollbar {
  width: 6px;
}

.search-results::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.search-results::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.search-results::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

/* 主要内容区域样式 */
.main-content {
  padding: 40px 0;
  position: relative;
  z-index: 1;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
}

.section-header h2 {
  font-size: 2.5em;
  margin: 0;
  padding: 0;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content h1 {
    font-size: 2em;
  }
}

/* 添加一些微动画 */
@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

.search-container {
  animation: float 6s ease-in-out infinite;
}

/* 添加鼠标悬停效果 */
.view-button {
  background: #333;
  border: none;
  transition: transform 0.3s ease, background 0.3s ease;

  &:hover {
    transform: scale(1.05);
    background: #000;
  }
}

.reviews-section {
  margin-top: 80px;
}

.recent-reviews {
  margin: 0 auto;
  max-width: 800px;
  background-color: #fff;
  border: none;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.recent-reviews:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.review-list {
  min-height: 200px;
  padding: 0;
  border-radius: 8px;
  overflow: hidden;
}

.review-item {
  padding: 20px;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s ease;
  background-color: #fff;
}

.review-item:last-child {
  border-bottom: none;
}

.review-item:hover {
  background-color: #fafafa;
  transform: translateX(0);
}

.review-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.review-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.teacher-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #000;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 500;
}

.teacher-name {
  font-size: 15px;
  font-weight: 500;
  color: #000;
}

.review-time {
  font-size: 13px;
  color: #999;
  font-weight: 400;
}

.review-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  padding-left: 48px;
  position: relative;
}

.empty-text {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
  font-weight: 400;
}

.news-section {
  margin-top: 40px;
}

.news-card {
  margin: 0 auto;
  max-width: 800px;
  background-color: #fff;
  border: none;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.news-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.admin-actions {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.admin-actions .el-button {
  padding: 12px 20px;
  font-size: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.admin-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}
</style> 

