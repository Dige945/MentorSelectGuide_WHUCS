<script>
import { mapActions } from 'vuex'
import request from '@/utils/request'

export default {
  name: "ALogin",
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      loading: false
    }
  },
  methods: {
    ...mapActions(['login']),
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        this.$message.warning('请输入用户名和密码')
        return
      }
      
      this.loading = true
      try {
        const res = await request.post('/user/login', this.form)
        if (res.code === '0') {
          // 直接设置用户信息到store
          this.$store.commit('SET_USER', res.data)
          this.$store.commit('SET_TOKEN', res.data.token)
          localStorage.setItem('token', res.data.token)
          
          this.$message.success('登录成功')
          const redirect = this.$route.query.redirect || '/'
          this.$router.push(redirect)
        } else {
          this.$message.error(res.msg || '用户名或密码错误')
        }
      } catch (error) {
        this.$message.error('登录失败：' + error.message)
      } finally {
        this.loading = false
      }
    },
    goToRegister() {
      this.$router.push('/register')
    }
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <h2>欢迎登录</h2>
        <p>Welcome Back</p>
      </div>
      <el-form ref="form" :model="form" size="large" class="login-form">
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
        <el-form-item>
          <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？<span @click="goToRegister">立即注册</span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7eb 100%);
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('@/assets/login-bg.jpg') center/cover no-repeat;
  opacity: 0.1;
  z-index: 0;
}

.login-box {
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

.login-title {
  text-align: center;
  margin-bottom: 40px;
}

.login-title h2 {
  color: #1a1a1a;
  font-size: 2.2rem;
  margin-bottom: 8px;
  font-weight: 600;
  letter-spacing: 1px;
}

.login-title p {
  color: #666;
  font-size: 1rem;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.login-form {
  margin-top: 20px;
}

.login-button {
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

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  background: #333;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 0.9rem;
}

.register-link span {
  color: #1a1a1a;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
  padding-bottom: 2px;
}

.register-link span::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 1px;
  background: #1a1a1a;
  transition: width 0.3s ease;
}

.register-link span:hover::after {
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
  .login-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .login-title h2 {
    font-size: 1.8rem;
  }
}
</style>