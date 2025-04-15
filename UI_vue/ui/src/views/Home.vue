<template>
  <div class="home-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <h2>找到最适合你的老师和课程</h2>
      <div class="search-box">
        <el-select v-model="searchType" placeholder="选择搜索类型" style="width: 120px; margin-right: 10px">
          <el-option label="教师" value="teacher" />
          <el-option label="研究方向" value="research" />
        </el-select>
        <el-input
          v-model="searchKeyword"
          :placeholder="searchType === 'teacher' ? '输入教师姓名' : '输入研究方向'"
          class="search-input"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="search-tags">
        <span>热门搜索：</span>
        <el-tag v-for="tag in hotTags" :key="tag" clickable @click="handleTagClick(tag)">{{ tag }}</el-tag>
      </div>
    </div>
    
    <!-- 热门教师推荐 -->
    <HotTeachers />
    
    <!-- 最新评价 -->
    <div class="reviews-section">
      <h3 class="section-title">最新评价</h3>
      <el-timeline>
        <el-timeline-item
          v-for="review in latestReviews"
          :key="review.id"
          :timestamp="review.date"
          placement="top"
        >
          <el-card>
            <h4>{{ review.teacherName }} - {{ review.courseName }}</h4>
            <p class="review-content">{{ review.content }}</p>
            <div class="review-rating">
              <span>评分：</span>
              <el-rate
                v-model="review.rating"
                disabled
              ></el-rate>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
import HotTeachers from '@/components/HotTeachers.vue'

export default {
  name: 'HomePage',
  components: {
    Search,
    HotTeachers
  },
  data() {
    return {
      searchKeyword: '',
      searchType: 'teacher',
      hotTags: ['计算机视觉', '自然语言处理', '机器学习', '人工智能', '数据挖掘'],
      latestReviews: [
        {
          id: 1,
          teacherName: '杜博',
          courseName: '人工智能导论',
          content: '老师讲课非常生动，能够将复杂的理论讲解得通俗易懂。',
          rating: 5,
          date: '2024-03-15'
        },
        {
          id: 2,
          teacherName: '李教授',
          courseName: '操作系统',
          content: '课程内容充实，实验指导详细，收获很大。',
          rating: 4.5,
          date: '2024-03-14'
        },
        {
          id: 3,
          teacherName: '王教授',
          courseName: '软件工程',
          content: '理论与实践结合得很好，项目经验分享很有价值。',
          rating: 4.8,
          date: '2024-03-13'
        }
      ]
    }
  },
  methods: {
    handleSearch() {
      if (!this.searchKeyword.trim()) {
        this.$message.warning('请输入搜索关键词')
        return
      }
      
      if (this.searchType === 'teacher') {
        // 跳转到教师列表页面，并传递搜索参数
        this.$router.push({
          path: '/teachers',
          query: { search: this.searchKeyword }
        })
      } else {
        // 跳转到研究方向页面，并传递搜索参数
        this.$router.push({
          path: '/research',
          query: { search: this.searchKeyword }
        })
      }
    },
    handleTagClick(tag) {
      this.searchType = 'research'
      this.searchKeyword = tag
      this.handleSearch()
    }
  }
}
</script>

<style scoped>
.home-container {
  width: 100%;
}

.search-section {
  text-align: center;
  padding: 40px 0;
  background: linear-gradient(135deg, #409EFF 0%, #42b983 100%);
  border-radius: 8px;
  color: white;
  margin-bottom: 40px;
}

.search-section h2 {
  margin-bottom: 20px;
  font-size: 28px;
}

.search-box {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.search-input {
  width: 500px;
}

.search-tags {
  margin-top: 20px;
}

.search-tags .el-tag {
  margin: 0 5px;
  cursor: pointer;
}

.section-title {
  text-align: center;
  margin: 40px 0;
  color: #303133;
  font-size: 24px;
}

.reviews-section {
  margin-top: 40px;
}

.review-content {
  margin: 10px 0;
  color: #606266;
}

.review-rating {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.review-rating span {
  margin-right: 10px;
  color: #606266;
}
</style> 