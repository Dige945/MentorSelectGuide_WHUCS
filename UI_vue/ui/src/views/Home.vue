<template>
  <div class="home-container">
    <!-- 顶部横幅 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>武汉大学计算机学院导师推荐系统</h1>
        <p>发现最适合您的研究导师，开启科研之旅</p>
      </div>
    </div>

    <!-- 搜索框部分 -->
    <div class="search-section">
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索导师姓名或研究方向"
          class="search-input"
          @input="handleSearch"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <!-- 搜索结果下拉框 -->
        <div v-if="searchResults.length > 0" class="search-results">
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
              @click="openTeacherProfile(teacher)"
            >
              查看主页
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计数据展示 -->
    <div class="stats-section">
      <div class="stat-card">
        <el-icon><User /></el-icon>
        <div class="stat-number">50+</div>
        <div class="stat-label">优秀导师</div>
      </div>
      <div class="stat-card">
        <el-icon><Collection /></el-icon>
        <div class="stat-number">10+</div>
        <div class="stat-label">研究方向</div>
      </div>
      <div class="stat-card">
        <el-icon><Medal /></el-icon>
        <div class="stat-number">100+</div>
        <div class="stat-label">成功案例</div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 热门导师推荐 -->
      <div class="section-container">
        <div class="section-header">
          <h2>热门导师</h2>
        </div>
        <HotTeachers />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search, User, Collection, Medal, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'
import HotTeachers from '@/components/HotTeachers.vue'
import ResearchAreas from '@/components/ResearchAreas.vue'

export default {
  name: 'Home',
  components: {
    HotTeachers,
    ResearchAreas,
    Search,
    User,
    Collection,
    Medal,
    ArrowRight
  },
  setup() {
    const searchKeyword = ref('')
    const searchResults = ref([])
    const hoveredTeacherId = ref(null)
    let searchTimer = null

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

    return {
      searchKeyword,
      searchResults,
      hoveredTeacherId,
      handleSearch,
      showButton,
      hideButton,
      openTeacherProfile
    }
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.hero-section {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  padding: 80px 20px;
  text-align: center;
  color: white;
  margin-bottom: 40px;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-content h1 {
  font-size: 2.5em;
  margin-bottom: 20px;
  font-weight: 600;
}

.hero-content p {
  font-size: 1.2em;
  opacity: 0.9;
}

.search-section {
  margin: -40px auto 40px;
  max-width: 600px;
  padding: 0 20px;
  position: relative;
  z-index: 10;
}

.search-container {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.search-input {
  width: 100%;
}

.search-results {
  margin-top: 10px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  transition: background-color 0.3s;
}

.search-result-item:last-child {
  border-bottom: none;
}

.search-result-item:hover {
  background-color: #f5f7fa;
}

.teacher-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.teacher-name {
  font-weight: 500;
  color: #333;
}

.teacher-title {
  font-size: 0.9em;
  color: #666;
}

.stats-section {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin: 40px auto;
  max-width: 1200px;
  padding: 0 20px;
}

.stat-card {
  background: white;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  flex: 1;
  max-width: 200px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card .el-icon {
  font-size: 2em;
  color: var(--el-color-primary);
  margin-bottom: 12px;
}

.stat-number {
  font-size: 1.8em;
  font-weight: 600;
  color: #333;
  margin: 8px 0;
}

.stat-label {
  color: #666;
  font-size: 1em;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.section-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 1.5em;
  color: #333;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-section {
    padding: 60px 20px;
  }

  .hero-content h1 {
    font-size: 2em;
  }

  .stats-section {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }

  .stat-card {
    width: 100%;
    max-width: none;
  }
}
</style> 

