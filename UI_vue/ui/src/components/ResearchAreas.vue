<template>
  <div class="research-areas">
    <h2 class="section-title">热门研究方向</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="area in researchAreas" :key="area.id">
        <el-card class="area-card" shadow="hover" @click="goToArea(area.id)">
          <div class="area-icon">
            <el-icon :size="40"><component :is="area.icon" /></el-icon>
          </div>
          <div class="area-info">
            <h3>{{ area.name }}</h3>
            <p class="area-description">{{ area.description }}</p>
            <div class="area-stats">
              <span>{{ area.directions }}个方向</span>
              <span>{{ area.teachers }}位导师</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Monitor, ChatLineRound, Connection, DataAnalysis, Cpu, Histogram, TrendCharts } from '@element-plus/icons-vue'

export default {
  name: 'ResearchAreas',
  components: {
    Monitor,
    ChatLineRound,
    Connection,
    DataAnalysis,
    Cpu,
    Histogram,
    TrendCharts
  },
  setup() {
    const router = useRouter()
    const researchAreas = ref([
      {
        id: 'cv',
        name: '计算机视觉 (CV)',
        description: '研究如何使计算机理解和处理图像与视频',
        directions: 8,
        teachers: 15,
        icon: 'Monitor'
      },
      {
        id: 'nlp',
        name: '自然语言处理 (NLP)',
        description: '研究计算机与人类语言之间的交互',
        directions: 8,
        teachers: 12,
        icon: 'ChatLineRound'
      },
      {
        id: 'methodology',
        name: '方法论 (Methodology)',
        description: '研究机器学习和人工智能的基础理论与方法',
        directions: 6,
        teachers: 10,
        icon: 'Connection'
      },
      {
        id: 'graphs',
        name: '图计算 (Graphs)',
        description: '研究图结构数据的处理与分析方法',
        directions: 5,
        teachers: 8,
        icon: 'DataAnalysis'
      }
    ])

    const goToArea = (id) => {
      router.push(`/area/${id}`)
    }

    return {
      researchAreas,
      goToArea
    }
  }
}
</script>

<style scoped>
.research-areas {
  margin: 40px 0;
  padding: 0 20px;
}

.section-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #303133;
  font-weight: 600;
  text-align: center;
}

.area-card {
  height: 100%;
  transition: transform 0.3s;
  cursor: pointer;
  margin-bottom: 20px;
}

.area-card:hover {
  transform: translateY(-5px);
}

.area-icon {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
  color: #409EFF;
}

.area-info h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #303133;
}

.area-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
  min-height: 40px;
}

.area-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
</style>
