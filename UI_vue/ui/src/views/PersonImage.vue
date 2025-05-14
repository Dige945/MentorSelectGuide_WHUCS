<template>
  <div class="cosmic-profile-container">
    <!-- Background effect -->
    <div class="cosmic-particles"></div>
    
    <!-- Orbit container with all the interactive elements -->
    <div class="orbit-container">
      <!-- Center user icon with glowing effect -->
      <div class="user-icon-container">
        <div class="user-icon-glow"></div>
        <div class="user-icon">
          <img v-if="userAvatar" :src="userAvatar" alt="User Avatar" />
          <div v-else class="default-avatar">
            <el-icon class="avatar-icon"><User /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- Major category tags surrounding the center icon -->
      <div 
        v-for="(category, index) in categories" 
        :key="category.id"
        class="category-tag"
        :class="{ 'active': category.active }"
        :style="positionCategory(index, categories.length)"
        @click="toggleCategory(category.id)"
      >
        <div class="category-icon">
          <el-icon><component :is="category.icon" /></el-icon>
        </div>
        <span>{{ category.name }}</span>
      </div>
      
      <!-- Sub-tags for each major category -->
      <div 
        v-for="subtag in visibleSubtags" 
        :key="subtag.id"
        class="subtag"
        :class="{ 'active': subtag.selected }"
        :style="positionSubtag(subtag)"
        @click="toggleSubtag(subtag.id)"
      >
        {{ subtag.name }}
      </div>
    </div>
    
    <!-- AI Profile assistant -->
    <div class="ai-profile-section">
      <div class="ai-header">
        <el-icon class="ai-icon"><Avatar /></el-icon>
        <span>AI 简介助手</span>
      </div>
      <textarea 
        v-model="userProfile" 
        placeholder="在这里编辑您的个人简介..." 
        class="profile-textarea"
      ></textarea>
      <button class="generate-button" @click="generateAIProfile">
        <el-icon><Magic /></el-icon>
        AI生成简介
      </button>
    </div>
    
    <!-- Selected tags summary -->
    <div class="selected-tags-section">
      <h3>已选标签</h3>
      <div class="selected-tags-container">
        <div 
          v-for="tag in selectedTags" 
          :key="tag.id" 
          class="selected-tag"
        >
          {{ tag.name }}
          <span class="remove-tag" @click="toggleSubtag(tag.id)">×</span>
        </div>
      </div>
    </div>
    
    <!-- Save button -->
    <button class="save-button">保存个人设置</button>
  </div>
</template>

<script>
import { User, Collection, School, Monitor, Briefcase, Cpu, Avatar, Magic } from '@element-plus/icons-vue'

export default {
  name: 'PersonImage',
  components: {
    User, Collection, School, Monitor, Briefcase, Cpu, Avatar, Magic
  },
  data() {
    return {
      userAvatar: null,
      userProfile: '',
      activeCategoryId: null,
      categories: [
        { 
          id: 'research', 
          name: '科研方向', 
          icon: 'Collection', 
          active: false,
          subtags: [
            { id: 'r1', name: '人工智能', selected: false, categoryId: 'research' },
            { id: 'r2', name: '大数据分析', selected: false, categoryId: 'research' },
            { id: 'r3', name: '网络安全', selected: false, categoryId: 'research' },
            { id: 'r4', name: '云计算', selected: false, categoryId: 'research' },
            { id: 'r5', name: '区块链', selected: false, categoryId: 'research' },
            { id: 'r6', name: '嵌入式系统', selected: false, categoryId: 'research' },
            { id: 'r7', name: '计算机视觉', selected: false, categoryId: 'research' },
            { id: 'r8', name: '自然语言处理', selected: false, categoryId: 'research' }
          ]
        },
        { 
          id: 'mentor', 
          name: '导师标签', 
          icon: 'School', 
          active: false,
          subtags: [
            { id: 'm1', name: '严谨', selected: false, categoryId: 'mentor' },
            { id: 'm2', name: '开放', selected: false, categoryId: 'mentor' },
            { id: 'm3', name: '负责任', selected: false, categoryId: 'mentor' },
            { id: 'm4', name: '国际化视野', selected: false, categoryId: 'mentor' },
            { id: 'm5', name: '行业资源丰富', selected: false, categoryId: 'mentor' },
            { id: 'm6', name: '学术大牛', selected: false, categoryId: 'mentor' }
          ]
        },
        { 
          id: 'lab', 
          name: '实验室氛围', 
          icon: 'Monitor', 
          active: false,
          subtags: [
            { id: 'l1', name: '协作型', selected: false, categoryId: 'lab' },
            { id: 'l2', name: '自由创新', selected: false, categoryId: 'lab' },
            { id: 'l3', name: '结构化', selected: false, categoryId: 'lab' },
            { id: 'l4', name: '竞争激励', selected: false, categoryId: 'lab' },
            { id: 'l5', name: '学术导向', selected: false, categoryId: 'lab' },
            { id: 'l6', name: '产业导向', selected: false, categoryId: 'lab' }
          ]
        },
        { 
          id: 'career', 
          name: '职业规划', 
          icon: 'Briefcase', 
          active: false,
          subtags: [
            { id: 'c1', name: '学术研究', selected: false, categoryId: 'career' },
            { id: 'c2', name: '工业界', selected: false, categoryId: 'career' },
            { id: 'c3', name: '创业', selected: false, categoryId: 'career' },
            { id: 'c4', name: '继续深造', selected: false, categoryId: 'career' },
            { id: 'c5', name: '跨专业发展', selected: false, categoryId: 'career' }
          ]
        },
        { 
          id: 'style', 
          name: '学习风格', 
          icon: 'Cpu', 
          active: false,
          subtags: [
            { id: 's1', name: '自主学习', selected: false, categoryId: 'style' },
            { id: 's2', name: '小组协作', selected: false, categoryId: 'style' },
            { id: 's3', name: '实践动手', selected: false, categoryId: 'style' },
            { id: 's4', name: '理论钻研', selected: false, categoryId: 'style' },
            { id: 's5', name: '创新探索', selected: false, categoryId: 'style' }
          ]
        }
      ]
    }
  },
  computed: {
    visibleSubtags() {
      if (!this.activeCategoryId) return [];
      const category = this.categories.find(c => c.id === this.activeCategoryId);
      return category ? category.subtags : [];
    },
    selectedTags() {
      let selected = [];
      this.categories.forEach(category => {
        category.subtags.forEach(subtag => {
          if (subtag.selected) {
            selected.push(subtag);
          }
        });
      });
      return selected;
    }
  },
  methods: {
    toggleCategory(categoryId) {
      // If clicking the same category, close it
      if (this.activeCategoryId === categoryId) {
        this.activeCategoryId = null;
      } else {
        this.activeCategoryId = categoryId;
      }
      
      // Update active state for visual effect
      this.categories.forEach(category => {
        category.active = category.id === this.activeCategoryId;
      });
    },
    toggleSubtag(subtagId) {
      this.categories.forEach(category => {
        category.subtags.forEach(subtag => {
          if (subtag.id === subtagId) {
            subtag.selected = !subtag.selected;
          }
        });
      });
    },
    positionCategory(index, total) {
      // Adjusted radius for better spacing
      const radius = 230; // Distance from center
      const angle = (index * (2 * Math.PI / total)) - Math.PI/2; // Starting from top
      const x = radius * Math.cos(angle);
      const y = radius * Math.sin(angle);
      
      return {
        top: `calc(50% + ${y}px)`,
        left: `calc(50% + ${x}px)`,
        transform: 'translate(-50%, -50%)'
      };
    },
    positionSubtag(subtag) {
      const category = this.categories.find(c => c.id === subtag.categoryId);
      const categoryIndex = this.categories.findIndex(c => c.id === subtag.categoryId);
      const subtagIndex = category.subtags.findIndex(s => s.id === subtag.id);
      const totalSubtags = category.subtags.length;
      
      // Base position of the category
      const categoryRadius = 230;
      const categoryAngle = (categoryIndex * (2 * Math.PI / this.categories.length)) - Math.PI/2;
      const categoryX = categoryRadius * Math.cos(categoryAngle);
      const categoryY = categoryRadius * Math.sin(categoryAngle);
      
      // Position around the category
      const subtagRadius = 120;
      // Distribute subtags in a wider arc
      const spreadAngle = Math.PI * 0.7;
      const subtagAngle = categoryAngle + ((subtagIndex - (totalSubtags - 1) / 2) * (spreadAngle / (totalSubtags || 1)));
      
      const x = categoryX + subtagRadius * Math.cos(subtagAngle);
      const y = categoryY + subtagRadius * Math.sin(subtagAngle);
      
      return {
        top: `calc(50% + ${y}px)`,
        left: `calc(50% + ${x}px)`,
        transform: 'translate(-50%, -50%)'
      };
    },
    generateAIProfile() {
      // Simulating AI generation
      const selectedTagNames = this.selectedTags.map(tag => tag.name).join('、');
      
      if (selectedTagNames) {
        this.userProfile = `我是一名对${selectedTagNames}等领域感兴趣的计算机科学学生。我期待能在这些方向上深入学习和研究，寻找能够指导我发挥潜力的导师和实验室环境。`;
      } else {
        this.userProfile = "请至少选择一个标签，AI将根据您的选择生成个人简介。";
      }
    }
  }
}
</script>

<style scoped>
.cosmic-profile-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  overflow: hidden;
  background-color: #0a0a2a;
  color: #e0e7ff;
  font-family: 'BlinkMacSystemFont', 'Segoe UI', Roboto, sans-serif;
  z-index: 0; /* Keep below the header */
  background-image: radial-gradient(rgba(255, 255, 255, 0.05) 1px, transparent 1px);
  background-size: 30px 30px;
}

.cosmic-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(#2a4b8d 1px, transparent 1px), 
                   radial-gradient(#2a4b8d 1px, transparent 1px);
  background-size: 50px 50px;
  background-position: 0 0, 25px 25px;
  animation: particlesMove 20s infinite linear;
}

@keyframes particlesMove {
  0% {
    background-position: 0 0, 25px 25px;
  }
  100% {
    background-position: 50px 50px, 75px 75px;
  }
}

.orbit-container {
  position: relative;
  width: 700px;
  height: 700px;
  margin: 120px auto 150px;
  pointer-events: none;
}

.user-icon-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
}

.user-icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(66, 153, 225, 0.2);
  filter: blur(15px);
  animation: pulse 4s infinite alternate;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    background: rgba(66, 153, 225, 0.2);
  }
  100% {
    transform: translate(-50%, -50%) scale(1.2);
    background: rgba(99, 179, 237, 0.3);
  }
}

.user-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #1a365d;
  border: 3px solid #4299e1;
  box-shadow: 0 0 15px rgba(66, 153, 225, 0.5),
             0 0 30px rgba(66, 153, 225, 0.3),
             0 0 45px rgba(66, 153, 225, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.user-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.avatar-icon {
  font-size: 60px;
  color: #4299e1;
}

.category-tag {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px; /* Increased from 90px */
  height: 100px; /* Increased from 90px */
  border-radius: 50%;
  background: rgba(26, 54, 93, 0.8);
  border: 2px solid #63b3ed;
  color: #e0e7ff;
  text-align: center;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  pointer-events: auto;
  z-index: 5;
  box-shadow: 0 0 10px rgba(99, 179, 237, 0.3);
}

.category-tag:hover {
  background: rgba(44, 82, 130, 0.9);
  transform: translate(-50%, -50%) scale(1.1);
}

.category-tag.active {
  background: rgba(66, 153, 225, 0.8);
  border-color: #bee3f8;
  box-shadow: 0 0 15px rgba(99, 179, 237, 0.6);
}

.category-icon {
  font-size: 1.8rem;
  margin-bottom: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.category-icon .el-icon {
  font-size: 2rem;
}

.subtag {
  position: absolute;
  padding: 8px 15px;
  background: rgba(26, 54, 93, 0.7);
  border: 1px solid #4299e1;
  border-radius: 20px;
  color: #e0e7ff;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s ease;
  pointer-events: auto;
  z-index: 4;
  box-shadow: 0 0 5px rgba(66, 153, 225, 0.2);
  white-space: nowrap;
}

.subtag:hover {
  background: rgba(44, 82, 130, 0.8);
  transform: translate(-50%, -50%) scale(1.1);
}

.subtag.active {
  background: rgba(49, 130, 206, 0.8);
  border-color: #bee3f8;
  box-shadow: 0 0 10px rgba(99, 179, 237, 0.5);
}

.ai-profile-section {
  position: relative;
  width: 100%;
  max-width: 600px;
  background: rgba(26, 54, 93, 0.7);
  border: 2px solid #4299e1;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 30px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 20px rgba(66, 153, 225, 0.2);
}

.ai-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  color: #90cdf4;
  font-weight: 600;
}

.ai-icon {
  margin-right: 10px;
  font-size: 1.3rem;
}

.profile-textarea {
  width: 100%;
  min-height: 120px;
  background: rgba(17, 29, 57, 0.7);
  border: 1px solid #63b3ed;
  border-radius: 5px;
  color: #e2e8f0;
  padding: 15px;
  font-size: 0.9rem;
  resize: vertical;
  margin-bottom: 15px;
}

.generate-button {
  background: linear-gradient(135deg, #4299e1, #3182ce);
  color: white;
  border: none;
  border-radius: 5px;
  padding: 10px 15px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.generate-button:hover {
  background: linear-gradient(135deg, #3182ce, #2c5282);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(49, 130, 206, 0.3);
}

.selected-tags-section {
  width: 100%;
  max-width: 600px;
  background: rgba(26, 54, 93, 0.7);
  border: 2px solid #4299e1;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 30px;
  backdrop-filter: blur(5px);
}

.selected-tags-section h3 {
  color: #90cdf4;
  margin-bottom: 15px;
  font-weight: 600;
}

.selected-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.selected-tag {
  background: rgba(49, 130, 206, 0.7);
  border: 1px solid #bee3f8;
  border-radius: 20px;
  padding: 5px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
}

.remove-tag {
  cursor: pointer;
  font-weight: bold;
  font-size: 1.1rem;
  margin-left: 5px;
}

.save-button {
  background: linear-gradient(135deg, #00b0ff, #0072ff);
  color: white;
  border: none;
  border-radius: 30px;
  padding: 12px 40px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  box-shadow: 0 0 15px rgba(0, 179, 255, 0.3);
  margin-bottom: 40px;
}

.save-button:hover {
  background: linear-gradient(135deg, #0072ff, #00c6ff);
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0, 179, 255, 0.4);
}

/* Responsive styles */
@media (max-width: 768px) {
  .category-tag {
    width: 70px;
    height: 70px;
    font-size: 0.7rem;
  }
  
  .category-icon {
    font-size: 1.5rem;
  }
  
  .subtag {
    padding: 6px 12px;
    font-size: 0.7rem;
  }
  
  .user-icon {
    width: 100px;
    height: 100px;
  }
  
  .user-icon-glow {
    width: 130px;
    height: 130px;
  }
}
</style>
