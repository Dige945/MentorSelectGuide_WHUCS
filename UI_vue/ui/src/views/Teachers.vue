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
        
        <!-- 新的推荐人数显示 -->
        <div class="recommend-stats">
          <div class="recommend-count">
            <div class="count-circle" :class="getRecommendClass(teacher.recommendcount)">
              <span class="count-number">{{ teacher.recommendcount || 0 }}</span>
              <span class="count-label">推荐</span>
            </div>
          </div>
          <div class="recommend-trend" v-if="teacher.recommendcount > 0">
            <el-tooltip 
              :content="getRecommendTip(teacher.recommendcount)"
              placement="top"
            >
              <div class="trend-icon" :class="getRecommendTrendClass(teacher.recommendcount)">
                <el-icon><TrendCharts /></el-icon>
              </div>
            </el-tooltip>
          </div>
        </div>

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
import { Search, TrendCharts } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AOS from 'aos';
import 'aos/dist/aos.css';

export default {
  name: 'TeachersView',
  components: {
    Search,
    TrendCharts
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
      
      // 先检查是否可以评价
      this.checkCanEvaluate(teacher.id);
    },
    
    async checkCanEvaluate(teacherId) {
      try {
        const response = await axios.get(`/api/evaluations/can-evaluate/${teacherId}`);
        if (response.data.code === '0') {
          const canEvaluate = response.data.data;
          if (canEvaluate) {
            this.reviewDialogVisible = true;
          } else {
            this.$message.warning('您今天已经评价过该教师，请明天再试');
          }
        } else {
          this.$message.error('检查评价权限失败');
          this.reviewDialogVisible = true; // 失败时也允许显示，后端会再次验证
        }
      } catch (error) {
        console.error('检查评价权限失败:', error);
        this.$message.error('检查评价权限失败，请稍后重试');
        this.reviewDialogVisible = true; // 失败时也允许显示，后端会再次验证
      }
    },
    
    async submitReview() {
      if (!this.reviewForm.content.trim()) {
        this.$message.warning('请输入评价内容');
        return;
      }

      // 显示加载动画
      const loadingInstance = this.$loading({
        lock: true,
        text: '正在检查内容...',
        background: 'rgba(0, 0, 0, 0.7)',
        spinner: 'el-icon-loading',
        fullscreen: true,
        customClass: 'custom-loading'
      });

      try {
        // 首先检查内容是否包含负面信息
        console.log('开始检查负面内容，发送请求到:', '/api/deepSeek/check-negative-content');
        console.log('请求内容:', this.reviewForm.content);
        
        const checkResponse = await axios.post('/api/deepSeek/check-negative-content', {
          recommendationText: this.reviewForm.content
        });
        
        // 详细记录API返回内容
        console.log('负面内容检查API返回:', checkResponse);
        console.log('负面内容检查结果:', checkResponse.data);
        
        // 关闭加载动画
        loadingInstance.close();
        
        // 检查是否包含负面内容
        if (checkResponse.data.hasNegativeContent === true) {
          // 如果包含负面内容，显示错误信息
          console.log('检测到负面内容，阻止提交');
          this.$message.error(`内容审核未通过: ${checkResponse.data.message || '包含负面信息'}`);
          return;
        } else {
          console.log('未检测到负面内容，继续提交流程');
        }

        // 显示提交中的加载动画
        const submitLoading = this.$loading({
          lock: true,
          text: '正在提交评价...',
          background: 'rgba(0, 0, 0, 0.7)',
          spinner: 'el-icon-loading',
          fullscreen: true,
          customClass: 'custom-loading'
        });

        try {
          console.log('准备提交评价数据:', {
            teacherId: this.currentTeacher.id,
            teacherName: this.currentTeacher.name,
            content: this.reviewForm.content,
            userId: "1" // 注意：现在是字符串类型
          });

          // 调用后端API保存评价
          const response = await axios.post('/api/evaluations/insert', {
            teacherId: this.currentTeacher.id,
            teacherName: this.currentTeacher.name,
            content: this.reviewForm.content,
            userId: "1" // 注意：现在是字符串类型
          }, {
            headers: {
              'Content-Type': 'application/json'
            }
          });

          // 关闭提交加载动画
          submitLoading.close();

          console.log('服务器响应:', response);

          if (response.data.code === '0') {
            // 更新本地教师数据中的推荐数量
            const teacherIndex = this.teachers.findIndex(t => t.id === this.currentTeacher.id);
            if (teacherIndex !== -1) {
              this.teachers[teacherIndex].recommendcount = (this.teachers[teacherIndex].recommendcount || 0) + 1;
            }

            this.$message.success('评价提交成功！');
            this.reviewDialogVisible = false;
          } else {
            throw new Error(response.data.msg || '提交失败');
          }
        } catch (error) {
          // 关闭提交加载动画
          submitLoading.close();
          
          console.error('提交评价失败:', error);
          if (error.response) {
            console.error('错误状态码:', error.response.status);
            console.error('错误响应数据:', error.response.data);
            this.$message.error(`提交失败: ${error.response.data.msg || '服务器错误'}`);
          } else if (error.request) {
            console.error('没有收到响应:', error.request);
            this.$message.error('提交失败: 无法连接到服务器');
          } else {
            console.error('请求配置错误:', error.message);
            this.$message.error(`提交失败: ${error.message}`);
          }
        }
      } catch (error) {
        // 关闭加载动画
        loadingInstance.close();
        
        console.error('内容检查失败:', error);
        this.$message.error('内容检查失败，请稍后重试');
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
    },
    getRecommendClass(count) {
      if (count >= 100) return 'count-excellent'
      if (count >= 50) return 'count-great'
      if (count >= 20) return 'count-good'
      return 'count-normal'
    },
    
    getRecommendTrendClass(count) {
      if (count >= 100) return 'trend-excellent'
      if (count >= 50) return 'trend-great'
      if (count >= 20) return 'trend-good'
      return 'trend-normal'
    },
    
    getRecommendTip(count) {
      if (count >= 100) return '极受欢迎的导师'
      if (count >= 50) return '深受学生喜爱'
      if (count >= 20) return '评价良好'
      return '暂无明显趋势'
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
  },
  mounted() {
    AOS.init({
      duration: 800,
      offset: 100,
      once: true
    });
  },
  updated() {
    AOS.refresh();
  }
}
</script>

<style>
/* 全局样式，不使用scoped */
.custom-loading .el-loading-spinner .circular {
  width: 60px !important;
  height: 60px !important;
  animation: loading-rotate 2s linear infinite;
}

.custom-loading .el-loading-spinner .el-loading-text {
  color: #fff !important;
  font-size: 18px !important;
  margin-top: 20px !important;
  font-weight: 500 !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3) !important;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0.6;
  }
}

@keyframes loading-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>

<style scoped>
.teachers-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  animation: fadeIn 0.8s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.page-title {
  font-size: 2.5em;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
  font-weight: 700;
  letter-spacing: -0.5px;
  position: relative;
  overflow: hidden;
}

.page-title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  width: 0;
  height: 2px;
  background: #333;
  transform: translateX(-50%);
  animation: titleLine 1s ease forwards 0.5s;
}

@keyframes titleLine {
  to {
    width: 100px;
  }
}

.filter-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 30px;
  position: relative;
  overflow: hidden;
}

.filter-container::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.1), transparent);
  animation: filterLine 2s infinite;
}

@keyframes filterLine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.search-box {
  display: flex;
  gap: 10px;
  position: relative;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__inner) {
  transition: all 0.3s ease;
}

.search-input :deep(.el-input__inner:focus) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.search-button {
  background: #333;
  border: none;
  padding: 12px 24px;
  transition: all 0.3s;
}

.search-button:hover {
  background: #000;
  transform: translateY(-2px);
}

.teacher-card {
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  overflow: hidden;
  animation: fadeIn 0.8s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.teacher-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.teacher-header {
  text-align: center;
  padding: 20px;
  background: #f8f8f8;
  transition: all 0.3s ease;
}

.teacher-card:hover .teacher-header {
  background: #f0f0f0;
}

/* 修改头像样式为长方形 */
:deep(.teacher-avatar) {
  width: 120px !important;
  height: 160px !important;
  border-radius: 4px !important;
  object-fit: cover;
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.teacher-card:hover .teacher-avatar {
  transform: scale(1.05);
}

.teacher-body {
  padding: 20px;
}

.teacher-body h3 {
  margin: 0 0 10px;
  color: #333;
  font-size: 18px;
  font-weight: 600;
}

.teacher-department {
  color: #666;
  margin: 5px 0;
  font-size: 14px;
}

.teacher-title {
  color: #333;
  margin: 5px 0;
  font-size: 14px;
  font-weight: 500;
}

.teacher-recommendcount {
  color: #666;
  margin: 10px 0;
  font-size: 14px;
}

.teacher-research {
  margin: 15px 0;
}

.teacher-research h4 {
  color: #333;
  margin: 0 0 5px;
  font-size: 14px;
  font-weight: 500;
}

.teacher-research p {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.teacher-tags {
  margin-top: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.teacher-tag {
  background: #f5f5f5;
  border: none;
  color: #666;
  transition: all 0.3s ease;
}

.teacher-tag:hover {
  transform: translateY(-2px);
  background: #333 !important;
  color: white !important;
}

.teacher-footer {
  padding: 15px 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
}

.teacher-footer :deep(.el-button) {
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.teacher-footer :deep(.el-button--primary) {
  background: #333;
  border: none;
}

.teacher-footer :deep(.el-button--primary)::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s ease, height 0.6s ease;
}

.teacher-footer :deep(.el-button--primary:hover)::before {
  width: 300px;
  height: 300px;
}

.teacher-footer :deep(.el-button--link) {
  color: #666;
}

.teacher-footer :deep(.el-button--link:hover) {
  color: #333;
}

.pagination-container {
  margin-top: 30px;
  text-align: center;
}

.pagination-container :deep(.el-pagination__total),
.pagination-container :deep(.el-pagination__jump) {
  transition: all 0.3s ease;
}

.pagination-container :deep(.el-pager li) {
  transition: all 0.3s ease;
}

.pagination-container :deep(.el-pager li:hover) {
  transform: translateY(-2px);
}

.pagination-container :deep(.el-pager li.active) {
  animation: bounce 0.3s ease;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

/* 搜索建议样式 */
.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
}

.suggestion-detail {
  font-size: 12px;
  color: #666;
}

/* 评价对话框样式 */
.review-form {
  padding: 20px;
  animation: dialogPop 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes dialogPop {
  from {
    opacity: 0;
    transform: scale(0.9);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.review-form h3 {
  margin-bottom: 20px;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-container {
    padding: 15px;
  }
  
  .search-box {
    flex-direction: column;
  }
  
  .teacher-card {
    margin: 10px;
  }
}

/* Element Plus 组件样式覆盖 */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-pagination) {
  justify-content: center;
}

:deep(.el-empty) {
  padding: 40px 0;
}

/* 添加新的样式 */
.recommend-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 15px 0;
  gap: 10px;
}

.recommend-count {
  position: relative;
}

.count-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: #f5f7fa;
  transition: all 0.3s ease;
  cursor: pointer;
}

.count-circle:hover {
  transform: scale(1.05);
}

.count-number {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
  background: linear-gradient(45deg, #333, #666);
  -webkit-background-clip: text;
  color: transparent;
}

.count-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 不同等级的样式 */
.count-excellent {
  background: linear-gradient(135deg, #ffd700, #ffa500);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2);
}

.count-excellent .count-number {
  background: linear-gradient(45deg, #fff, #fff8dc);
  -webkit-background-clip: text;
}

.count-excellent .count-label {
  color: #fff;
}

.count-great {
  background: linear-gradient(135deg, #87ceeb, #4169e1);
  box-shadow: 0 4px 12px rgba(135, 206, 235, 0.2);
}

.count-great .count-number {
  background: linear-gradient(45deg, #fff, #f0f8ff);
  -webkit-background-clip: text;
}

.count-great .count-label {
  color: #fff;
}

.count-good {
  background: linear-gradient(135deg, #98fb98, #3cb371);
  box-shadow: 0 4px 12px rgba(152, 251, 152, 0.2);
}

.count-good .count-number {
  background: linear-gradient(45deg, #fff, #f0fff0);
  -webkit-background-clip: text;
}

.count-good .count-label {
  color: #fff;
}

.count-normal {
  background: linear-gradient(135deg, #f5f5f5, #e0e0e0);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 趋势图标样式 */
.recommend-trend {
  display: flex;
  align-items: center;
}

.trend-icon {
  padding: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.trend-icon:hover {
  transform: translateY(-2px);
}

.trend-excellent {
  color: #ffd700;
  background: rgba(255, 215, 0, 0.1);
}

.trend-great {
  color: #4169e1;
  background: rgba(65, 105, 225, 0.1);
}

.trend-good {
  color: #3cb371;
  background: rgba(60, 179, 113, 0.1);
}

.trend-normal {
  color: #909399;
  background: rgba(144, 147, 153, 0.1);
}

/* 添加动画效果 */
@keyframes countPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

.count-circle {
  animation: countPulse 2s infinite;
}

.count-excellent {
  animation: countPulse 2s infinite, shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 确保渐变背景可以动画 */
.count-excellent,
.count-great,
.count-good {
  background-size: 200% 200%;
}
</style> 









