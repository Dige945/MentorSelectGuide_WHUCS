<template>
  <div class="hot-teachers">
    <el-row :gutter="20" justify="center">
      <el-col :span="4" v-for="teacher in hotTeachers" :key="teacher.id">
        <el-card class="teacher-card" shadow="hover" @click="goToDetail(teacher)">
          <div class="teacher-avatar">
            <el-avatar :size="80" :src="teacher.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          </div>
          <div class="teacher-info">
            <h3>{{ teacher.name }}</h3>
            <p class="title">{{ teacher.title }}</p>
            <p class="department">{{ teacher.department }}</p>
            <div class="recommend-count">
              <el-icon><Star /></el-icon>
              <span>推荐人数：{{ teacher.recommendcount || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Star } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export default {
  name: 'HotTeachers',
  components: {
    Star
  },
  setup() {
    const router = useRouter()
    const hotTeachers = ref([])

    const fetchHotTeachers = async () => {
      try {
        const res = await request.get('/teachers/hot')
        if (res.code === '0') {
          hotTeachers.value = res.data
        }
      } catch (error) {
        console.error('获取热门导师失败:', error)
      }
    }

    const goToDetail = (teacher) => {
      if (teacher.profileUrl) {
        window.open(teacher.profileUrl, '_blank')
      } else {
        ElMessage.warning('该导师暂无个人主页')
      }
    }

    onMounted(() => {
      fetchHotTeachers()
    })

    return {
      hotTeachers,
      goToDetail
    }
  }
}
</script>

<style scoped>
.hot-teachers {
  margin: 20px 0;
  padding: 0 20px;
}

.section-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-size: 24px;
}

.teacher-card {
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
  text-align: center;
  width: 200px; /* 固定卡片宽度 */
  margin: 0 auto; /* 居中显示 */
}

.teacher-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.teacher-avatar {
  text-align: center;
  margin-bottom: 15px;
}

.teacher-info {
  text-align: center;
}

.teacher-info h3 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 18px;
}

.title {
  color: #409EFF;
  margin: 5px 0;
  font-size: 14px;
}

.department {
  color: #606266;
  margin: 5px 0;
  font-size: 14px;
}

.recommend-count {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #E6A23C;
  margin-top: 10px;
  font-size: 14px;
}

.recommend-count .el-icon {
  margin-right: 5px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .el-col {
    width: 33.33% !important;
  }
}

@media (max-width: 768px) {
  .el-col {
    width: 50% !important;
  }
}

@media (max-width: 480px) {
  .el-col {
    width: 100% !important;
  }
}
</style> 
