<template>
  <div class="area-detail-container">
    <h1 class="page-title">{{ areaTitle }}</h1>
    
    <div class="area-stats">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number">{{ totalPapers }}</div>
            <div class="stat-label">相关论文</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number">{{ totalBenchmarks }}</div>
            <div class="stat-label">基准测试</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-number">{{ directions.length }}</div>
            <div class="stat-label">研究方向</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="filter-container">
      <el-input
        v-model="searchQuery"
        placeholder="搜索研究方向"
        prefix-icon="Search"
        clearable
        style="width: 300px"
      />
      <el-select v-model="sortBy" placeholder="排序方式" style="width: 200px; margin-left: 20px">
        <el-option label="论文数量从多到少" value="papers-desc" />
        <el-option label="论文数量从少到多" value="papers-asc" />
        <el-option label="基准测试从多到少" value="benchmarks-desc" />
        <el-option label="基准测试从少到多" value="benchmarks-asc" />
      </el-select>
    </div>

    <div class="directions-grid">
      <el-row :gutter="20">
        <el-col :span="6" v-for="direction in filteredDirections" :key="direction.name">
          <el-card shadow="hover" class="direction-card" @click="navigateToTask(direction.name)">
            <div class="direction-content">
              <h3>{{ direction.name }}</h3>
              <div class="direction-stats">
                <span class="paper-count">{{ direction.papers }}篇论文</span>
                <span class="benchmarks">{{ direction.benchmarks }}个基准测试</span>
              </div>
              <div class="direction-description">
                {{ direction.description }}
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AreaDetail',
  data() {
    return {
      areaId: this.$route.params.area,
      searchQuery: '',
      sortBy: 'papers-desc',
      directions: []
    }
  },
  computed: {
    areaTitle() {
      const areaMap = {
        'cv': '计算机视觉 (Computer Vision)',
        'nlp': '自然语言处理 (NLP)',
        'methodology': '方法论 (Methodology)',
        'graphs': '图 (Graphs)',
        'code': '计算机代码 (Computer Code)',
        'robots': '机器人 (Robots)',
        'adversarial': '对抗 (Adversarial)'
      }
      return areaMap[this.areaId] || '未知领域'
    },
    totalPapers() {
      return this.directions.reduce((sum, dir) => sum + dir.papers, 0)
    },
    totalBenchmarks() {
      return this.directions.reduce((sum, dir) => sum + dir.benchmarks, 0)
    },
    filteredDirections() {
      let result = [...this.directions]
      
      // 搜索筛选
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase()
        result = result.filter(direction => 
          direction.name.toLowerCase().includes(query) ||
          direction.description.toLowerCase().includes(query)
        )
      }
      
      // 排序
      if (this.sortBy === 'papers-desc') {
        result.sort((a, b) => b.papers - a.papers)
      } else if (this.sortBy === 'papers-asc') {
        result.sort((a, b) => a.papers - b.papers)
      } else if (this.sortBy === 'benchmarks-desc') {
        result.sort((a, b) => b.benchmarks - a.benchmarks)
      } else if (this.sortBy === 'benchmarks-asc') {
        result.sort((a, b) => a.benchmarks - b.benchmarks)
      }
      
      return result
    }
  },
  created() {
    // 根据 areaId 加载对应的研究方向数据
    this.loadDirections()
  },
  methods: {
    loadDirections() {
      // 这里应该从后端API获取数据
      // 现在使用模拟数据
      const areaData = {
        cv: [
          { name: '图像分类', papers: 500, benchmarks: 12, description: '将图像分类到预定义的类别中' },
          { name: '目标检测', papers: 450, benchmarks: 8, description: '检测图像中的目标并定位其位置' },
          { name: '语义分割', papers: 350, benchmarks: 6, description: '为图像中的每个像素分配语义标签' },
          { name: '实例分割', papers: 280, benchmarks: 5, description: '检测和分割图像中的每个实例' },
          { name: '姿态估计', papers: 220, benchmarks: 4, description: '估计图像中人体或物体的姿态' },
          { name: '图像生成', papers: 180, benchmarks: 3, description: '生成新的图像内容' },
          { name: '视频理解', papers: 160, benchmarks: 3, description: '理解视频内容并进行分析' },
          { name: '3D视觉', papers: 140, benchmarks: 3, description: '处理和分析3D视觉数据' }
        ],
        nlp: [
          { name: '机器翻译', papers: 600, benchmarks: 15, description: '将文本从一种语言翻译到另一种语言' },
          { name: '文本分类', papers: 550, benchmarks: 10, description: '将文本分类到预定义的类别中' },
          { name: '命名实体识别', papers: 400, benchmarks: 8, description: '识别文本中的命名实体' },
          { name: '情感分析', papers: 350, benchmarks: 7, description: '分析文本的情感倾向' },
          { name: '问答系统', papers: 300, benchmarks: 6, description: '回答用户提出的问题' },
          { name: '文本生成', papers: 250, benchmarks: 5, description: '生成新的文本内容' },
          { name: '对话系统', papers: 200, benchmarks: 4, description: '与用户进行自然语言对话' },
          { name: '预训练模型', papers: 180, benchmarks: 4, description: '大规模预训练语言模型' }
        ]
        // 其他领域的数据...
      }
      
      this.directions = areaData[this.areaId] || []
    },
    navigateToTask(task) {
      this.$router.push(`/tasks/${task}`)
    }
  }
}
</script>

<style scoped>
.area-detail-container {
  padding: 20px;
}

.page-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.area-stats {
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

.filter-container {
  margin-bottom: 30px;
  display: flex;
  align-items: center;
}

.directions-grid {
  margin-top: 20px;
}

.direction-card {
  cursor: pointer;
  transition: transform 0.3s;
  margin-bottom: 20px;
}

.direction-card:hover {
  transform: translateY(-5px);
}

.direction-content {
  padding: 15px;
}

.direction-content h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.direction-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.paper-count, .benchmarks {
  font-size: 14px;
  color: #909399;
}

.direction-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}
</style> 