<template>
  <div class="teacher-detail">
    <div class="teacher-header">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-avatar :size="120" :src="teacher.avatar"></el-avatar>
        </el-col>
        <el-col :span="18">
          <h1>{{ teacher.name }}</h1>
          <p class="teacher-title">{{ teacher.title }}</p>
          <p class="teacher-department">{{ teacher.department }}</p>
          <div class="teacher-rating">
            <el-rate
              v-model="teacher.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
            <span class="review-count">{{ teacher.reviewCount }}条评价</span>
          </div>
          <div class="teacher-tags">
            <el-tag v-for="tag in teacher.tags" :key="tag" size="small" type="info" effect="plain">
              {{ tag }}
            </el-tag>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="teacher-content">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="教师简介" name="profile">
          <div class="profile-section">
            <h2>个人简介</h2>
            <p>{{ teacher.profile || '暂无简介' }}</p>
            
            <h2>研究方向</h2>
            <ul>
              <li v-for="(area, index) in teacher.researchAreas" :key="index">
                {{ area }}
              </li>
            </ul>
            
            <h2>主讲课程</h2>
            <ul>
              <li v-for="(course, index) in teacher.courses" :key="index">
                {{ course }}
              </li>
            </ul>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="学生评价" name="reviews">
          <div class="reviews-section">
            <div v-for="review in teacher.reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <el-avatar :size="40" :src="review.studentAvatar"></el-avatar>
                <div class="review-info">
                  <span class="student-name">{{ review.studentName }}</span>
                  <span class="review-time">{{ review.time }}</span>
                </div>
              </div>
              <div class="review-content">
                <el-rate
                  v-model="review.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
                <p class="review-text">{{ review.content }}</p>
                <div class="review-tags">
                  <el-tag v-for="tag in review.tags" :key="tag" size="small" type="info" effect="plain">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeacherDetailView',
  data() {
    return {
      activeTab: 'profile',
      teacher: {
        id: 1,
        name: '张教授',
        department: '计算机学院',
        title: '教授',
        rating: 4.8,
        reviewCount: 256,
        avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
        tags: ['教学认真', '有耐心', '授课生动'],
        courses: ['数据结构', '算法分析', '人工智能'],
        profile: '张教授现任武汉大学计算机学院教授，博士生导师。主要从事人工智能、机器学习等领域的研究。在国内外重要期刊发表论文30余篇，主持多项国家级科研项目。',
        researchAreas: [
          '人工智能',
          '机器学习',
          '深度学习',
          '计算机视觉'
        ],
        reviews: [
          {
            id: 1,
            studentName: '李同学',
            studentAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
            rating: 5,
            content: '张教授的课程非常生动有趣，讲解深入浅出，让我对人工智能产生了浓厚的兴趣。',
            tags: ['教学认真', '授课生动'],
            time: '2024-03-15'
          },
          {
            id: 2,
            studentName: '王同学',
            studentAvatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
            rating: 4.5,
            content: '课程内容充实，作业难度适中，老师答疑很及时。',
            tags: ['重点突出', '答疑及时'],
            time: '2024-03-10'
          }
        ]
      }
    }
  },
  created() {
    // 从路由参数获取教师ID
    const teacherId = this.$route.params.id;
    // 这里应该调用API获取教师详细信息
    console.log('获取教师ID:', teacherId);
  }
}
</script>

<style scoped>
.teacher-detail {
  padding: 20px;
}

.teacher-header {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.teacher-header h1 {
  margin: 0 0 10px;
  font-size: 28px;
  color: #303133;
}

.teacher-title, .teacher-department {
  color: #606266;
  margin: 5px 0;
}

.teacher-rating {
  margin: 15px 0;
}

.review-count {
  margin-left: 10px;
  color: #909399;
}

.teacher-tags {
  margin-top: 15px;
}

.teacher-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.profile-section h2 {
  color: #303133;
  margin: 20px 0 10px;
}

.profile-section p, .profile-section ul {
  color: #606266;
  line-height: 1.6;
}

.profile-section ul {
  padding-left: 20px;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #EBEEF5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.review-info {
  margin-left: 10px;
}

.student-name {
  font-weight: bold;
  color: #303133;
}

.review-time {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}

.review-content {
  margin-top: 10px;
}

.review-text {
  color: #606266;
  line-height: 1.6;
  margin: 10px 0;
}

.review-tags {
  margin-top: 10px;
}
</style> 