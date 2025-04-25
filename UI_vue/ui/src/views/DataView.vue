<template>
  <div class="data-view-container">
    <div class="header" v-motion-slide-visible-bottom>
      <h1>计算机科研数据可视化</h1>
      <div class="time">{{ currentTime }}</div>
    </div>
    
    <div class="main-content">
      <div class="charts-grid">
        <!-- 第一行 -->
        <div class="charts-row">
          <!-- 实时论文发布数量 -->
          <div class="chart-card paper-trend-card" v-motion-slide-visible-left>
            <h3>实时论文发布趋势</h3>
            <div class="paper-stats">
              <div class="stat-item" v-motion-pop-visible>
                <div class="stat-value">{{ todayPapers }}</div>
                <div class="stat-label">今日发布</div>
              </div>
              <div class="stat-item" v-motion-pop-visible>
                <div class="stat-value">{{ weekPapers }}</div>
                <div class="stat-label">本周发布</div>
              </div>
            </div>
            <div id="lineChart" style="width: 100%; height: 380px;"></div>
          </div>

          <!-- CSRanking排名 -->
          <div class="chart-card ranking-card" v-motion-slide-visible-right style="width: 600px; height: 540px;">
            <h3>CSRanking亚洲排名</h3>
            <div class="ranking-selector">
              <select v-model="selectedArea" @change="updateRanking">
                <option value="all">综合排名</option>
                <option value="ai">人工智能</option>
                <option value="systems">系统</option>
                <option value="theory">理论</option>
              </select>
            </div>
            
            <div class="ranking-container" style="width: 500px">
              <div class="whu-ranking-container">
                <div class="whu-ranking" v-motion-pop-visible>
                  <div class="ranking-header">
                    <div class="ranking-title">WHU</div>
                    <div class="ranking-score-container">
                      <div class="ranking-score">{{ whuRanking.count }}</div>
                      <div class="ranking-position">亚洲第 {{ whuRanking.rank }} 名</div>
                    </div>
                  </div>
                  <div class="ranking-details">
                    <div class="detail-item" v-for="(detail, index) in rankingDetails" 
                        :key="detail.label"
                        v-motion-slide-visible-left
                        :delay="index * 100">
                      <span class="label">{{ detail.label }}</span>
                      <span class="value">第 {{ detail.value }} 名</span>
                    </div>
                  </div>
                </div>
                
                <div class="nearby-universities" v-motion-slide-visible-right>
                  <div class="list-title">相近排名高校</div>
                  <div class="list-content">
                    <div v-for="(uni, index) in nearbyUniversities" 
                        :key="uni.name"
                        class="list-item"
                        :class="{ 'whu': uni.name === '武汉大学' }"
                        v-motion-slide-visible-right
                        :delay="index * 100">
                      <div class="item-rank">{{ uni.rank }}</div>
                      <div class="item-name">{{ uni.name }}</div>
                      <div class="item-score">{{ uni.count }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 第二行 -->
        <div class="charts-row">
          <!-- 顶会DDL -->
          <div class="chart-card" v-motion-slide-visible-left style="width: 500px; height: 540px;">
            <h3>顶会截止日期</h3>
            <div class="nearest-conference" v-if="nearestConference">
              <div class="nearest-badge">最近截止</div>
              <div class="nearest-content">
                <div class="nearest-header">
                  <span class="conf-tier">{{ nearestConference.tier }}</span>
                  <span class="conf-track">{{ nearestConference.track }}</span>
                </div>
                <div class="nearest-name">{{ nearestConference.name }}</div>
                <div class="nearest-date">截止日期：{{ nearestConference.formattedDate }}</div>
                <div class="nearest-countdown" :class="{ 'urgent': nearestConference.daysLeft <= 30 }">
                  <template v-if="nearestConference.status">
                    <span class="status-badge">{{ nearestConference.status }}</span>
                  </template>
                  <template v-else>
                    还剩 {{ nearestConference.daysLeft }} 天
                  </template>
                </div>
              </div>
            </div>
            <div class="conference-list">
              <div v-for="(conf, index) in conferences" 
                   :key="conf.name" 
                   class="conference-item"
                   :class="{ 'urgent': conf.daysLeft <= 30 }"
                   v-motion-slide-visible-right
                   :delay="index * 100">
                <div class="conf-header">
                  <span class="conf-tier">{{ conf.tier }}</span>
                  <span class="conf-track">{{ conf.track }}</span>
                </div>
                <div class="conf-name">{{ conf.name }}</div>
                <div class="conf-info">
                  <div class="conf-date">{{ conf.formattedDate }}</div>
                  <div class="conf-countdown" :class="{ 'urgent': conf.daysLeft <= 30 }">
                    <template v-if="conf.status">
                      <span class="status-badge">{{ conf.status }}</span>
                    </template>
                    <template v-else>
                      还剩 {{ conf.daysLeft }} 天
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 科研热门方向 -->
          <div class="chart-card" v-motion-slide-visible-right style="width: 480px; height: 520px;">
            <h3>热门研究方向</h3>
            <div class="trend-container">
              <div class="trend-header">
                <div class="trend-stats">
                  <div class="stat-box">
                    <div class="stat-value">{{ hotTopics[0].name }}</div>
                    <div class="stat-label">最热方向</div>
                  </div>
                  <div class="stat-box">
                    <div class="stat-value">{{ hotTopics[0].papers }}</div>
                    <div class="stat-label">相关论文</div>
                  </div>
                </div>
              </div>
              <div class="topics-grid">
                <div v-for="(topic, index) in hotTopics" 
                     :key="topic.name"
                     class="topic-card"
                     :class="{ 'hot': topic.hot }"
                     v-motion-pop-visible
                     :delay="index * 100">
                  <div class="topic-content">
                    <div class="topic-name">{{ topic.name }}</div>
                    <div class="topic-stats">
                      <div class="papers-count">{{ topic.papers }} 篇</div>
                      <div class="trend-value" :class="{ 'up': topic.trend.includes('+') }">
                        {{ topic.trend }}
                        <i class="trend-arrow" :class="{ 'up': topic.trend.includes('+') }">▲</i>
                      </div>
                    </div>
                  </div>
                  <div class="topic-progress">
                    <div class="progress-bar" :style="{ width: (topic.papers / 2500 * 100) + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useMotion } from '@vueuse/motion'

export default {
  name: 'DataView',
  setup() {
    const chartContainer = ref(null)
    const currentTime = ref('')
    const todayPapers = ref(0)
    const weekPapers = ref(2)
    const selectedArea = ref('all')
    let charts = []
    let timer = null

    // 热门研究方向数据
    const hotTopics = ref([
      { name: 'LLM', size: 28, trend: '+25%', papers: 2150, hot: true },
      { name: 'AIGC', size: 26, trend: '+20%', papers: 1850, hot: true },
      { name: '联邦学习', size: 24, trend: '+15%', papers: 1560, hot: true },
      { name: '知识图谱', size: 22, trend: '+10%', papers: 1280 },
      { name: '强化学习', size: 20, trend: '+8%', papers: 980 },
      { name: '计算机视觉', size: 24, trend: '+12%', papers: 1680 },
      { name: '自然语言处理', size: 22, trend: '+18%', papers: 1420 },
      { name: '图神经网络', size: 20, trend: '+5%', papers: 860 }
    ])

    // 会议数据
    const conferences = ref([
      { 
        name: 'AAAI 2026',
        date: '2025-07-10',
        tier: 'A',
        track: 'AI'
      },
      { 
        name: 'ICML 2026(未定期)',
        date: '2026-07-01',
        tier: 'A+',
        track: 'ML'
      },
      { 
        name: 'CVPR 2026',
        date: '2026-06-06',
        tier: 'A+',
        track: 'CV'
      },
      { 
        name: 'NeurIPS 2024',
        date: '2025-05-11',
        tier: 'A+',
        track: 'ML'
      },
      { 
        name: 'ICLR 2026(未定期)',
        date: '2026-02-08',
        tier: 'A+',
        track: 'ML'
      }
    ])

    // 武汉大学排名数据
    const whuRanking = ref({
      rank: 40,
      count: 2.3,
      aiRank: 38,
      systemsRank: 45,
      theoryRank: 50
    })

    // 相近排名的高校
    const nearbyUniversities = ref([
      { name: 'Sungkyunkwan University', rank: 34, count: 2.4 },
      { name: 'Xi\'an Jiaotong University', rank: 34, count: 2.4 },
      { name: 'Wuhan University', rank: 40, count: 2.3 },
      { name: 'Xiamen University', rank: 40, count: 2.3 },
      { name: 'Hunan University', rank: 42, count: 2.2 }
    ])

    // 计算属性：排名详情
    const rankingDetails = computed(() => [
      { label: 'AI领域', value: whuRanking.value.aiRank },
      { label: '系统领域', value: whuRanking.value.systemsRank },
      { label: '理论领域', value: whuRanking.value.theoryRank }
    ])

    // 修改会议数据和计算逻辑
    const calculateDaysLeft = (deadline) => {
      const today = new Date()
      const deadlineDate = new Date(deadline)
      const timeDiff = deadlineDate.getTime() - today.getTime()
      return Math.ceil(timeDiff / (1000 * 3600 * 24))
    }

    const formatDate = (dateStr) => {
      const date = new Date(dateStr)
      return date.toLocaleDateString('zh-CN', { 
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    // 计算属性：处理会议数据
    const processedConferences = computed(() => {
      return conferences.value.map(conf => ({
        ...conf,
        daysLeft: calculateDaysLeft(conf.date),
        formattedDate: formatDate(conf.date),
        status: conf.date.includes('未定期') ? '待定' : ''
      }))
    })

    // 计算属性：按截止日期排序的会议列表
    const sortedConferences = computed(() => {
      return [...processedConferences.value]
        .filter(conf => conf.daysLeft > 0)
        .sort((a, b) => a.daysLeft - b.daysLeft)
    })

    // 计算属性：最近的会议
    const nearestConference = computed(() => {
      return sortedConferences.value[0]
    })

    // 计算属性：按热度排序的研究方向
    const sortedHotTopics = computed(() => {
      return [...hotTopics.value].sort((a, b) => {
        if (a.hot && !b.hot) return -1
        if (!a.hot && b.hot) return 1
        return b.papers - a.papers
      })
    })

    // 更新时间
    const updateTime = () => {
      const now = new Date()
      currentTime.value = now.toLocaleString()
    }

    // 修改初始化论文趋势图方法
    const initPaperChart = async () => {
      await nextTick()
      
      const chartDom = document.getElementById('lineChart')
      if (!chartDom) {
        console.error('Cannot find lineChart element')
        return
      }

      // 确保先销毁已存在的图表实例
      const existingChart = echarts.getInstanceByDom(chartDom)
      if (existingChart) {
        existingChart.dispose()
      }

      const chart = echarts.init(chartDom)
      
      // 生成最近30天的日期
      const dates = Array.from({length: 30}, (_, i) => {
        const date = new Date()
        date.setDate(date.getDate() - (29 - i))
        return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
      })
      
      // 生成符合实际情况的数据（每月5篇以下）
      const generateRealisticData = () => {
        const data = []
        let monthlyCount = 0
        let currentMonth = new Date().getMonth()
        
        for (let i = 0; i < 30; i++) {
          const date = new Date()
          date.setDate(date.getDate() - (29 - i))
          
          if (date.getMonth() !== currentMonth) {
            monthlyCount = 0
            currentMonth = date.getMonth()
          }
          
          const dayValue = monthlyCount < 5 ? Math.random() < 0.2 ? 1 : 0 : 0
          monthlyCount += dayValue
          data.push(dayValue)
        }
        return data
      }

      const data = generateRealisticData()
      
      // 更新统计数据
      todayPapers.value = data[data.length - 1]
      weekPapers.value = data.slice(-7).reduce((sum, val) => sum + val, 0)
      
      const option = {
        backgroundColor: 'transparent',
        grid: {
          top: 40,
          bottom: 40,
          left: 60,
          right: 30,
          containLabel: true
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: (params) => {
            const data = params[0]
            return `${data.name}<br/>发表论文：${data.value} 篇`
          }
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLine: { lineStyle: { color: '#666' } },
          axisTick: { show: false },
          axisLabel: {
            interval: 'auto',
            rotate: 30,
            fontSize: 12,
            margin: 15
          }
        },
        yAxis: {
          type: 'value',
          name: '论文数量',
          nameTextStyle: {
            padding: [0, 0, 0, 40],
            color: '#666'
          },
          max: 2,
          interval: 1,
          axisLine: { lineStyle: { color: '#666' } },
          splitLine: { 
            show: true,
            lineStyle: {
              color: '#eee',
              type: 'dashed'
            }
          }
        },
        series: [{
          name: '论文数量',
          data: data,
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 8,
          lineStyle: {
            width: 3,
            color: '#333'
          },
          itemStyle: {
            color: '#333',
            borderWidth: 2,
            borderColor: '#fff'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
              offset: 0,
              color: 'rgba(51,51,51,0.2)'
            }, {
              offset: 1,
              color: 'rgba(51,51,51,0)'
            }])
          },
          emphasis: {
            itemStyle: {
              color: '#ff4d4f',
              borderColor: '#ff4d4f',
              borderWidth: 3,
              shadowColor: 'rgba(255,77,79,0.3)',
              shadowBlur: 10
            }
          },
          markLine: {
            silent: true,
            lineStyle: {
              color: '#666',
              type: 'dashed'
            },
            data: [{
              yAxis: 1,
              label: {
                formatter: '日均线',
                position: 'start'
              }
            }]
          }
        }]
      }

      try {
        chart.setOption(option)
        charts.push(chart)

        // 添加自动调整大小
        const resizeHandler = () => {
          if (chart && !chart.isDisposed()) {
            chart.resize()
          }
        }

        window.addEventListener('resize', resizeHandler)

        // 清理函数
        onUnmounted(() => {
          window.removeEventListener('resize', resizeHandler)
          if (chart && !chart.isDisposed()) {
            chart.dispose()
          }
        })
      } catch (error) {
        console.error('Error initializing chart:', error)
      }
    }

    // 初始化排名图
    const initRankingChart = () => {
      const chart = echarts.init(document.getElementById('rankingChart'))
      const data = [
        { name: 'MIT', value: 100, itemStyle: { color: '#333' } },
        { name: 'Stanford', value: 98, itemStyle: { color: '#444' } },
        { name: 'CMU', value: 97, itemStyle: { color: '#555' } },
        { name: 'Berkeley', value: 96, itemStyle: { color: '#666' } },
        { name: 'ETH Zurich', value: 95, itemStyle: { color: '#777' } },
        { name: '清华', value: 94, itemStyle: { color: '#888' } },
        { name: '北大', value: 93, itemStyle: { color: '#999' } },
        { name: '武汉大学', value: 89.5, itemStyle: { color: '#ff4d4f' } },
        { name: '上海交大', value: 92, itemStyle: { color: '#aaa' } }
      ]

      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c}'
        },
        series: [{
          type: 'treemap',
          data: data,
          label: {
            show: true,
            formatter: '{b}\n{c}',
            fontSize: 14
          },
          upperLabel: {
            show: true,
            height: 30
          },
          itemStyle: {
            borderColor: '#fff'
          },
          levels: [{
            itemStyle: {
              borderColor: '#777',
              borderWidth: 0,
              gapWidth: 1
            },
            upperLabel: {
              show: false
            }
          }],
          animation: true,
          animationDuration: 2000,
          animationEasing: 'elasticOut'
        }]
      }
      chart.setOption(option)
      charts.push(chart)
    }

    // 修改初始化热门方向图表方法
    const initTopicChart = () => {
      setTimeout(() => {
        const chart = echarts.init(document.getElementById('topicChart'))
        const data = hotTopics.value.map(topic => ({
          name: topic.name,
          value: Math.random() * 100
        }))

        const option = {
          backgroundColor: 'transparent',
          tooltip: {
            trigger: 'item'
          },
          series: [{
            type: 'treemap',
            data: data,
            label: {
              show: true,
              formatter: '{b}',
              fontSize: 12
            },
            breadcrumb: {
              show: false
            },
            itemStyle: {
              borderColor: '#fff',
              borderWidth: 1,
              gapWidth: 1
            },
            levels: [{
              color: ['#f5f5f5', '#e0e0e0', '#d5d5d5', '#cccccc']
            }],
            animation: true,
            animationDuration: 1000,
            animationEasing: 'quinticInOut'
          }]
        }
        
        chart.setOption(option)
        charts.push(chart)
      }, 200)
    }

    const updateRanking = () => {
      // 这里可以根据选择的领域更新排名数据
      initRankingChart()
    }

    // 调整图表初始化延迟
    const initCharts = () => {
      setTimeout(() => {
        initPaperChart();
        setTimeout(() => {
          initTopicChart();
        }, 100);
      }, 200);
    }

    onMounted(async () => {
      updateTime()
      timer = setInterval(updateTime, 1000)
      
      // 确保DOM加载完成后再初始化图表
      await nextTick()
      setTimeout(initPaperChart, 300) // 添加一个小延时确保DOM完全准备好

      window.addEventListener('resize', () => {
        charts.forEach(chart => chart.resize())
      })
    })

    onUnmounted(() => {
      clearInterval(timer)
      charts.forEach(chart => chart.dispose())
    })

    return {
      chartContainer,
      currentTime,
      todayPapers,
      weekPapers,
      selectedArea,
      hotTopics: sortedHotTopics,
      conferences: sortedConferences,
      updateRanking,
      whuRanking,
      nearbyUniversities,
      rankingDetails,
      nearestConference
    }
  }
}
</script>

<style scoped>
.data-view-container {
  min-height: 100vh;
  background: #fff;
  padding: 20px;
  color: #333;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #fafafa;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.header h1 {
  margin: 0;
  font-size: 1.8em;
  color: #333;
  font-weight: 500;
}

.time {
  font-size: 1.1em;
  color: #666;
}

.main-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  min-height: calc(100vh - 100px);
}

.charts-grid {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 1000px;
}

.charts-row {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.chart-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.chart-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.ranking-container {
  height: calc(100% - 80px);
}

.whu-ranking-container {
  display: flex;
  gap: 15px;
  height: 100%;
}

.trend-container {
  display: flex;
  flex-direction: column;
  height: calc(100% - 40px);
  gap: 15px;
}

.trend-header {
  background: linear-gradient(135deg, #f6f6f6 0%, #ffffff 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.trend-stats {
  display: flex;
  justify-content: space-around;
  gap: 20px;
}

.stat-box {
  text-align: center;
  padding: 15px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  flex: 1;
  transition: all 0.3s ease;
}

.stat-box:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-value {
  font-size: 1.6em;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9em;
  color: #666;
}

.topics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 0 10px;
  max-height: calc(100% - 140px);
  overflow-y: auto;
}

.topic-card {
  background: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
  border: 1px solid #eee;
}

.topic-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.topic-card.hot {
  background: linear-gradient(135deg, #fff9f9 0%, #fff 100%);
  border: 1px solid #ffccc7;
}

.topic-content {
  margin-bottom: 10px;
}

.topic-name {
  font-size: 1.2em;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.topic-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.papers-count {
  color: #666;
  font-size: 0.9em;
}

.trend-value {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.9em;
  font-weight: 500;
}

.trend-value.up {
  color: #52c41a;
}

.trend-arrow {
  font-size: 0.8em;
}

.trend-arrow.up {
  transform: translateY(-1px);
}

.topic-progress {
  height: 4px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.topic-card.hot .progress-bar {
  background: linear-gradient(90deg, #ff4d4f 0%, #ff7a45 100%);
}

/* 优化滚动条样式 */
.topics-grid::-webkit-scrollbar {
  width: 6px;
}

.topics-grid::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.topics-grid::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.topics-grid::-webkit-scrollbar-thumb:hover {
  background: #aaa;
}

h3 {
  margin: 0 0 15px 0;
  color: #333;
  text-align: center;
  font-size: 1.2em;
  font-weight: 500;
}

.paper-stats {
  display: flex;
  justify-content: space-around;
  margin: 10px 0;
  padding: 0 20px;
}

.stat-item {
  text-align: center;
  padding: 10px 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.stat-value {
  font-size: 2em;
  font-weight: bold;
  color: #333;
  transition: all 0.3s ease;
}

.stat-item:hover .stat-value {
  transform: scale(1.1);
  color: #ff4d4f;
}

.stat-label {
  font-size: 0.9em;
  color: #666;
  margin-top: 5px;
}

.ranking-header {
  text-align: center;
  padding: 20px;
  background: linear-gradient(to bottom, #fff, #f8f8f8);
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #eee;
}

.ranking-title {
  font-size: 1.8em;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
  letter-spacing: 1px;
}

.ranking-score-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.ranking-score {
  font-size: 3.2em;
  font-weight: bold;
  color: #ff4d4f;
  line-height: 1.2;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.whu-ranking:hover .ranking-score {
  transform: scale(1.1);
  color: #ff4d4f;
}

.ranking-position {
  font-size: 1.2em;
  color: #666;
  font-weight: 500;
}

.ranking-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  padding: 20px;
  background: #fff;
  border-radius: 0 0 8px 8px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  background: #f8f8f8;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.detail-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 3px 8px rgba(0,0,0,0.1);
  background: #f0f0f0;
}

.detail-item .label {
  font-size: 0.9em;
  color: #666;
  margin-bottom: 8px;
}

.detail-item .value {
  font-size: 1.2em;
  font-weight: 600;
  color: #333;
}

.nearby-universities {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  width: 280px;
}

.list-title {
  font-size: 1.2em;
  font-weight: 500;
  color: #333;
  padding: 15px;
  text-align: center;
  border-bottom: 1px solid #eee;
  background: linear-gradient(to bottom, #fff, #f8f8f8);
  border-radius: 8px 8px 0 0;
}

.list-content {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.list-item {
  display: grid;
  grid-template-columns: 40px 1fr 60px;
  align-items: center;
  padding: 12px;
  margin: 5px 0;
  border-radius: 6px;
  transition: all 0.3s ease;
  background: #f8f8f8;
}

.list-item:hover {
  transform: translateX(5px);
  background: #f0f0f0;
}

.list-item.whu {
  background: #fff1f0;
  border: 1px solid #ffccc7;
  position: relative;
  overflow: hidden;
}

.list-item.whu::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent, rgba(255,77,79,0.1), transparent);
  animation: shine 2s infinite;
}

@keyframes shine {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.item-rank {
  font-weight: 600;
  color: #666;
  text-align: center;
}

.item-name {
  font-weight: 500;
  color: #333;
  padding: 0 10px;
}

.item-score {
  font-weight: 500;
  color: #ff4d4f;
  text-align: right;
}

.ranking-selector {
  margin-bottom: 20px;
  text-align: center;
}

.ranking-selector select {
  width: 200px;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #fff;
  color: #333;
  font-size: 1em;
  cursor: pointer;
  transition: all 0.3s ease;
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 1em;
}

.ranking-selector select:hover {
  border-color: #ff4d4f;
  box-shadow: 0 0 0 2px rgba(255,77,79,0.1);
}

.ranking-selector select:focus {
  outline: none;
  border-color: #ff4d4f;
  box-shadow: 0 0 0 3px rgba(255,77,79,0.2);
}

.conference-list {
  overflow-y: auto;
  padding: 0 15px;
  height: calc(100% - 200px);
}

.conference-item {
  background: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 12px;
  border: 1px solid #eee;
  transition: all 0.3s ease;
}

.conference-item:hover {
  transform: translateX(5px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.conference-item.urgent {
  background: #fff1f0;
  border-color: #ffccc7;
}

.conf-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.conf-tier, .conf-track {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  font-weight: 500;
}

.conf-tier {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.conf-track {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.conf-name {
  font-size: 1.2em;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.conf-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conf-date {
  color: #666;
  font-size: 0.95em;
}

.conf-countdown {
  font-weight: 500;
  color: #333;
}

.conf-countdown.urgent {
  color: #ff4d4f;
  font-weight: 600;
}

/* 顶会DDL样式优化 */
.nearest-conference {
  background: linear-gradient(135deg, #fff1f0 0%, #fff 100%);
  border-radius: 12px;
  padding: 20px;
  margin: 0 15px 15px;
  position: relative;
  border: 1px solid #ffccc7;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.1);
}

.nearest-badge {
  position: absolute;
  top: -10px;
  left: 20px;
  background: #ff4d4f;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.9em;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.2);
}

.nearest-content {
  margin-top: 10px;
}

.nearest-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.nearest-name {
  font-size: 1.6em;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.nearest-date {
  font-size: 1.1em;
  color: #666;
  margin-bottom: 5px;
}

.nearest-countdown {
  font-size: 1.2em;
  font-weight: 500;
  color: #333;
}

.nearest-countdown.urgent {
  color: #ff4d4f;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

.chart {
  flex: 1;
  width: 100%;
  min-height: 320px;
  margin-top: 20px;
  position: relative;
}

.chart::before {
  content: '';
  display: block;
  padding-top: 60%;
}

.status-badge {
  background: #f5f5f5;
  color: #666;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.9em;
  font-weight: normal;
}

.nearest-conference .status-badge {
  background: #fff;
  border: 1px solid #ddd;
}

.chart-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chart-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  margin-top: 10px;
  min-height: 360px;
}

.chart {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100% !important;
  height: 100% !important;
}

.paper-trend-card {
  width: 500px;
  height: 540px;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.paper-trend-card h3 {
  margin-bottom: 15px;
}

.paper-stats {
  margin-bottom: 20px;
}
</style> 