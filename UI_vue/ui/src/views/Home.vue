<template>
  <div class="home-container">
    <!-- 动态背景 -->
    <div class="background-animation"></div>
    
    <!-- 顶部横幅 -->
    <div class="hero-section">
      <kinesis-container>
        <kinesis-element :strength="20" type="depth">
          <div class="hero-content" data-aos="fade-up">
            <h1>武汉大学计算机学院导师推荐系统</h1>
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
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { Search, User, Collection, Medal } from '@element-plus/icons-vue'
import { useElementSize } from '@vueuse/core'
import gsap from 'gsap'
import HotTeachers from '@/components/HotTeachers.vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export default {
  name: 'Home',
  components: {
    HotTeachers,
    Search,
    User,
    Collection,
    Medal
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

    const stats = ref([
      { icon: 'User', number: '50+', label: '优秀导师' },
      { icon: 'Collection', number: '10+', label: '研究方向' },
      { icon: 'Medal', number: '100+', label: '成功案例' }
    ])

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
    })

    return {
      searchKeyword,
      searchResults,
      hoveredTeacherId,
      handleSearch,
      showButton,
      hideButton,
      openTeacherProfile,
      stats
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
</style> 

