import axios from 'axios'
import { ElMessage } from 'element-plus'

// 1. 定义一个变量，用于防止多个 401 错误同时触发弹窗
let isRelogging = false;

export const request = axios.create({
    baseURL: '/api',
    timeout: 60000
})

// 请求拦截器
request.interceptors.request.use((config) => {
    const loginUser = localStorage.getItem("Authorization")
    if (loginUser) {
        // 注意：通常后端需要 Bearer 前缀，如果你的后端不需要请忽略
        // config.headers['Authorization'] = `Bearer ${loginUser}` 
        config.headers['Authorization'] = loginUser
    }
    return config
}, (error) => {
    return Promise.reject(error)
})

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        return response.data
    },
    (error) => {
        // 处理 HTTP 状态码错误
        if (error.response && error.response.status === 401) {
            
            // 2. 关键逻辑：如果当前已经是登录页，或者已经在重定向中，则不再执行后续逻辑
            // 防止在登录页调用某些接口失败导致死循环，也防止并发请求导致多次弹窗
            if (window.location.pathname.includes('/login') || isRelogging) {
                return Promise.reject(error)
            }

            // 3. 开启锁，防止后续并发请求再次进入
            isRelogging = true;

            // 4. 清除 Token
            localStorage.removeItem('Authorization')

            // 5. 提示用户 (只提示一次)
            ElMessage.error('登录状态已过期，请重新登录')

            // 6. 跳转
            setTimeout(() => {
                // 使用 location.href 跳转会刷新页面，重置 js 内存，isRelogging 自然也会重置
                window.location.href = '/login'
            }, 1000)

            return Promise.reject(error)
        }
        // 
       

        return Promise.reject(error)
    
    }
)