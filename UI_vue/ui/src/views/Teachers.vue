<template>
  <div class="teachers-container">
    <h1 class="page-title">教师列表</h1>
    
    <div class="filter-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchQuery"
            placeholder="搜索教师名称"
            prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedDepartment" placeholder="选择学院" clearable style="width: 100%">
            <el-option
              v-for="item in departments"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="sortBy" placeholder="排序方式" style="width: 100%">
            <el-option label="综合评分从高到低" value="rating-desc" />
            <el-option label="综合评分从低到高" value="rating-asc" />
            <el-option label="评价数量从多到少" value="reviews-desc" />
            <el-option label="评价数量从少到多" value="reviews-asc" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-col>
      </el-row>
    </div>
    
    <div class="teachers-list">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="teacher in filteredTeachers" :key="teacher.id">
          <el-card shadow="hover" class="teacher-card">
            <div class="teacher-header">
              <el-avatar :size="80" :src="teacher.avatar"></el-avatar>
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
            </div>
            <div class="teacher-body">
              <h3>{{ teacher.name }}</h3>
              <p class="teacher-department">{{ teacher.department }}</p>
              <p class="teacher-title">{{ teacher.title }}</p>
              <div class="teacher-tags">
                <el-tag v-for="tag in teacher.tags" :key="tag" size="small" type="info" effect="plain" class="teacher-tag">
                  {{ tag }}
                </el-tag>
              </div>
              <div class="teacher-courses">
                <h4>主讲课程:</h4>
                <p>{{ teacher.courses.join('、') }}</p>
              </div>
            </div>
            <div class="teacher-footer">
              <el-button type="primary" size="small">查看详情</el-button>
              <el-button type="success" size="small">写评价</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalTeachers"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeachersView',
  data() {
    return {
      searchQuery: '',
      selectedDepartment: '',
      sortBy: 'rating-desc',
      currentPage: 1,
      pageSize: 12,
      totalTeachers: 100,
      departments: [
        { value: '计算机学院', label: '计算机学院' },
        { value: '数学与统计学院', label: '数学与统计学院' },
        { value: '物理学院', label: '物理学院' },
        { value: '电子信息学院', label: '电子信息学院' },
        { value: '经济管理学院', label: '经济管理学院' }
      ],
      teachers: [
        {
          id: 1,
          name: '张教授',
          department: '计算机学院',
          title: '教授',
          rating: 4.8,
          reviewCount: 256,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['教学认真', '有耐心', '授课生动'],
          courses: ['数据结构', '算法分析', '人工智能']
        },
        {
          id: 2,
          name: '李教授',
          department: '计算机学院',
          title: '教授',
          rating: 4.6,
          reviewCount: 189,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['思路清晰', '重点突出', '答疑及时'],
          courses: ['计算机网络', '信息安全', '密码学']
        },
        {
          id: 3,
          name: '王教授',
          department: '数学与统计学院',
          title: '教授',
          rating: 4.9,
          reviewCount: 320,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['优秀教师', '讲解透彻', '作业适中'],
          courses: ['高等数学', '线性代数', '概率论']
        },
        {
          id: 4,
          name: '刘教授',
          department: '计算机学院',
          title: '副教授',
          rating: 4.7,
          reviewCount: 175,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['专业知识扎实', '循循善诱', '板书工整'],
          courses: ['数据库系统', '大数据分析', '数据挖掘']
        },
        {
          id: 5,
          name: '陈教授',
          department: '物理学院',
          title: '教授',
          rating: 4.5,
          reviewCount: 142,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['实验指导详细', '理论联系实际', '幽默风趣'],
          courses: ['大学物理', '量子力学', '光学']
        },
        {
          id: 6,
          name: '赵教授',
          department: '电子信息学院',
          title: '教授',
          rating: 4.4,
          reviewCount: 162,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['实践经验丰富', '案例丰富', '关心学生'],
          courses: ['信号处理', '通信原理', '电路分析']
        },
        {
          id: 7,
          name: '孙教授',
          department: '经济管理学院',
          title: '教授',
          rating: 4.8,
          reviewCount: 198,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['思维活跃', '鼓励创新', '善于引导'],
          courses: ['经济学原理', '管理学', '市场营销']
        },
        {
          id: 8,
          name: '周教授',
          department: '计算机学院',
          title: '副教授',
          rating: 4.7,
          reviewCount: 156,
          avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
          tags: ['知识面广', '讲解细致', '耐心'],
          courses: ['操作系统', '编译原理', '计算机组成原理']
        }
      ]
    }
  },
  computed: {
    filteredTeachers() {
      let result = [...this.teachers]
      
      // 按学院筛选
      if (this.selectedDepartment) {
        result = result.filter(teacher => teacher.department === this.selectedDepartment)
      }
      
      // 搜索筛选
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        result = result.filter(teacher => 
          teacher.name.toLowerCase().includes(query) || 
          teacher.courses.some(course => course.toLowerCase().includes(query))
        )
      }
      
      // 排序
      if (this.sortBy === 'rating-desc') {
        result.sort((a, b) => b.rating - a.rating)
      } else if (this.sortBy === 'rating-asc') {
        result.sort((a, b) => a.rating - b.rating)
      } else if (this.sortBy === 'reviews-desc') {
        result.sort((a, b) => b.reviewCount - a.reviewCount)
      } else if (this.sortBy === 'reviews-asc') {
        result.sort((a, b) => a.reviewCount - b.reviewCount)
      }
      
      return result
    }
  },
  methods: {
    handleSearch() {
      // 在实际应用中，这里可能需要从后端获取数据
      console.log('Searching with:', {
        query: this.searchQuery,
        department: this.selectedDepartment,
        sortBy: this.sortBy
      })
    },
    resetFilters() {
      this.searchQuery = ''
      this.selectedDepartment = ''
      this.sortBy = 'rating-desc'
    },
    handleSizeChange(val) {
      this.pageSize = val
      // 在实际应用中，这里需要重新获取数据
      console.log('Page size changed to:', val)
    },
    handleCurrentChange(val) {
      this.currentPage = val
      // 在实际应用中，这里需要重新获取数据
      console.log('Current page changed to:', val)
    }
  }
}
</script>

<style scoped>
.teachers-container {
  padding: 20px 0;
}

.page-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #303133;
}

.filter-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.teachers-list {
  margin-top: 20px;
}

.teacher-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.teacher-card:hover {
  transform: translateY(-5px);
}

.teacher-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 15px;
}

.teacher-rating {
  margin-top: 10px;
  text-align: center;
}

.review-count {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  display: block;
}

.teacher-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.teacher-body h3 {
  text-align: center;
  margin: 0 0 10px;
  font-size: 18px;
}

.teacher-department, .teacher-title {
  text-align: center;
  color: #606266;
  margin: 0 0 5px;
}

.teacher-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  margin: 10px 0;
}

.teacher-tag {
  margin: 3px;
}

.teacher-courses {
  margin-top: 10px;
}

.teacher-courses h4 {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.teacher-courses p {
  color: #909399;
  font-size: 13px;
}

.teacher-footer {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style> 