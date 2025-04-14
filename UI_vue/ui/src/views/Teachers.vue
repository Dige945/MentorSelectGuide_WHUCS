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
          <el-select v-model="selectedDepartment" placeholder="选择专业" clearable style="width: 100%">
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
  <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="teacher in paginatedTeachers" :key="teacher.id">
    <el-card shadow="hover" class="teacher-card">
      <div class="teacher-header">
        <el-avatar :size="80" :src="teacher.avatar || '默认头像URL'"></el-avatar>
      </div>
      <div class="teacher-body">
        <h3>{{ teacher.name || '未知教师' }}</h3>
        <p class="teacher-department">{{ teacher.department || '未知专业' }}</p>
        <p class="teacher-title">{{ teacher.title || '未知职称' }}</p>
        <p class="teacher-recommendations">推荐人数: {{ teacher.recommendations || 0 }}</p>
        <div class="teacher-research">
          <h4>主要研究方向:</h4>
          <p>{{ teacher.researchAreas || '暂无研究方向信息' }}</p>
        </div>
        <div class="teacher-tags">
          <el-tag v-for="tag in teacher.tags || []" :key="tag" size="small" type="info" effect="plain" class="teacher-tag">
            {{ tag }}
          </el-tag>
        </div>
      </div>
      <div class="teacher-footer">
      <el-button type="text" @click="viewTeacherDetail(teacher.id)">查看详情</el-button>
      <el-button type="primary" @click="showReviewDialog(teacher)">评价</el-button>
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
        :total="filteredTeachers.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="教师评价"
      width="50%"
    >
      <div class="review-form">
        <h3>{{ currentTeacher?.name }}</h3>
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="评分">
            <el-rate
              v-model="reviewForm.rating"
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              show-text
            />
            <span class="rating-text">{{ reviewForm.rating }}分</span>
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入您的评价内容"
            />
          </el-form-item>
          <el-form-item label="标签">
            <el-select
              v-model="reviewForm.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入标签"
            >
              <el-option
                v-for="tag in commonTags"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview">提交评价</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";

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
      departments: [],
      teachers: [],
      isCrawling: false,
      reviewDialogVisible: false,
      currentTeacher: null,
      reviewForm: {
        rating: 5,
        content: '',
        tags: []
      },
      commonTags: [
        '教学认真',
        '有耐心',
        '授课生动',
        '思路清晰',
        '重点突出',
        '答疑及时',
        '优秀教师',
        '讲解透彻',
        '作业适中',
        '专业知识扎实',
        '循循善诱',
        '板书工整'
      ]
    }
  },
  computed: {
    paginatedTeachers() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      const result = this.filteredTeachers.slice(start, end);
      
      console.log('Paginated Teachers:', result); // 调试日志
      return result;
    },
    filteredTeachers() {
      let result = [...this.teachers];
      
      // 按学院筛选
      if (this.selectedDepartment) {
        result = result.filter(teacher => teacher.department === this.selectedDepartment);
      }
      
      // 搜索筛选
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter(teacher => 
          teacher.name.toLowerCase().includes(query) || 
          teacher.courses.some(course => course.toLowerCase().includes(query))
        );
      }
      
      // 排序
      if (this.sortBy === 'rating-desc') {
        result.sort((a, b) => b.rating - a.rating);
      } else if (this.sortBy === 'rating-asc') {
        result.sort((a, b) => a.rating - b.rating);
      } else if (this.sortBy === 'reviews-desc') {
        result.sort((a, b) => b.reviewCount - a.reviewCount);
      } else if (this.sortBy === 'reviews-asc') {
        result.sort((a, b) => a.reviewCount - b.reviewCount);
      }
      
      console.log('Filtered Teachers:', result); // 调试日志
      return result;
    }
  },
  created() {
    this.fetchTeachers();
  },
  methods: {
    async fetchTeachers() {
      try {
        const response = await axios.get('/api/teachers/all');
        console.log('教师数据:', response.data); // 调试日志
        if (response.data.code === '0') {
          this.teachers = response.data.data.map(teacher => ({
            id: teacher.id,
            name: teacher.name,
            department: teacher.department,
            title: teacher.title,
            avatar: teacher.profileUrl || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
            tags: teacher.tags || [],
            recommendations: teacher.recommendcount || 0, // 推荐人数
            researchAreas: teacher.researchArea || []    // 主要研究方向
          }));
          console.log('教师数据:', this.teachers); // 调试日志
          // 动态生成院系筛选选项
          const departments = [...new Set(this.teachers.map(t => t.department))];
          this.departments = departments.map(d => ({ value: d, label: d }));

          this.totalTeachers = this.teachers.length;
        }
      } catch (error) {
        console.error('获取教师数据失败:', error);
        this.$message.error('数据加载失败');
      }
    },
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
    },
    viewTeacherDetail(teacherId) {
      this.$router.push(`/teacher/${teacherId}`);
    },
    showReviewDialog(teacher) {
      this.currentTeacher = teacher;
      this.reviewForm = {
        rating: 5,
        content: '',
        tags: []
      };
      this.reviewDialogVisible = true;
    },
    async submitReview() {
      if (!this.reviewForm.content.trim()) {
        this.$message.warning('请输入评价内容');
        return;
      }
      
      try {
        // 这里应该调用后端API保存评价
        console.log('提交评价:', {
          teacherId: this.currentTeacher.id,
          ...this.reviewForm
        });
        
        this.$message.success('评价提交成功！');
        this.reviewDialogVisible = false;
        
        // 更新教师评分和评价数量
        this.currentTeacher.rating = 
          (this.currentTeacher.rating * this.currentTeacher.reviewCount + this.reviewForm.rating) / 
          (this.currentTeacher.reviewCount + 1);
        this.currentTeacher.reviewCount += 1;
        this.currentTeacher.tags = [...new Set([...this.currentTeacher.tags, ...this.reviewForm.tags])];
      } catch (error) {
        this.$message.error('评价提交失败：' + error.message);
      }
    }
  }
}
</script>

<style scoped>
.el-col {
  display: flex;
  flex-direction: column; /* 让子元素按列排列 */
  height: 100%; /* 确保列的高度占满父容器 */
}

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
  height: 100%; /* 确保卡片高度占满父容器 */
  display: flex;
  flex-direction: column; /* 让内容和按钮按列排列 */
  justify-content: space-between; /* 确保内容和按钮之间有足够的空间 */
  background-color: #fff; /* 确保卡片背景为白色 */
  border-radius: 8px; /* 可选：为卡片添加圆角 */
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 可选：为卡片添加阴影 */
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
  flex: 1; /* 让内容区域占据剩余空间 */
  display: flex;
  flex-direction: column;
  justify-content: flex-start; /* 确保内容从顶部开始排列 */
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
  display: flex;
  justify-content: space-between;
  padding: 10px 15px; /* 添加内边距 */
  background-color: #fff; /* 确保按钮区域背景与卡片一致 */
  border-top: 1px solid #ebeef5; /* 添加顶部边框以区分内容和按钮 */
  margin-top: auto; /* 将按钮区域推到卡片底部 */
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.crawl-button {
  margin-left: 16px;
}

.review-form {
  padding: 20px;
}

.rating-text {
  margin-left: 10px;
  color: #ff9900;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.teacher-recommendations {
  text-align: center;
  color: #606266;
  margin: 5px 0;
}

.teacher-research {
  margin-top: 10px;
}

.teacher-research h4 {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.teacher-research p {
  color: #909399;
  font-size: 13px;
}
</style> 