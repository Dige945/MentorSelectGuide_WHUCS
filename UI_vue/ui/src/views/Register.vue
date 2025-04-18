<script>
import request from "@/utils/request";
export default {
  name: "ARegister",
  data(){
    return {
      form:{},
      loading: false
    }
  },
  methods:{
    register(){
      if(this.form.password !== this.form.conforms){
        this.$message({
          type: 'error',
          message: '两次密码不一致'
        });
        return;
      }
      this.loading = true;
      request.post("/user/register",this.form).then(res=>{
        if(res.code === '0'){
          this.$message({
            type: 'success',
            message: '注册成功'
          })
          this.$router.push('/login')
        }else{
          this.$message({
            type: 'error',
            message: res.msg
          });
        }
      }).finally(() => {
        this.loading = false;
      })
    },
    goToLogin() {
      this.$router.push('/login')
    }
  }
}
</script>

<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-title">
        <h2>欢迎注册</h2>
        <p>Create Account</p>
      </div>
      <el-form ref="form" :model="form" size="large" class="register-form">
        <el-form-item prop="username">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
              v-model="form.password"
              show-password
              placeholder="请输入密码"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="conforms">
          <el-input
              v-model="form.conforms"
              show-password
              placeholder="请再次确认密码"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              class="register-button"
              :loading="loading"
              @click="register"
          >
            注册
          </el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<span @click="goToLogin">立即登录</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.register-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
  z-index: 0;
}

.register-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  animation: fadeIn 0.5s ease;
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.register-title {
  text-align: center;
  margin-bottom: 40px;
}

.register-title h2 {
  color: #1a1a1a;
  font-size: 2.2rem;
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: 1px;
}

.register-title p {
  color: #666;
  font-size: 1rem;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.register-form {
  margin-top: 20px;
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 1rem;
  border-radius: 22px;
  background: #1a1a1a;
  border: none;
  transition: all 0.3s ease;
  letter-spacing: 1px;
  font-weight: 500;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  background: #333;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 0.9rem;
}

.login-link span {
  color: #1a1a1a;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  padding-bottom: 2px;
}

.login-link span::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 1px;
  background: #1a1a1a;
  transition: width 0.3s ease;
}

.login-link span:hover::after {
  width: 100%;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-input__wrapper) {
  border-radius: 22px;
  padding: 8px 15px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: rgba(0, 0, 0, 0.3);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #1a1a1a;
  box-shadow: 0 0 0 1px #1a1a1a;
}

:deep(.el-input__inner) {
  height: 44px;
  line-height: 44px;
  color: #1a1a1a;
}

:deep(.el-input__prefix) {
  color: #666;
}

:deep(.el-input__prefix-inner) {
  margin-right: 8px;
}

@media (max-width: 480px) {
  .register-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .register-title h2 {
    font-size: 1.8rem;
  }
}
</style>
