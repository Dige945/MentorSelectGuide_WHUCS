<template>
  <div class="teacher-detail">
    <!-- 教师基本信息卡片 -->
    <el-card class="teacher-info">
      <div class="teacher-header">
        <div class="avatar-container">
          <el-image 
            :src="teacher.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
            fit="cover"
            class="teacher-avatar"
          />
        </div>
        <div class="teacher-basic">
          <h1 class="teacher-name">{{ teacher.name }}</h1>
          <div class="info-item">
            <span class="label">职称：</span>
            <span class="value">{{ teacher.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">院系：</span>
            <span class="value">{{ teacher.department }}</span>
          </div>
          <div class="info-item">
            <span class="label">研究方向：</span>
            <span class="value">{{ teacher.researchArea }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 论文列表 -->
    <el-card class="paper-list">
      <template #header>
        <div class="paper-header">
          <span class="section-title">最近发表的20篇论文</span>
          <div class="paper-filters">
            <el-input
              v-model="searchQuery"
              placeholder="搜索论文"
              prefix-icon="Search"
              clearable
              style="width: 200px"
            />
            <el-select v-model="yearFilter" clearable placeholder="选择年份" style="margin-left: 10px">
              <el-option
                v-for="year in years"
                :key="year"
                :label="year"
                :value="year"
              />
            </el-select>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="filteredPapers"
        style="width: 100%"
      >
        <el-table-column prop="title" label="论文标题">
          <template #default="scope">
            <el-link type="primary" :href="scope.row.url" target="_blank">
              {{ scope.row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="200" />
        <el-table-column prop="year" label="年份" width="100" />
      </el-table>

      <div class="pagination-container" v-if="totalPapers > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalPapers"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评价卡片 -->
    <el-card class="evaluations-card">
      <template #header>
        <div class="card-header">
          <span>学生推荐理由</span>
        </div>
      </template>
      
      <div v-loading="evaluationsLoading" class="evaluations-list">
        <div v-if="evaluations.length === 0" class="empty-text">
          暂无评价
        </div>
        <div v-else v-for="evaluation in evaluations" :key="evaluation.id" class="evaluation-item">
          <div class="evaluation-content">
            <div class="evaluation-header">
              <span class="evaluation-time">{{ formatTime(evaluation.createdAt) }}</span>
            </div>
            <p class="evaluation-text">{{ evaluation.content }}</p>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export default {
  name: 'TeacherDetail',
  setup() {
    const route = useRoute()
    const teacher = ref({})
    const papers = ref([])
    const evaluations = ref([])
    const loading = ref(true)
    const evaluationsLoading = ref(false)
    const searchQuery = ref('')
    const yearFilter = ref('')
    const currentPage = ref(1)
    const pageSize = ref(4)
    const years = ref([])

    // 获取教师信息
    const fetchTeacherInfo = async () => {
      try {
        const response = await request.get(`/teachers/${route.params.id}`)
        if (response.code === '0') {
          teacher.value = response.data
          // 获取该教师的论文和评价
          fetchTeacherPapers()
          fetchTeacherEvaluations()
        } else {
          ElMessage.error(response.msg || '获取教师信息失败')
        }
      } catch (error) {
        console.error('获取教师信息失败:', error)
        ElMessage.error('获取教师信息失败')
      }
    }

    // 获取教师论文
    const fetchTeacherPapers = async () => {
      if (!teacher.value.name) {
        ElMessage.warning('教师信息不完整')
        return
      }
      
      try {
        loading.value = true
        const response = await request.get(`/paper/teacher/${teacher.value.name}`)
        if (response.code === '0') {
          papers.value = response.data
          // 提取所有年份并去重
          years.value = [...new Set(papers.value.map(paper => paper.year))].sort().reverse()
        } else {
          ElMessage.error(response.msg || '获取论文列表失败')
        }
      } catch (error) {
        console.error('获取论文列表失败:', error)
        ElMessage.error('获取论文列表失败')
      } finally {
        loading.value = false
      }
    }

    // 获取教师评价
    const fetchTeacherEvaluations = async () => {
      if (!teacher.value.id) {
        ElMessage.warning('教师信息不完整')
        return
      }
      
      try {
        evaluationsLoading.value = true
        const response = await request.get(`/evaluations/teacher/${teacher.value.id}`)
        if (response.code === '0') {
          evaluations.value = response.data
        } else {
          ElMessage.error(response.msg || '获取评价列表失败')
        }
      } catch (error) {
        console.error('获取评价列表失败:', error)
        ElMessage.error('获取评价列表失败')
      } finally {
        evaluationsLoading.value = false
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

    // 过滤论文
    const filteredPapers = computed(() => {
      let result = papers.value

      // 搜索过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        result = result.filter(paper => 
          paper.title.toLowerCase().includes(query) ||
          paper.author.toLowerCase().includes(query)
        )
      }

      // 年份过滤
      if (yearFilter.value) {
        result = result.filter(paper => paper.year === yearFilter.value)
      }

      return result
    })

    // 分页相关数据
    const totalPapers = computed(() => filteredPapers.value.length)
    const paginatedPapers = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return filteredPapers.value.slice(start, end)
    })

    // 分页处理方法
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
    }

    onMounted(() => {
      fetchTeacherInfo()
    })

    return {
      teacher,
      papers,
      evaluations,
      loading,
      evaluationsLoading,
      searchQuery,
      yearFilter,
      currentPage,
      pageSize,
      years,
      totalPapers,
      filteredPapers: paginatedPapers,
      fetchTeacherInfo,
      fetchTeacherPapers,
      fetchTeacherEvaluations,
      formatTime,
      handleSizeChange,
      handleCurrentChange
    }
  }
}
</script>

<style scoped>
.teacher-detail {
  padding: 20px;
  width: 1000px;
  margin: 0 auto;
}

.teacher-info {
  margin-bottom: 20px;
  background-color: #fff;
}

.teacher-header {
  display: flex;
  padding: 20px;
  gap: 30px;
}

.avatar-container {
  flex-shrink: 0;
}

.teacher-avatar {
  width: 180px;
  height: 240px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.teacher-basic {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.teacher-name {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #409EFF;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  font-size: 16px;
  line-height: 1.6;
}

.info-item .label {
  color: #606266;
  width: 100px;
  flex-shrink: 0;
}

.info-item .value {
  color: #303133;
  flex-grow: 1;
}

.paper-list {
  background-color: #fff;
  padding: 0 20px 20px 20px;
}

.paper-header {
  margin: 20px 0;
  padding: 0;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.paper-filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pagination-container {
  margin-top: 30px;
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

/* 表格样式优化 */
:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
  padding: 16px 0;
}

:deep(.el-table td) {
  padding: 20px 0;
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: #f5f7fa;
}

/* 卡片阴影效果 */
.teacher-info,
.paper-list {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s ease;
}

.teacher-info:hover,
.paper-list:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

/* 分页器样式优化 */
:deep(.el-pagination) {
  padding: 0;
  font-weight: normal;
}

:deep(.el-pagination .el-select .el-input) {
  width: 100px;
}

:deep(.el-pagination .el-pagination__sizes) {
  margin-right: 15px;
}

/* 论文标题链接样式 */
:deep(.el-link) {
  font-size: 15px;
  font-weight: 500;
}

:deep(.el-link:hover) {
  text-decoration: underline;
}

.evaluations-card {
  margin: 20px auto;
  width: 1000px;
  background-color: #fff;
  border: none;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.evaluations-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.evaluations-list {
  min-height: 200px;
  padding: 0;
  border-radius: 8px;
  overflow: hidden;
}

.evaluation-item {
  padding: 20px;
  border-bottom: 1px solid #f5f5f5;
  transition: all 0.3s ease;
  background-color: #fff;
}

.evaluation-item:last-child {
  border-bottom: none;
}

.evaluation-item:hover {
  background-color: #fafafa;
}

.evaluation-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.evaluation-time {
  font-size: 13px;
  color: #999;
  font-weight: 400;
}

.evaluation-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.empty-text {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 14px;
  font-weight: 400;
}
</style> 