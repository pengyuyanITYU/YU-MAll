/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const component: DefineComponent<{}, {}, any>
  export default component
}

import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    hideLayout?: boolean
    hideNavBar?: boolean
    requiresAuth?: boolean
  }
}

declare module '*.css'
declare module '*.scss'
declare module '*.sass'
declare module 'element-plus/dist/locale/zh-cn.mjs'
