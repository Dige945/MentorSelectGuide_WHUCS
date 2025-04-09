<script>
export default {
  name: 'AIRecommend',
  data() {
    return {
      tags: [
        { label: '放养型', value: 'relaxed' },
        { label: 'Push型', value: 'push' },
        { label: '科研成果多', value: 'research' },
        { label: '竞赛成果多', value: 'competition' },
        { label: '项目经验丰富', value: 'project' },
        { label: '学术严谨', value: 'strict' },
        { label: '平易近人', value: 'friendly' },
        { label: '有海外经历', value: 'overseas' },
        { label: '年轻有为', value: 'young' },
        { label: '资深教授', value: 'senior' }
      ],
      selectedTags: [],
      userInput: '',
      isLoading: false,
      recommendation: '',
      isStreaming: false,
      abortController: null,
      teachers: []
    }
  },
  computed: {
    canSubmit() {
      return this.selectedTags.length > 0 || this.userInput.trim() !== ''
    }
  },
  methods: {
    async loadTeachers() {
      try {
        console.log('开始加载教师数据...')
        const response = await fetch('http://localhost:9090/api/teachers')
        console.log('响应状态:', response.status)
        
        if (!response.ok) {
          const errorText = await response.text()
          console.error('服务器响应错误:', errorText)
          throw new Error(`获取教师数据失败: ${response.status} ${errorText}`)
        }
        
        const data = await response.json()
        console.log('成功加载教师数据:', data)
        this.teachers = data
      } catch (error) {
        console.error('加载教师数据失败:', error)
        this.$message.error(`加载教师数据失败: ${error.message}`)
      }
    },
    toggleTag(tag) {
      const index = this.selectedTags.indexOf(tag)
      if (index === -1) {
        this.selectedTags.push(tag)
      } else {
        this.selectedTags.splice(index, 1)
      }
    },
    removeTag(tag) {
      const index = this.selectedTags.indexOf(tag)
      if (index !== -1) {
        this.selectedTags.splice(index, 1)
      }
    },
    getTagLabel(value) {
      const tag = this.tags.find(t => t.value === value)
      return tag ? tag.label : value
    },
    async handleSubmit() {
      if (!this.canSubmit || this.isLoading) return

      this.isLoading = true
      this.recommendation = ''
      this.isStreaming = true
      this.abortController = new AbortController()

      try {
        if (this.teachers.length === 0) {
          await this.loadTeachers()
        }

        const response = await fetch('api/deepSeek/recommend', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            tags: this.selectedTags,
            preferences: this.userInput.trim(),
            teachers: this.teachers
          }),
          signal: this.abortController.signal
        })

        if (!response.ok) {
          throw new Error('推荐请求失败')
        }

        const reader = response.body.getReader()
        const decoder = new TextDecoder()

        while (this.isStreaming) {
          const { done, value } = await reader.read()
          if (done) {
            this.isStreaming = false
            break
          }

          const chunk = decoder.decode(value, { stream: true })
          this.recommendation += chunk
        }
      } catch (error) {
        if (error.name === 'AbortError') {
          console.log('请求已取消')
        } else {
          console.error('获取推荐失败:', error)
          this.$message.error('获取推荐失败，请稍后重试')
        }
      } finally {
        this.isLoading = false
        this.isStreaming = false
        this.abortController = null
      }
    },
    cancelRequest() {
      if (this.abortController) {
        this.abortController.abort()
        this.isLoading = false
        this.isStreaming = false
        this.abortController = null
      }
    }
  },
  created() {
    this.loadTeachers()
  },
  beforeUnmount() {
    this.cancelRequest()
  }
}
</script>

<template>
  <div class="recommend-container">
    <div class="header">
      <img src="@/assets/deepseek-logo.png" alt="DeepSeek" class="logo">
      <h1>AI导师推荐</h1>
    </div>

    <div class="tags-section">
      <h2>选择你期望的导师类型</h2>
      <div class="tags-container">
        <el-tag
          v-for="tag in tags"
          :key="tag.value"
          :type="selectedTags.includes(tag.value) ? 'primary' : 'info'"
          class="tag-item"
          @click="toggleTag(tag.value)"
        >
          {{ tag.label }}
        </el-tag>
      </div>
    </div>

    <div class="input-section">
      <div class="selected-tags" v-if="selectedTags.length > 0">
        <el-tag
          v-for="tag in selectedTags"
          :key="tag"
          closable
          @close="removeTag(tag)"
        >
          {{ getTagLabel(tag) }}
        </el-tag>
      </div>
      <div class="input-container">
        <el-input
          v-model="userInput"
          type="textarea"
          :rows="4"
          placeholder="请输入你的其他偏好，例如：希望导师有海外经历、希望研究方向是计算机视觉等..."
          :disabled="isLoading"
        />
        <div class="button-group">
          <el-button
            type="primary"
            :loading="isLoading"
            :disabled="!canSubmit"
            @click="handleSubmit"
          >
            获取推荐
          </el-button>
          <el-button
            v-if="isLoading"
            @click="cancelRequest"
          >
            取消
          </el-button>
        </div>
      </div>
    </div>

    <div class="result-section" v-if="recommendation">
      <h2>推荐结果</h2>
      <div class="recommendation-content">
        {{ recommendation }}
      </div>
    </div>
  </div>
</template>

<style scoped>
.recommend-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 100px;
  height: 100px;
  margin-bottom: 20px;
}

.header h1 {
  font-size: 28px;
  color: #303133;
}

.tags-section {
  margin-bottom: 30px;
}

.tags-section h2 {
  font-size: 18px;
  color: #606266;
  margin-bottom: 15px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  transform: translateY(-2px);
}

.input-section {
  margin-bottom: 30px;
}

.selected-tags {
  margin-bottom: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.result-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.result-section h2 {
  font-size: 18px;
  color: #606266;
  margin-bottom: 15px;
}

.recommendation-content {
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .recommend-container {
    padding: 15px;
  }

  .logo {
    width: 80px;
    height: 80px;
  }

  .header h1 {
    font-size: 24px;
  }

  .tags-container {
    gap: 8px;
  }
}
</style>