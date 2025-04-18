<template>
  <div class="about-container">
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <h2 class="title">关于我们</h2>
        </div>
      </template>

      <div class="about-content">
        <div class="section fade-in">
          <h3 class="section-title">项目介绍</h3>
          <p class="section-text">武汉大学计算机学院导师选择指南系统是一个为计算机学院学生提供导师选择参考的智能平台。本系统整合了学院导师信息，通过智能推荐算法，帮助学生找到最适合自己的导师。</p>
        </div>

        <div class="section fade-in">
          <h3 class="section-title">主要功能</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="feature-item">
                <div class="icon-wrapper">
                  <el-icon><User /></el-icon>
                </div>
                <h4>导师信息</h4>
                <p>查看学院导师的详细信息，包括研究方向、科研成果等</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <div class="icon-wrapper">
                  <el-icon><Search /></el-icon>
                </div>
                <h4>智能推荐</h4>
                <p>基于AI算法，根据学生兴趣和需求推荐合适的导师</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <div class="icon-wrapper">
                  <el-icon><Document /></el-icon>
                </div>
                <h4>研究方向</h4>
                <p>了解学院各个研究方向的最新动态和成果</p>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="section fade-in">
          <h3 class="section-title">联系我们</h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="contact-info">
                <p><el-icon><Location /></el-icon> 地址：湖北省武汉市武汉大学计算机学院</p>
                <p><el-icon><Phone /></el-icon> 电话：027-6877XXXX</p>
                <p><el-icon><Message /></el-icon> 邮箱：mentorguide@whu.edu.cn</p>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="qr-code">
                <img src="@/assets/qrcode.png" alt="微信公众号二维码">
                <p>扫描关注我们的微信公众号</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { User, Search, Document, Location, Phone, Message } from '@element-plus/icons-vue'
</script>

<style scoped>
.about-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
}

.about-card {
  background-color: #ffffff;
  border: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.about-card:hover {
  transform: translateY(-5px);
}

.card-header {
  text-align: center;
  padding: 20px 0;
}

.title {
  margin: 0;
  font-size: 2.5rem;
  color: #1a1a1a;
  position: relative;
  display: inline-block;
}

.title::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background-color: #1a1a1a;
}

.about-content {
  padding: 30px;
}

.section {
  margin-bottom: 50px;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.8s ease forwards;
}

.section:nth-child(2) {
  animation-delay: 0.2s;
}

.section:nth-child(3) {
  animation-delay: 0.4s;
}

.section-title {
  color: #1a1a1a;
  margin-bottom: 25px;
  font-size: 1.8rem;
  position: relative;
  padding-bottom: 10px;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 50px;
  height: 2px;
  background-color: #1a1a1a;
}

.section-text {
  color: #4a4a4a;
  line-height: 1.8;
  font-size: 1.1rem;
}

.feature-item {
  text-align: center;
  padding: 30px;
  background: #ffffff;
  border-radius: 12px;
  height: 100%;
  transition: all 0.4s ease;
  border: 1px solid #e0e0e0;
}

.feature-item:hover {
  transform: translateY(-10px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  border-color: #1a1a1a;
}

.icon-wrapper {
  width: 60px;
  height: 60px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.feature-item:hover .icon-wrapper {
  background-color: #1a1a1a;
}

.feature-item .el-icon {
  font-size: 30px;
  color: #1a1a1a;
  transition: all 0.3s ease;
}

.feature-item:hover .el-icon {
  color: #ffffff;
}

.feature-item h4 {
  margin: 15px 0;
  color: #1a1a1a;
  font-size: 1.2rem;
}

.feature-item p {
  color: #666666;
  font-size: 0.95rem;
  line-height: 1.6;
}

.contact-info {
  padding: 30px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
}

.contact-info p {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  color: #4a4a4a;
  font-size: 1.1rem;
}

.contact-info .el-icon {
  margin-right: 15px;
  color: #1a1a1a;
  font-size: 1.2rem;
}

.qr-code {
  text-align: center;
  padding: 30px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
}

.qr-code img {
  width: 180px;
  height: 180px;
  margin-bottom: 15px;
  transition: transform 0.3s ease;
}

.qr-code img:hover {
  transform: scale(1.05);
}

.qr-code p {
  color: #666666;
  font-size: 0.95rem;
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

@media (max-width: 768px) {
  .about-container {
    padding: 10px;
  }
  
  .title {
    font-size: 2rem;
  }
  
  .section-title {
    font-size: 1.5rem;
  }
  
  .feature-item {
    margin-bottom: 20px;
  }
}
</style> 