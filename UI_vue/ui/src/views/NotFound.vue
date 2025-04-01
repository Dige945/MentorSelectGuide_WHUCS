<template>
  <div class="not-found">
    <img src="../assets/404.png" alt="404" class="not-found-image">
    <h1>页面未找到</h1>
    <p>抱歉，您访问的页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script>
export default {
  name: 'NotFound'
}
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  text-align: center;
}

.not-found-image {
  max-width: 300px;
  margin-bottom: 30px;
}

h1 {
  font-size: 28px;
  margin-bottom: 15px;
  color: #303133;
}

p {
  color: #606266;
  margin-bottom: 30px;
  font-size: 16px;
}
</style> 