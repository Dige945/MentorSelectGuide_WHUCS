<template>
  <div class="home-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <h2>找到最适合你的老师和课程</h2>
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="输入教师姓名、课程名称或关键词"
          class="search-input"
        >
          <template #append>
            <el-button type="primary">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="search-tags">
        <span>热门搜索：</span>
        <el-tag v-for="tag in hotTags" :key="tag" clickable>{{ tag }}</el-tag>
      </div>
    </div>
    
    <!-- 热门教师推荐 -->
    <div class="recommend-section">
      <h3 class="section-title">热门教师推荐</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="teacher in popularTeachers" :key="teacher.id">
          <el-card shadow="hover" class="teacher-card">
            <div class="teacher-avatar">
              <el-avatar :size="80" :src="teacher.avatar"></el-avatar>
            </div>
            <div class="teacher-info">
              <h4>{{ teacher.name }}</h4>
              <p>{{ teacher.department }}</p>
              <div class="teacher-rating">
                <el-rate
                  v-model="teacher.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                ></el-rate>
              </div>
              <p class="teacher-specialty">{{ teacher.specialty }}</p>
              <el-button type="primary" plain size="small">查看详情</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
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

export default {
  name: 'HomePage',
  components: {
    Search
  },
  data() {
    return {
      searchKeyword: '',
      hotTags: ['计算机科学', '数据结构', '操作系统', '编译原理', '人工智能'],
      popularTeachers: [
        {
          id: 1,
          name: '杜博',
          department: '人工智能系',
          rating: 4.8,
          specialty: '人工智能、机器学习',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        },
        {
          id: 2,
          name: '李教授',
          department: '计算机科学系',
          rating: 4.7,
          specialty: '操作系统、分布式系统',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        },
        {
          id: 3,
          name: '王教授',
          department: '软件工程系',
          rating: 4.9,
          specialty: '软件工程、项目管理',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        },
        {
          id: 4,
          name: '张教授',
          department: '信息安全系',
          rating: 4.6,
          specialty: '网络安全、密码学',
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
        }
      ],
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
  margin-bottom: 30px;
}

.search-section h2 {
  margin-bottom: 20px;
  font-size: 28px;
}

.search-box {
  max-width: 600px;
  margin: 0 auto 20px;
}

.search-input {
  width: 100%;
}

.search-tags {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.section-title {
  margin: 30px 0 20px;
  font-size: 22px;
  color: #303133;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background-color: #409EFF;
  border-radius: 2px;
}

.teacher-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.teacher-card:hover {
  transform: translateY(-5px);
}

.teacher-avatar {
  text-align: center;
  margin-bottom: 15px;
}

.teacher-info {
  text-align: center;
}

.teacher-info h4 {
  margin: 10px 0;
  font-size: 18px;
}

.teacher-info p {
  color: #606266;
  margin: 5px 0;
}

.teacher-rating {
  margin: 10px 0;
}

.teacher-specialty {
  color: #909399;
  font-size: 14px;
  margin-bottom: 15px;
  height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .el-col {
    width: 50% !important;
  }
}

@media (max-width: 768px) {
  .el-col {
    width: 100% !important;
  }
}
</style> 