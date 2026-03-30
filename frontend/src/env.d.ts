/// <reference types="vite/client" />

// 解决无法找到模块“./App.vue”的声明文件
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
  const component: DefineComponent<{}, {}, any>
  export default component
}
// src/env.d.ts 或 src/router.d.ts
import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    // 这里定义你的 meta 字段
    title?: string
    hideLayout?: boolean
  }
}
// 解决无法找到模块“element-plus/dist/index.css”等样式文件的报错
declare module '*.css'
declare module '*.scss'
declare module '*.sass'

// 1. 告诉 TypeScript：所有以 .vue 结尾的文件，都是 Vue 组件
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 2. 解决 Element Plus 中文包找不到的问题
declare module 'element-plus/dist/locale/zh-cn.mjs'