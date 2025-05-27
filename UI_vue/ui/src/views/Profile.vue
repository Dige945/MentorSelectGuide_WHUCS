<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人信息</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="userInfo"
        :rules="rules"
        label-width="100px"
        class="profile-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userInfo.username" disabled />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userInfo.email" />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="userInfo.newPassword"
            type="password"
            show-password
            placeholder="不修改请留空"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="userInfo.confirmPassword"
            type="password"
            show-password
            placeholder="不修改请留空"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="logout-card">
      <el-button type="danger" @click="handleLogout">退出登录</el-button>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

export default {
  name: 'Profile',
  setup() {
    const store = useStore()
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)

    const userInfo = reactive({
      username: '',
      email: '',
      newPassword: '',
      confirmPassword: ''
    })

    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback()
      } else if (value.length < 6) {
        callback(new Error('密码长度不能小于6位'))
      } else {
        if (userInfo.confirmPassword !== '') {
          formRef.value.validateField('confirmPassword')
        }
        callback()
      }
    }

    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback()
      } else if (value !== userInfo.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    const rules = {
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      newPassword: [
        { validator: validatePass, trigger: 'blur' }
      ],
      confirmPassword: [
        { validator: validatePass2, trigger: 'blur' }
      ]
    }    // 获取用户信息
    const fetchUserInfo = async () => {
      try {
        // 从 store 中获取当前用户信息
        const currentUser = store.state.user
        if (!currentUser || !currentUser.username) {
          ElMessage.error('未登录或登录已过期')
          router.push('/login')
          return
        }

        // 直接从 store 中获取用户信息，然后尝试刷新
        userInfo.username = currentUser.username
        userInfo.email = currentUser.email || ''
        userInfo.newPassword = ''
        userInfo.confirmPassword = ''

        // 尝试从服务器获取最新的用户信息
        const res = await request.get('/user/info')
        
        if (res.code === '0' || res.code === 200) {
          const userData = res.data
          userInfo.username = userData.username
          userInfo.email = userData.email || ''
          // 清空密码字段
          userInfo.newPassword = ''
          userInfo.confirmPassword = ''
          
          // 更新 store 和 localStorage 中的用户信息
          store.commit('SET_USER', userData)
          localStorage.setItem('user', JSON.stringify(userData))
        } else {
          console.warn('获取用户信息响应异常:', res)
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        if (error.response?.status === 401) {
          ElMessage.error('未登录或登录已过期')
          router.push('/login')
        } else {
          console.error('获取用户信息详细错误:', error)
        }
      }
    }    // 提交修改
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        loading.value = true

        const currentUser = store.state.user
        if (!currentUser || !currentUser.username) {
          ElMessage.error('未登录或登录已过期')
          router.push('/login')
          return
        }

        const updateData = {
          id: currentUser.id,
          username: userInfo.username,
          email: userInfo.email,
          password: userInfo.newPassword || undefined // 只在有新密码时更新
        }

        const res = await request.post('/user/update', updateData)

        if (res.code === '0' || res.code === 200) {
          ElMessage.success('个人信息更新成功')
          // 清空密码字段
          userInfo.newPassword = ''
          userInfo.confirmPassword = ''
          
          // 如果返回了用户数据，更新 store 中的用户信息
          if (res.data) {
            store.commit('SET_USER', res.data)
            localStorage.setItem('user', JSON.stringify(res.data))
          } else {
            // 如果没有返回用户数据，重新获取用户信息
            fetchUserInfo()
          }
        } else {
          ElMessage.error(res.msg || '更新失败')
        }
      } catch (error) {
        console.error('更新用户信息失败:', error)
        if (error.response?.status === 401) {
          ElMessage.error('未登录或登录已过期')
          router.push('/login')
        } else {
          ElMessage.error(error.response?.data?.msg || error.message || '更新失败')
        }
      } finally {
        loading.value = false
      }
    }

    const handleLogout = () => {
      store.dispatch('logout')
      router.push('/login')
    }

    onMounted(() => {
      fetchUserInfo()
    })

    return {
      formRef,
      userInfo,
      rules,
      loading,
      handleSubmit,
      handleLogout
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.profile-form {
  margin-top: 20px;
}

.logout-card {
  text-align: center;
  padding: 20px;
}

.logout-card .el-button {
  width: 200px;
}
</style> 