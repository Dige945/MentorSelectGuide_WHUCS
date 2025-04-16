<template>
  <div class="about-container">
    <el-card class="about-card">
      <template #header>
        <div class="card-header">
          <h2>关于我们</h2>
        </div>
      </template>

      <div class="about-content">
        <div class="section">
          <h3>项目介绍</h3>
          <p>武汉大学计算机学院导师选择指南系统是一个为计算机学院学生提供导师选择参考的智能平台。本系统整合了学院导师信息，通过智能推荐算法，帮助学生找到最适合自己的导师。</p>
        </div>

        <div class="section">
          <h3>主要功能</h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><User /></el-icon>
                <h4>导师信息</h4>
                <p>查看学院导师的详细信息，包括研究方向、科研成果等</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><Search /></el-icon>
                <h4>智能推荐</h4>
                <p>基于AI算法，根据学生兴趣和需求推荐合适的导师</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="feature-item">
                <el-icon><Document /></el-icon>
                <h4>研究方向</h4>
                <p>了解学院各个研究方向的最新动态和成果</p>
              </div>
            </el-col>
          </el-row>
        </div>

        <div class="section">
          <h3>联系我们</h3>
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

<script>
import { User, Search, Document, Location, Phone, Message } from '@element-plus/icons-vue'
export default {
  name: 'About',
  components: {
    User,
    Search,
    Document,
    Location,
    Phone,
    Message
  }
}
</script>

<style scoped>
.about-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.about-card {
  margin-bottom: 20px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.about-content {
  padding: 20px;
}

.section {
  margin-bottom: 40px;
}

.section h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 20px;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 10px;
}

.section p {
  color: #606266;
  line-height: 1.8;
  margin-bottom: 15px;
}

.feature-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  height: 100%;
  transition: all 0.3s;
}

.feature-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.feature-item .el-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 15px;
}

.feature-item h4 {
  margin: 10px 0;
  color: #303133;
}

.feature-item p {
  color: #606266;
  font-size: 14px;
}

.contact-info {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.contact-info p {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.contact-info .el-icon {
  margin-right: 10px;
  color: #409EFF;
}

.qr-code {
  text-align: center;
  padding: 20px;
}

.qr-code img {
  width: 150px;
  height: 150px;
  margin-bottom: 10px;
}

.qr-code p {
  color: #606266;
  font-size: 14px;
}
</style> 