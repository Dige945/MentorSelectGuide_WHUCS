<template>
  <div class="research-container">
    <h1 class="page-title">研究方向</h1>
    
    <div class="research-stats">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number">7</div>
            <div class="stat-label">主要研究领域</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number">35</div>
            <div class="stat-label">具体研究方向</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number">100+</div>
            <div class="stat-label">相关教师</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="research-areas">
      <div v-for="area in researchAreas" :key="area.id" class="area-section">
        <div class="area-header">
          <h2>{{ area.name }}</h2>
          <el-button type="primary" @click="navigateToArea(area.id)">
            查看所有方向
          </el-button>
        </div>
        <el-row :gutter="20">
          <el-col :span="4" v-for="direction in area.directions.slice(0, 5)" :key="direction.id">
            <el-card 
              shadow="hover" 
              class="direction-card"
              @click="navigateToTask(direction.id)"
            >
              <div class="direction-image">
                <img :src="require(`@/assets/research/${direction.image}`)" :alt="direction.name">
              </div>
              <div class="direction-content">
                <div class="direction-name">{{ direction.name }}</div>
                <div class="direction-en-name">{{ direction.enName }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResearchDirection',
  data() {
    return {
      searchQuery: this.$route.query.search || '',
      researchAreas: [
        {
          id: 'cv',
          name: '计算机视觉 (CV)',
          directions: [
            { id: 'image-classification', name: '图像分类', enName: 'Image Classification', image: 'cv/image-classification.jpg' },
            { id: 'object-detection', name: '目标检测', enName: 'Object Detection', image: 'cv/object-detection.jpg' },
            { id: 'semantic-segmentation', name: '语义分割', enName: 'Semantic Segmentation', image: 'cv/semantic-segmentation.jpg' },
            { id: 'face-recognition', name: '人脸识别', enName: 'Face Recognition', image: 'cv/face-recognition.jpg' },
            { id: 'video-analysis', name: '视频分析', enName: 'Video Analysis', image: 'cv/video-analysis.jpg' }
          ]
        },
        {
          id: 'nlp',
          name: '自然语言处理 (NLP)',
          directions: [
            { id: 'text-classification', name: '文本分类', enName: 'Text Classification', image: 'nlp/text-classification.jpg' },
            { id: 'machine-translation', name: '机器翻译', enName: 'Machine Translation', image: 'nlp/machine-translation.jpg' },
            { id: 'sentiment-analysis', name: '情感分析', enName: 'Sentiment Analysis', image: 'nlp/sentiment-analysis.jpg' },
            { id: 'question-answering', name: '问答系统', enName: 'Question Answering', image: 'nlp/question-answering.jpg' },
            { id: 'text-generation', name: '文本生成', enName: 'Text Generation', image: 'nlp/text-generation.jpg' }
          ]
        },
        {
          id: 'methodology',
          name: '方法论',
          directions: [
            { id: 'deep-learning', name: '深度学习', enName: 'Deep Learning', image: 'methodology/deep-learning.jpg' },
            { id: 'reinforcement-learning', name: '强化学习', enName: 'Reinforcement Learning', image: 'methodology/reinforcement-learning.jpg' },
            { id: 'transfer-learning', name: '迁移学习', enName: 'Transfer Learning', image: 'methodology/transfer-learning.jpg' },
            { id: 'federated-learning', name: '联邦学习', enName: 'Federated Learning', image: 'methodology/federated-learning.jpg' },
            { id: 'meta-learning', name: '元学习', enName: 'Meta Learning', image: 'methodology/meta-learning.jpg' }
          ]
        },
        {
          id: 'graphs',
          name: '图计算',
          directions: [
            { id: 'graph-neural-networks', name: '图神经网络', enName: 'Graph Neural Networks', image: 'graphs/graph-neural-networks.jpg' },
            { id: 'graph-embedding', name: '图嵌入', enName: 'Graph Embedding', image: 'graphs/graph-embedding.jpg' },
            // { id: 'graph-matching', name: '图匹配', enName: 'Graph Matching', image: 'graphs/graph-matching.jpg' },
            // { id: 'graph-clustering', name: '图聚类', enName: 'Graph Clustering', image: 'graphs/graph-clustering.jpg' },
            // { id: 'graph-generation', name: '图生成', enName: 'Graph Generation', image: 'graphs/graph-generation.jpg' }
          ]
        },
        {
          id: 'code',
          name: '计算机代码',
          directions: [
            { id: 'code-generation', name: '代码生成', enName: 'Code Generation', image: 'code/code-generation.jpg' },
            { id: 'code-summarization', name: '代码摘要', enName: 'Code Summarization', image: 'code/code-summarization.jpg' },
            { id: 'code-search', name: '代码搜索', enName: 'Code Search', image: 'code/code-search.jpg' },
            { id: 'code-clone-detection', name: '代码克隆检测', enName: 'Code Clone Detection', image: 'code/code-clone-detection.jpg' },
            { id: 'code-recommendation', name: '代码推荐', enName: 'Code Recommendation', image: 'code/code-recommendation.jpg' }
          ]
        },
        {
          id: 'robots',
          name: '机器人',
          directions: [
            { id: 'robot-perception', name: '机器人感知', enName: 'Robot Perception', image: 'robots/robot-perception.jpg' },
            { id: 'robot-control', name: '机器人控制', enName: 'Robot Control', image: 'robots/robot-control.jpg' },
            // { id: 'robot-planning', name: '机器人规划', enName: 'Robot Planning', image: 'robots/robot-planning.jpg' },
            { id: 'robot-learning', name: '机器人学习', enName: 'Robot Learning', image: 'robots/robot-learning.jpg' },
            { id: 'robot-navigation', name: '机器人导航', enName: 'Robot Navigation', image: 'robots/robot-navigation.jpg' }
          ]
        },
        {
          id: 'adversarial',
          name: '对抗学习',
          directions: [
            { id: 'adversarial-attacks', name: '对抗攻击', enName: 'Adversarial Attacks', image: 'adversarial/adversarial-attacks.jpg' },
            // { id: 'adversarial-defense', name: '对抗防御', enName: 'Adversarial Defense', image: 'adversarial/adversarial-defense.jpg' },
            // { id: 'adversarial-training', name: '对抗训练', enName: 'Adversarial Training', image: 'adversarial/adversarial-training.jpg' },
            // { id: 'adversarial-examples', name: '对抗样本', enName: 'Adversarial Examples', image: 'adversarial/adversarial-examples.jpg' },
            // { id: 'adversarial-robustness', name: '对抗鲁棒性', enName: 'Adversarial Robustness', image: 'adversarial/adversarial-robustness.jpg' }
          ]
        }
      ]
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
  methods: {
    handleSearch() {
      if (this.searchQuery) {
        // 遍历所有研究领域和方向，找到匹配的项
        for (const area of this.researchAreas) {
          const matchingDirections = area.directions.filter(direction => 
            direction.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            direction.enName.toLowerCase().includes(this.searchQuery.toLowerCase())
          )
          
          if (matchingDirections.length > 0) {
            // 如果找到匹配的方向，跳转到对应的研究领域页面
            this.$router.push({
              path: `/area/${area.id}`,
              query: { search: this.searchQuery }
            })
            return
          }
        }
        
        // 如果没有找到匹配的方向，显示提示信息
        this.$message.info('未找到相关研究方向')
      }
    },
    navigateToArea(areaId) {
      this.$router.push(`/area/${areaId}`)
    },
    navigateToTask(taskId) {
      this.$router.push(`/tasks/${taskId}`)
    }
  }
}
</script>

<style scoped>
.research-container {
  padding: 20px;
}

.page-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.research-stats {
  margin-bottom: 40px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  color: #606266;
}

.area-section {
  margin-bottom: 40px;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.area-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.direction-card {
  cursor: pointer;
  margin-bottom: 20px;
  transition: all 0.3s;
  padding: 10px;
  height: 180px;
}

.direction-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.direction-image {
  width: 100%;
  height: 80px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 10px;
}

.direction-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.direction-content {
  padding: 0 5px;
}

.direction-name {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
  text-align: center;
  margin-bottom: 5px;
}

.direction-en-name {
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
