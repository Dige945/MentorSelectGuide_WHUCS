<template>
  <div class="news-container">
    <div class="news-filter">
      <el-radio-group v-model="selectedTag" @change="handleTagChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="tag in availableTags" :key="tag" :label="tag">
          {{ tag }}
        </el-radio-button>
      </el-radio-group>
    </div>
    <div class="news-list">
      <div v-if="filteredNews.length === 0" class="empty-text">
        暂无新闻
      </div>
      <div v-else v-for="news in filteredNews" :key="news.id" class="news-item">
        <div class="news-content">
          <div class="news-header">
            <div class="news-info">
              <p class="news-text">{{ news.content }}</p>
            </div>
            <span class="news-time">{{ formatTime(news.createdAt) }}</span>
          </div>
          <div class="news-tags">
            <el-tag
              v-for="tag in getNewsLabels(news)"
              :key="tag"
              size="small"
              :type="getTagType(tag)"
              class="news-tag"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export default {
  name: 'News',
  setup() {
    const newsList = ref([])
    const loading = ref(false)
    const selectedTag = ref('')
    const availableTags = ref(['科研', '活动', '通知', '其他'])

    // 将 YY-MM-DD 格式转换为标准日期
    const parseDate = (dateStr) => {
      if (!dateStr) return new Date(0);
      const [year, month, day] = dateStr.split('-').map(num => parseInt(num));
      // 注意：这里年份需要加上 2000，因为是两位数年份
      return new Date(2000 + year, month - 1, day);
    }

    // 从本地存储加载标签
    const loadAvailableTags = () => {
      const savedTags = localStorage.getItem('availableTags')
      if (savedTags) {
        availableTags.value = JSON.parse(savedTags)
      }
    }

    // 获取新闻标签数组
    const getNewsLabels = (news) => {
      if (!news.label) return []
      try {
        return typeof news.label === 'string' ? JSON.parse(news.label) : news.label
      } catch (error) {
        console.error('解析标签失败:', error)
        return []
      }
    }

    // 获取标签类型
    const getTagType = (tag) => {
      const types = {
        '科研': 'success',
        '活动': 'warning',
        '通知': 'info',
        '其他': ''
      }
      return types[tag] || ''
    }

    // 获取新闻列表
    const fetchNews = async () => {
      try {
        loading.value = true
        const response = await request.get('/news/getNewsList')
        if (response.code === '0') {
          console.log('原始新闻数据:', response.data)
          newsList.value = response.data
          // 从新闻数据中收集所有标签
          const allTags = new Set()
          response.data.forEach(news => {
            const labels = getNewsLabels(news)
            labels.forEach(label => allTags.add(label))
          })
          // 更新可用标签列表
          availableTags.value = Array.from(allTags)
        } else {
          ElMessage.error(response.msg || '获取新闻失败')
        }
      } catch (error) {
        console.error('获取新闻失败:', error)
        ElMessage.error('获取新闻失败')
      } finally {
        loading.value = false
      }
    }

    // 过滤新闻列表
    const filteredNews = computed(() => {
      // 先按时间排序
      const sortedNews = [...newsList.value].sort((a, b) => {
        const dateA = parseDate(a.createdAt)
        const dateB = parseDate(b.createdAt)
        return dateB - dateA
      })
      // 如果没有选择标签，返回所有新闻
      if (!selectedTag.value) return sortedNews
      // 如果选择了标签，过滤包含该标签的新闻
      return sortedNews.filter(news => {
        const labels = getNewsLabels(news)
        return labels.includes(selectedTag.value)
      })
    })

    // 处理标签变化
    const handleTagChange = (value) => {
      selectedTag.value = value
    }

    // 格式化时间
    const formatTime = (timestamp) => {
      if (!timestamp) return ''
      if (typeof timestamp === 'string' && timestamp.includes('-')) {
        return timestamp.split(' ')[0]
      }
      const date = new Date(timestamp)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    }

    // 监听本地存储变化
    window.addEventListener('storage', (e) => {
      if (e.key === 'availableTags') {
        loadAvailableTags()
      }
    })

    onMounted(() => {
      fetchNews()
    })

    return {
      newsList,
      filteredNews,
      loading,
      selectedTag,
      availableTags,
      formatTime,
      handleTagChange,
      getNewsLabels,
      getTagType
    }
  }
}
</script>

<style scoped>
.news-container {
  margin: 20px auto;
  max-width: 1400px;
  padding: 30px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  height: auto;
}

.news-filter {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.news-list {
  padding: 0 20px;
  border-radius: 8px;
  overflow: hidden;
  height: auto;
}

.news-item {
  padding: 30px;
  border-bottom: 1px solid #f0f2f5;
  transition: all 0.3s ease;
  background-color: #fff;
  margin-bottom: 15px;
}

.news-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.news-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.news-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 0;
}

.news-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin: 0;
  width: 100%;
  padding: 0;
}

.news-info {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  flex: 1;
}

.news-text {
  margin: 0;
  padding: 0;
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  flex: 1;
}

.news-time {
  font-size: 14px;
  color: #909399;
  font-weight: 400;
  white-space: nowrap;
  margin-left: 20px;
}

.news-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.news-tag {
  font-size: 12px;
  border-radius: 4px;
}

.news-tag.el-tag--success {
  background-color: #f0f9eb;
}

.news-tag.el-tag--warning {
  background-color: #fdf6ec;
}

.news-tag.el-tag--info {
  background-color: #f4f4f5;
}

.empty-text {
  text-align: center;
  color: #909399;
  padding: 30px 0;
  font-size: 14px;
  font-weight: 400;
}

.el-radio-group {
  margin-bottom: 15px;
}

.el-radio-button {
  margin: 0 8px;
}

.el-radio-button__inner {
  padding: 12px 25px;
  font-size: 15px;
}
</style> 