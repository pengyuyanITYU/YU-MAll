import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// @ts-ignore // 如果这个中文包报错，可以暂时忽略，或者在 env.d.ts 里声明一下
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import './assets/main.css' // 确保路径正确，有了 env.d.ts 就不会报错了
import 'element-plus/theme-chalk/dark/css-vars.css'
import { createPinia } from 'pinia'
// 1. 引入持久化插件
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
// 1. 引入 Naive UI

import * as ionicons5 from '@vicons/ionicons5'
const app = createApp(App)
const pinia = createPinia()

// 2. 将插件注册到 pinia 实例上
pinia.use(piniaPluginPersistedstate)
app.use(pinia)
app.use(router)
app.use(ElementPlus as any, { locale: zhCn })

// 🌟 修复循环注册图标报错的关键点：加一个断言 (as any)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component as any) 
}

app.mount('#app')