<script>
import request from '@/utils/request'

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
      teachers: []
    }
  },
  computed: {
    canSubmit() {
      return this.selectedTags.length > 0 || this.userInput.trim() !== ''
    }
  },
  methods: {
    loadTeachers() {
      try {
        console.log('开始加载教师数据...')
        
        request.get("http://localhost:9090/api/teachers").then(res => {
          console.log('API响应:', res)
          if (res.code === 200) {
            this.teachers = res.data
            console.log('成功从API获取教师数据:', this.teachers)
          } else {
            console.warn('API请求失败，使用模拟数据')
            this.useLocalData()
          }
        }).catch(error => {
          console.warn('API请求异常，使用模拟数据:', error)
          this.useLocalData()
        })
      } catch (error) {
        console.error('加载教师数据失败:', error)
        this.$message.error(`加载教师数据失败: ${error.message}`)
        this.useLocalData()
      }
    },
    useLocalData() {
      // 使用模拟数据
      console.log('使用模拟数据')
      this.teachers = [
        {
          id: 1,
          name: '黄浩',
          title: '教授',
          research_area: '大数据挖掘与分析,机器学习',
          profile_url: 'http://jszy.whu.edu.cn/huanghao',
          department: '计算机科学与技术学院',
          rank: '正高级'
        },
        {
          id: 2,
          name: '江桂佳',
          title: '教授',
          research_area: '大数据挖掘与分析,分布式系统',
          profile_url: 'https://jszy.whu.edu.cn/jianggj',
          department: '计算机科学与技术学院',
          rank: '正高级'
        },
        {
          id: 3, 
          name: '刘树波',
          title: '教授',
          research_area: '嵌入式系统,物联网及其应用',
          profile_url: 'http://cs.whu.edu.cn/info/',
          department: '计算机科学与技术学院',
          rank: '正高级'
        },
        {
          id: 4,
          name: '刘咸威',
          title: '教授',
          research_area: '机器学习与智能交互,人工智能',
          profile_url: 'http://cs.whu.edu.cn/info/',
          department: '计算机科学与技术学院',
          rank: '正高级'
        },
        {
          id: 5,
          name: '牛柱光',
          title: '教授',
          research_area: '大数据挖掘与分析,机器学习',
          profile_url: 'http://cs.whu.edu.cn/info/',
          department: '计算机科学与技术学院',
          rank: '正高级'
        }
      ]
      
      console.log('成功加载教师模拟数据:', this.teachers)
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

      try {
        // 确保教师数据已加载
        if (this.teachers.length === 0) {
          await this.loadTeachers()
        }

        // 使用request向后端发送请求
        try {
          // 取消注释下面的代码来启用DeepSeek推荐
          /*
          request.post("/deepSeek/recommend", {
            tags: this.selectedTags,
            preferences: this.userInput.trim(),
            teachers: this.teachers
          }).then(res => {
            console.log('DeepSeek响应:', res)
            if (res.code === 200) {
              this.recommendation = res.data
            } else {
              console.warn('DeepSeek API请求失败，使用本地推荐')
              this.useLocalRecommend()
            }
          }).catch(error => {
            console.warn('DeepSeek API请求异常，使用本地推荐:', error)
            this.useLocalRecommend()
          })
          */
          
          // 暂时使用本地推荐
          this.useLocalRecommend()
        } catch (error) {
          console.error('推荐处理失败:', error)
          this.$message.error(`推荐处理失败: ${error.message}`)
          this.useLocalRecommend()
        }
      } catch (error) {
        console.error('处理推荐失败:', error)
        this.$message.error(`处理推荐失败: ${error.message}`)
      } finally {
        this.isLoading = false
      }
    },
    
    useLocalRecommend() {
      // 获取选中的标签的标签文本
      const selectedTagsText = this.selectedTags.map(tag => this.getTagLabel(tag)).join('、')
      
      // 根据标签和用户输入进行简单的本地推荐逻辑
      let recommendTeachers = [...this.teachers]
      
      // 如果选择了"科研成果多"标签，优先推荐研究方向多的老师
      if (this.selectedTags.includes('research')) {
        recommendTeachers.sort((a, b) => {
          const aResearchCount = a.research_area ? a.research_area.split(',').length : 0
          const bResearchCount = b.research_area ? b.research_area.split(',').length : 0
          return bResearchCount - aResearchCount
        })
      }
      
      // 如果用户有输入特定研究方向的偏好
      if (this.userInput) {
        const userPreferences = this.userInput.toLowerCase()
        recommendTeachers = recommendTeachers.filter(teacher => {
          if (!teacher.research_area) return false
          return teacher.research_area.toLowerCase().includes(userPreferences)
        })
      }
      
      // 生成推荐文本
      this.recommendation = `根据您选择的标签：${selectedTagsText || '无'}\n`
      this.recommendation += `以及您的输入：${this.userInput || '无'}\n\n`
      this.recommendation += `我们为您推荐以下导师：\n\n`
      
      if (recommendTeachers.length === 0) {
        this.recommendation += `很抱歉，没有找到符合条件的导师。请尝试其他条件。`
      } else {
        recommendTeachers.slice(0, 3).forEach((teacher, index) => {
          this.recommendation += `${index + 1}. ${teacher.name}（${teacher.title}）\n`
          this.recommendation += `   所属院系：${teacher.department}\n`
          this.recommendation += `   研究方向：${teacher.research_area || '无'}\n`
          this.recommendation += `   个人主页：${teacher.profile_url || '无'}\n\n`
        })
      }
    },
  },
  created() {
    this.loadTeachers()
  },
  beforeUnmount() {
    // 组件卸载前的清理工作
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