import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import AOS from 'aos'
import 'aos/dist/aos.css'
import VueKinesis from 'vue-kinesis'

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化时检查登录状态
store.dispatch('checkLogin').then(isLoggedIn => {
  if (isLoggedIn) {
    console.log('用户已登录')
  } else {
    console.log('用户未登录')
  }
})

app.use(router)
app.use(store)
app.use(ElementPlus)
app.use(VueKinesis)
app.use(AOS.init())
app.mount('#app')
