import { createStore } from 'vuex'
import request from '@/utils/request'

export default createStore({
  state: {
    user: null,
    token: null
  },
  mutations: {
    SET_USER(state, user) {
      console.log('Setting user:', user)
      state.user = user
    },
    SET_TOKEN(state, token) {
      state.token = token
    },
    CLEAR_USER(state) {
      state.user = null
      state.token = null
    }
  },
  actions: {
    async login({ commit }, userInfo) {
      try {
        const res = await request.post('/user/login', userInfo)
        if (res.code === '0' || res.code === 200) {
          // 设置用户信息到 store
          commit('SET_USER', res.data)
          
          // 将用户信息保存到 localStorage
          localStorage.setItem('user', JSON.stringify(res.data))
          
          return true
        }
        return false
      } catch (error) {
        console.error('登录失败:', error)
        return false
      }
    },
    logout({ commit }) {
      commit('CLEAR_USER')
      localStorage.removeItem('user')
    },
    async checkLogin({ commit, state }) {
      if (state.user) {
        console.log('User already exists:', state.user)
        return true
      }
      
      // 从 localStorage 获取用户信息
      const userJson = localStorage.getItem('user')
      if (userJson) {
        try {
          // 解析存储的用户信息
          const userFromStorage = JSON.parse(userJson)
          if (userFromStorage && userFromStorage.username) {
            console.log('User info from local storage:', userFromStorage)
            
            // 使用本地存储的用户信息先恢复state，然后再尝试从服务器刷新
            commit('SET_USER', userFromStorage)
            
            try {
              // 尝试从服务器获取最新的用户信息
              const res = await request.get('/user/info')
              if (res.code === '0' || res.code === 200) {
                console.log('User info from server:', res.data)
                commit('SET_USER', res.data)
                // 更新本地存储
                localStorage.setItem('user', JSON.stringify(res.data))
              }
            } catch (serverError) {
              console.warn('获取服务器用户信息失败，使用本地缓存:', serverError)
            }
            
            return true
          }
        } catch (parseError) {
          console.error('解析本地用户信息失败:', parseError)
          localStorage.removeItem('user')
        }
      }
      return false
    }
  },
  getters: {
    isLoggedIn: state => {
      console.log('Current user state:', state.user)
      return !!state.user
    },
    currentUser: state => state.user
  }
}) 