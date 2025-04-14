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
        if (res.code === '0') {
          commit('SET_USER', res.data)
          commit('SET_TOKEN', res.data.token)
          localStorage.setItem('token', res.data.token)
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
      localStorage.removeItem('token')
    },
    async checkLogin({ commit, state }) {
      if (state.user) {
        console.log('User already exists:', state.user)
        return true
      }
      
      const token = localStorage.getItem('token')
      if (token) {
        try {
          const res = await request.get('/user/info')
          if (res.code === '0') {
            console.log('User info from server:', res.data)
            commit('SET_USER', res.data)
            commit('SET_TOKEN', token)
            return true
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          localStorage.removeItem('token')
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