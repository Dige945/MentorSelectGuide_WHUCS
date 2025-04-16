<template>
  <div class="teachers-container">
    <h1 class="page-title">教师列表</h1>
    
    <div class="filter-container" style="width: 1250px;">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="search-box">
            <el-autocomplete
              v-model="searchQuery"
              :fetch-suggestions="querySearchAsync"
              placeholder="搜索教师名称"
              clearable
              class="search-input"
              @select="handleSelect"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #default="{ item }">
                <div class="suggestion-item">
                  <span>{{ item.name }}</span>
                  <span class="suggestion-detail">{{ item.title }} | {{ item.department }}</span>
                </div>
              </template>
            </el-autocomplete>
            <el-button type="primary" @click="handleSearch" class="search-button">
              搜索
            </el-button>
          </div>
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
            <el-option label="推荐人数从多到少" value="recommendcount-desc" />
            <el-option label="推荐人数从少到多" value="recommendcount-asc" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button @click="resetFilters">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索结果为空时的提示 -->
    <el-empty
      v-if="filteredTeachers.length === 0"
      description="未找到相关导师"
    />
    
    <div class="teachers-list">
      <el-row :gutter="20">
  <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="teacher in paginatedTeachers" :key="teacher.id">
    <el-card shadow="hover" class="teacher-card">
      <div class="teacher-header">
        <el-avatar 
          :size="[120, 160]" 
          :src="getProcessedAvatarUrl(teacher.avatar)"
          @error="handleAvatarError"
          class="teacher-avatar"
        ></el-avatar>
      </div>
      <div class="teacher-body">
        <h3>{{ teacher.name || '未知教师' }}</h3>
        <p class="teacher-department">{{ teacher.department || '未知专业' }}</p>
        <p class="teacher-title">{{ teacher.title || '未知职称' }}</p>
        <p class="teacher-recommendcount">推荐人数: {{ teacher.recommendcount || 0 }}</p>
        <div class="teacher-research">
          <h4>主要研究方向:</h4>
          <p>{{ teacher.researchArea || '暂无研究方向信息' }}</p>
        </div>
        <div class="teacher-tags">
          <el-tag v-for="tag in teacher.tags || []" :key="tag" size="small" type="info" effect="plain" class="teacher-tag">
            {{ tag }}
          </el-tag>
        </div>
      </div>
      <div class="teacher-footer">
      <el-button type="link" @click="viewTeacherDetail(teacher.id)">查看详情</el-button>
      <el-button type="primary" @click="showReviewDialog(teacher)">写推荐</el-button>
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
          <el-form-item label="推荐内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入您的推荐内容"
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
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'TeachersView',
  components: {
    Search
  },
  data() {
    return {
      searchQuery: this.$route.query.search || '',
      selectedDepartment: '',
      sortBy: 'recommendcount-desc', // 默认按推荐人数从多到少排序
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
      ],
      searchTimeout: null,
      isExactSearch: false, // 用于标记是否为精确搜索
      imageCache: new Map(), // 用于缓存处理过的图片URL
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
      
      if (this.isExactSearch && this.searchQuery) {
        // 精确搜索模式
        result = result.filter(teacher => 
          teacher.name.toLowerCase() === this.searchQuery.toLowerCase()
        );
      } else {
        // 普通筛选模式
        if (this.searchQuery) {
          const query = this.searchQuery.toLowerCase();
          result = result.filter(teacher => 
            teacher.name.toLowerCase().includes(query) ||
            (teacher.department && teacher.department.toLowerCase().includes(query)) ||
            (teacher.researchArea && teacher.researchArea.toLowerCase().includes(query))
          );
        }
        
        if (this.selectedDepartment) {
          result = result.filter(teacher => teacher.department === this.selectedDepartment);
        }
      }
      
      // 排序
      if (this.sortBy === 'recommendcount-desc') {
        result.sort((a, b) => (b.recommendcount || 0) - (a.recommendcount || 0));
      } else if (this.sortBy === 'recommendcount-asc') {
        result.sort((a, b) => (a.recommendcount || 0) - (b.recommendcount || 0));
      }
      
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
        if (response.data.code === '0') {
          this.teachers = response.data.data.map(teacher => ({
            id: teacher.id,
            name: teacher.name,
            department: teacher.department,
            title: teacher.title,
            avatar: teacher.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
            tags: teacher.tags || [],
            recommendcount: teacher.recommendcount || 0,
            researchArea: teacher.researchArea || '暂无研究方向信息'
          }));
          
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
    querySearchAsync(queryString, cb) {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }
      
      this.searchTimeout = setTimeout(() => {
        const results = queryString
          ? this.teachers.filter(teacher => 
              teacher.name.toLowerCase().includes(queryString.toLowerCase())
            )
          : [];
        
        // 将结果格式化为建议列表格式
        const suggestions = results.map(teacher => ({
          value: teacher.name,
          ...teacher
        }));
        
        cb(suggestions);
      }, 300);
    },
    
    handleSelect(item) {
      this.searchQuery = item.name;
      this.isExactSearch = true;
      this.handleSearch();
    },
    
    handleSearch() {
      this.isExactSearch = true;
      this.currentPage = 1;
      
      if (!this.searchQuery) {
        ElMessage.warning('请输入要搜索的教师姓名');
        return;
      }
      
      const results = this.filteredTeachers;
      if (results.length === 0) {
        ElMessage.warning('未找到该导师');
      }
    },
    
    resetFilters() {
      this.searchQuery = '';
      this.selectedDepartment = '';
      this.sortBy = 'recommendcount-desc';
      this.isExactSearch = false;
      this.currentPage = 1;
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
    },
    getProcessedAvatarUrl(url) {
      if (!url) {
        console.log('No URL provided, using default avatar');
        return 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
      }

      // 如果已经处理过这个URL，直接返回缓存的结果
      if (this.imageCache.has(url)) {
        return this.imageCache.get(url);
      }

      // 创建一个新的Image对象来加载图片
      const img = new Image();
      
      // 处理跨域问题
      img.crossOrigin = 'anonymous';
      
      img.onload = () => {
        console.log('Image loaded successfully:', url);
        console.log('Image dimensions:', img.width, 'x', img.height);
        
        const aspectRatio = img.width / img.height;
        console.log('Aspect ratio:', aspectRatio);
        
        try {
          // 如果是长方形图片（高度明显大于宽度）
          if (aspectRatio < 0.8) { // 可以调整这个阈值
            console.log('Processing rectangular image');
            // 创建canvas来处理图片
            const canvas = document.createElement('canvas');
            const ctx = canvas.getContext('2d');
            
            // 设置canvas尺寸为正方形，使用较小的尺寸以提高性能
            const size = Math.min(img.width, 300); // 限制最大尺寸
            canvas.width = size;
            canvas.height = size;
            
            // 计算裁剪参数，确保截取上半身部分
            const sourceY = 0; // 从顶部开始
            const sourceHeight = Math.min(img.width, img.height * 0.4); // 只取上面40%的部分
            
            // 绘制图片的上半部分
            ctx.drawImage(
              img,
              0, sourceY, // 源图像的起始坐标
              img.width, sourceHeight, // 源图像的裁剪尺寸
              0, 0, // canvas的起始坐标
              size, size // canvas的绘制尺寸
            );
            
            // 将处理后的图片转换为URL，使用较高的质量
            const processedUrl = canvas.toDataURL('image/jpeg', 0.9);
            console.log('Image processed successfully');
            this.imageCache.set(url, processedUrl);
          } else {
            console.log('Using original image (square aspect ratio)');
            this.imageCache.set(url, url);
          }
          // 强制更新视图
          this.$forceUpdate();
        } catch (error) {
          console.error('Image processing failed:', error);
          console.error('Error details:', {
            url: url,
            width: img.width,
            height: img.height,
            error: error.message
          });
          this.imageCache.set(url, url); // 处理失败时使用原图
          this.$forceUpdate();
        }
      };

      img.onerror = (error) => {
        console.error('Image failed to load:', url);
        console.error('Error details:', error);
        this.imageCache.set(url, url); // 加载失败时使用原图
        this.$forceUpdate();
      };

      // 设置图片源
      console.log('Starting to load image:', url);
      img.src = url;

      // 在图片加载完成前返回原始URL
      return url;
    },
    handleAvatarError(e) {
      console.error('Avatar error event:', e);
    }
  },
  watch: {
    '$route.query.search': {
      handler(newVal) {
        this.searchQuery = newVal || ''
        this.handleSearch()
      },
      immediate: true
    }
  }
}
</script>

<style scoped>
.search-box {
  display: flex;
  gap: 10px;
}

.search-input {
  flex: 1;
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.suggestion-detail {
  font-size: 12px;
  color: #909399;
}

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
  transition: all 0.3s ease;
  height: 100%; /* 确保卡片高度占满父容器 */
  display: flex;
  flex-direction: column; /* 让内容和按钮按列排列 */
  justify-content: space-between; /* 确保内容和按钮之间有足够的空间 */
  background-color: #fff; /* 确保卡片背景为白色 */
  border-radius: 8px; /* 可选：为卡片添加圆角 */
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1); /* 可选：为卡片添加阴影 */
  padding: 20px;
}

.teacher-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}

.teacher-header {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
  height: 160px; /* 设置固定高度 */
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
  margin-top: 10px;
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

.teacher-avatar {
  border: 2px solid #eee;
  background-color: #fff;
  object-fit: cover;
  width: 120px !important;  /* 覆盖el-avatar的默认样式 */
  height: 160px !important; /* 覆盖el-avatar的默认样式 */
  border-radius: 8px !important; /* 改为圆角矩形 */
}
</style> 









