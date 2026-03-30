# Frontend

用户端 Vue 3 前端，电商购物流程（浏览、搜索、购物车、下单、支付、评论）。

## STRUCTURE

```
src/
├── api/           # API 请求封装
├── components/    # 公共组件
├── views/         # 页面（cart, items, order, pay, user 等）
├── router/        # 路由配置
├── stores/        # Pinia 状态
└── utils/         # request.ts 等
```

## VUE PATTERNS

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
const loading = ref(false)
onMounted(() => { /* 初始化 */ })
</script>
<template><!-- Element Plus --></template>
<style scoped lang="scss"></style>
```

- 路由守卫：`meta.requiresAuth: true` 需登录
- 懒加载：`const Home = () => import('../views/items/Home.vue')`

## API CALLS

```typescript
// utils/request.ts: baseURL='/api', Authorization header
import { request } from '@/utils/request'
export function list(params: any) {
  return request({ url: '/items/list', method: 'get', params })
}
// 调用
const res = await list({ pageNo: 1, pageSize: 10 })
if (res.code === 200) { /* 成功 */ }
```

401 自动清 token 并跳转登录页。

## STATE MANAGEMENT

```typescript
export const useUserStore = defineStore('userInfo', () => {
  const userInfo = reactive({ username: '', userId: 0 })
  const updateUserInfo = (data) => Object.assign(userInfo, data)
  return { userInfo, updateUserInfo }
}, { persist: { storage: sessionStorage } })
```

持久化：`pinia-plugin-persistedstate`，用户信息存 sessionStorage。
