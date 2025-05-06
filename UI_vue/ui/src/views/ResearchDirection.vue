<template>
  <div class="research-container">
    <h1 class="page-title" data-aos="fade-down">研究方向</h1>
    
    <div class="research-stats" data-aos="fade-up" data-aos-delay="200">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-number animate-number">7</div>
              <div class="stat-label">主要研究领域</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-number animate-number">35</div>
              <div class="stat-label">具体研究方向</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-number animate-number">100+</div>
              <div class="stat-label">相关教师</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="research-areas">
      <div v-for="(area, index) in researchAreas" 
           :key="area.id" 
           class="area-section">
        <div class="area-header">
          <h2>{{ area.name }}</h2>
          <el-button 
            type="primary" 
            class="view-all-btn"
            @click="navigateToArea(area.id)"
          >
            查看所有方向
          </el-button>
        </div>
        <el-row :gutter="20">
          <el-col 
            :span="4" 
            v-for="direction in area.directions.slice(0, 5)" 
            :key="direction.id"
          >
            <el-card 
              shadow="hover" 
              class="direction-card"
            >
              <div class="direction-image-container">
                <div class="direction-image">
                  <img :src="require(`@/assets/research/${direction.image}`)" :alt="direction.name">
                </div>
                <div class="image-overlay"></div>
              </div>
              <div class="direction-content">
                <div class="direction-name">{{ direction.name }}</div>
                <div class="direction-en-name">{{ direction.enName }}</div>
                <div class="direction-buttons">
                  <el-button 
                    type="text" 
                    class="detail-btn"
                    @click="navigateToPapersWithCode(direction)"
                  >
                    具体详情
                  </el-button>
                  <el-button 
                    type="text" 
                    class="ai-btn"
                    @click="showAIDescription(direction)"
                  >
                    问AI
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="currentDirection ? currentDirection.name + '研究方向介绍' : '研究方向介绍'"
      width="50%"
      class="custom-dialog"
      @close="handleClose"
    >
      <div class="ai-description">
        <div v-if="aiDescription" v-html="formattedDescription"></div>
        <div v-else class="loading-text">
          <el-icon class="is-loading"><Loading /></el-icon>
          正在生成介绍...
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ElLoading } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { marked } from 'marked'

export default {
  name: 'ResearchDirection',
  components: {
    Loading
  },
  data() {
    return {
      searchQuery: this.$route.query.search || '',
      dialogVisible: false,
      currentDirection: null,
      aiDescription: '',
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
  computed: {
    formattedDescription() {
      if (!this.aiDescription) return '';
      // Using marked to convert markdown to HTML
      return marked(this.aiDescription);
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
      // 领域ID到PapersWithCode领域名称的映射
      const areaMapping = {
        'cv': 'computer-vision',
        'nlp': 'natural-language-processing',
        'methodology': 'methodology',
        'graphs': 'graphs',
        'code': 'code',
        'multimodal': 'multimodal',
        'speech': 'speech',
        'robotics': 'robotics'
      };

      const paperswithcodeArea = areaMapping[areaId] || areaId;
      window.open(`https://paperswithcode.com/area/${paperswithcodeArea}`, '_blank');
    },
    navigateToTask(taskId) {
      this.$router.push(`/tasks/${taskId}`)
    },
    navigateToPapersWithCode(direction) {
      // 这里需要根据实际的研究方向ID构建PapersWithCode的URL
      const baseUrl = 'https://paperswithcode.com/task/';
      const url = baseUrl + direction.id;
      window.open(url, '_blank');
    },
    formatDescription(text) {
      // 将换行符转换为HTML换行
      return text.replace(/\n/g, '<br>')
    },
    
    async showAIDescription(direction) {
      this.dialogVisible = true;
      this.currentDirection = direction;
      this.aiDescription = '';
      
      try {
        // 先检查该IP是否已经查询过这个研究方向
        const checkResponse = await fetch(`/api/deepSeek/direction/can-query?directionName=${encodeURIComponent(direction.name)}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        });
        
        const checkResult = await checkResponse.json();
        
        if (checkResult.success && !checkResult.canQuery) {
          // 该IP已经查询过这个研究方向，显示默认介绍
          console.log('已查询过该研究方向，显示默认介绍:', direction.name);
          this.showDefaultDescription(direction);
          return;
        }
        
        // 未查询过该研究方向，调用API获取介绍
        const response = await fetch('/api/deepSeek/direction/intro', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ direction: direction.name })
        });

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        while (true) {
          const { value, done } = await reader.read();
          if (done) break;
          
          const text = decoder.decode(value);
          // 先去掉data:前缀和多余空白
          let cleanText = text.replace(/data:/g, '').trim();
          // 累加
          this.aiDescription += cleanText;
        }
        // --- markdown结构优化 ---
        let formatted = this.aiDescription;
        // 标题前后加空行
        formatted = formatted.replace(/###\s*(.+)/g, '\n\n### $1\n');
        // 二级标题前后加空行
        formatted = formatted.replace(/##\s*(.+)/g, '\n\n## $1\n');
        // 加粗关键词
        formatted = formatted.replace(/(基本概念|主要研究内容|应用场景|最新研究热点|未来趋势)/g, '**$1**');
        // 合并多余空行
        formatted = formatted.replace(/\n{3,}/g, '\n\n');
        this.aiDescription = formatted;
      } catch (error) {
        console.error('Error:', error);
        this.$message.error('获取研究方向介绍失败');
      }
    },

    // 显示默认介绍
    showDefaultDescription(direction) {
      // 根据研究方向类型提供默认介绍
      const defaultDescriptions = {
        // 计算机视觉
        'image-classification': `
## 图像分类研究方向

**基本概念** 图像分类是计算机视觉的基础任务，旨在对整个图像进行分类，确定其所属的类别。这是计算机理解视觉世界的第一步。

**主要研究内容** 图像分类的核心在于如何从大量图像数据中提取有效特征，构建分类模型。从传统的手工特征到现代的深度学习方法，研究不断发展。

**应用场景** 图像分类广泛应用于医疗诊断、物体识别、自动驾驶、安防监控等多个领域，为智能决策提供视觉支持。

**最新研究热点** 目前研究热点包括少样本学习、自监督学习和多模态融合方法，以及如何构建更轻量级但高精度的分类模型。
        `,
        '目标检测': `
## 目标检测研究方向

**基本概念** 目标检测是计算机视觉中的关键任务，旨在识别图像中物体的位置和类别。它实现了从"看到"到"理解"的重要跨越。

**主要研究内容** 研究内容包括检测框架设计、特征提取网络优化、多尺度检测方法和训练策略改进，以提高检测精度和速度。

**应用场景** 目标检测在自动驾驶、视频监控、工业质检、零售分析和增强现实等领域有广泛应用，为计算机赋予了"发现"能力。

**最新研究热点** 当前研究热点包括无锚点检测、端到端检测框架、注意力机制应用和实时检测算法，以及如何处理极端场景下的检测问题。
        `,
        // NLP方向
        '文本分类': `
## 文本分类研究方向

**基本概念** 文本分类是自然语言处理的基础任务，旨在将文本根据内容分配到预定义的类别中，是语言理解的基础。

**主要研究内容** 研究重点包括特征表示学习、分类模型设计、标签不平衡处理和多语言分类技术，以及如何解决数据稀疏问题。

**应用场景** 文本分类广泛应用于情感分析、垃圾邮件过滤、新闻分类、内容推荐和客户反馈分析等领域，为信息处理提供基础。

**最新研究热点** 当前研究热点包括大型语言模型的微调、对比学习、多模态分类方法，以及如何使用更少的标注数据实现高质量分类。
        `,
        '深度学习': `
## 深度学习研究方向

**基本概念** 深度学习是机器学习的分支，通过多层次神经网络结构从数据中学习表示，模拟人脑对信息的处理方式。

**主要研究内容** 研究内容包括神经网络架构设计、优化算法改进、正则化方法、泛化理论和可解释性探索，以构建更强大的学习系统。

**应用场景** 深度学习已渗透到计算机视觉、自然语言处理、语音识别、推荐系统和生物信息学等众多领域，成为人工智能的核心驱动力。

**最新研究热点** 当前研究热点包括大规模预训练模型、自监督学习框架、神经架构搜索、能效优化和隐私保护学习方法，推动着AI技术边界的扩展。
        `,
      };
      
      // 获取默认描述，如果没有特定描述则提供通用描述
      let description = defaultDescriptions[direction.id] || 
                         defaultDescriptions[direction.name] ||
                         `## ${direction.name}研究方向\n\n该研究方向正在蓬勃发展中。由于您已经查询过这个方向，我们提供了这份通用介绍。您可以通过以下方式了解更多信息：\n\n1. 访问学校研究方向主页\n2. 咨询相关领域的导师\n3. 查阅相关学术论文和综述\n\n该方向在理论基础和应用实践上都有重要意义，是计算机科学与技术学科的重要组成部分。`;
      
      // 使用markdown解析器格式化
      this.aiDescription = description.trim();
    },

    handleClose() {
      this.dialogVisible = false
      this.currentDirection = null
      this.aiDescription = ''
    }
  }
}
</script>

<style scoped>
.research-container {
  padding: 40px;
  background: #ffffff;
  min-height: 100vh;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #000;
  text-align: center;
  margin-bottom: 50px;
  letter-spacing: 2px;
}

.research-stats {
  margin-bottom: 60px;
}

.stat-card {
  background: #fff;
  border: 1px solid #eee;
  transition: all 0.3s ease;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
}

.stat-content {
  padding: 20px;
  text-align: center;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 700;
  color: #000;
  margin-bottom: 10px;
}

.animate-number {
  opacity: 0;
  animation: fadeInUp 0.6s ease forwards;
}

.stat-label {
  font-size: 1rem;
  color: #666;
  letter-spacing: 1px;
}

.area-section {
  margin-bottom: 60px;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.area-header h2 {
  font-size: 1.8rem;
  font-weight: 600;
  color: #000;
  margin: 0;
}

.view-all-btn {
  background: #000;
  color: #fff;
  border: none;
  padding: 12px 24px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.view-all-btn:hover {
  background: #333;
  transform: translateY(-2px);
}

.direction-card {
  height: 280px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: none;
  background: #fff;
  overflow: hidden;
}

.direction-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.direction-image-container {
  position: relative;
  height: 140px;
  overflow: hidden;
}

.direction-image {
  width: 100%;
  height: 100%;
  transition: all 0.4s ease;
}

.direction-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.4s ease;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.2);
  opacity: 0;
  transition: all 0.4s ease;
}

.direction-card:hover .direction-image img {
  transform: scale(1.1);
  filter: grayscale(0%);
}

.direction-card:hover .image-overlay {
  opacity: 1;
}

.direction-content {
  padding: 20px;
}

.direction-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #000;
  margin-bottom: 5px;
}

.direction-en-name {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 8px;
}

.direction-buttons {
  display: flex;
  justify-content: space-between;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.3s ease;
  margin-top: -5px;
}

.direction-card:hover .direction-buttons {
  opacity: 1;
  transform: translateY(0);
}

.detail-btn, .ai-btn {
  color: #000;
  padding: 0;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.detail-btn:hover, .ai-btn:hover {
  color: #666;
  transform: translateX(5px);
}

.custom-dialog {
  border-radius: 8px;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #666;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .research-container {
    padding: 20px;
  }

  .page-title {
    font-size: 2rem;
  }

  .stat-number {
    font-size: 2rem;
  }

  .area-header {
    flex-direction: column;
    gap: 15px;
  }

  .direction-card {
    height: auto;
  }
}
</style>


