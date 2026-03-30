<template>
  <div class="login-container">
    <!-- 动态背景装饰 -->
    <div class="circle circle-1"></div>
    <div class="circle circle-2"></div>

    <el-card class="login-card animate__animated">
      <!-- 品牌 Logo 区域 -->
      <div class="brand-header">
        <div class="logo-box">
          <el-icon :size="32" color="#fff"><Shop /></el-icon>
        </div>
        <h2>欢迎回来</h2>
        <p>登录 <span class="brand-text">YU-Mall</span> 开启精彩生活</p>
      </div>
      
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        size="large"
        class="login-form"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名 / 手机号" 
            :prefix-icon="User" 
            class="custom-input"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            :prefix-icon="Lock" 
            show-password 
            class="custom-input"
            @keyup.enter="handleLogin(formRef)"
          />
        </el-form-item>

        <!-- 辅助功能区 -->
        <div class="form-options">
          <el-checkbox v-model="form.remember" label="记住我" size="default" />
          <el-link type="primary" :underline="false" class="forgot-pwd">忘记密码?</el-link>
        </div>

        <!-- 登录按钮 -->
        <el-form-item>
          <el-button 
            type="primary" 
            class="login-btn" 
            @click="handleLogin(formRef)" 
            :loading="loading"
            round
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部操作 -->
      <div class="login-footer">
        <div class="divider">
          <span>其他方式</span>
        </div>
        <div class="action-links">
          <el-link @click="handleGuest">
            暂不登录，先去逛逛 <el-icon><ArrowRight /></el-icon>
          </el-link>
          <span class="splitter">|</span>
          <el-link type="primary" @click="$router.push('/register')">
            注册新账号
          </el-link>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Lock, Shop, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/login'
import { useUserStore } from '@/stores/useUserStore'
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const userStore:any = useUserStore()

// 定义表单接口
interface LoginForm {
  username: string
  password: string
  remember: boolean
}


interface LoginVO{
  username:string,
  balance:number,
  userId:number,
  token:string
}
const form = reactive<LoginForm>({ 
  username: '', 
  password: '',
  remember: false
})

// 表单验证规则
const rules = reactive<FormRules<LoginForm>>({
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' },
    { min: 2, message: '长度不能少于2位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码不能少于6位', trigger: 'blur' }
  ]
})

// 登录处理逻辑
const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  
  await formEl.validate(async (valid) => {
    if (valid) {
         loading.value = true
      try{
       const loginVo:any = await login(form);
      if(!loginVo || !loginVo.data.token){
        throw new Error('登录失败')
       }else{
        const {token,...data} = loginVo.data
       localStorage.setItem('Authorization',loginVo.data.token)
       console.log(loginVo)
        userStore.updateUserInfo(loginVo.data)
       setTimeout(()=>{
        loading.value = false
        ElMessage.success({
          message: '登录成功，欢迎回来',
          type: 'success'
    
        })
           router.push('/')
      },600)
       }
     
      }
      catch(error){
          ElMessage.error({
            message: '登录失败',
            type: 'error'
          })
      }
    }
  })
}

// 游客访问
const handleGuest = () => {
  router.push('/')
}
</script>

<style scoped>
/* 1. 容器与动态背景 */
.login-container {
  height: 100vh;
  width: 100%;
  /* 柔和的现代渐变 */
  background: linear-gradient(135deg, #eef2f3 0%, #8e9eab 100%);
  /* 或者使用偏蓝色的背景，如果你更喜欢蓝色系： */
  /* background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); */
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

/* 漂浮的圆球装饰 */
.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #36d1dc);
  filter: blur(50px);
  z-index: 0;
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}

.circle-1 { top: -5%; right: 10%; width: 250px; height: 250px; }
.circle-2 { bottom: 5%; left: 10%; width: 300px; height: 300px; animation-delay: -4s; }

/* 2. 卡片主体 - 玻璃拟态 */
.login-card {
  width: 100%;
  max-width: 420px;
  /* 毛玻璃效果核心 */
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
  padding: 10px 20px;
  z-index: 1;
  animation: fadeInUp 0.8s ease-out;
}

/* 头部 Logo */
.brand-header {
  text-align: center;
  margin-bottom: 35px;
}

.logo-box {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.brand-header h2 {
  margin: 0 0 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.brand-header p {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.brand-text {
  color: #409eff;
  font-weight: bold;
}

/* 3. 输入框深度定制 */
:deep(.el-input__wrapper) {
  border-radius: 50px;
  padding: 8px 20px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  background-color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset, 0 0 0 3px rgba(64, 158, 255, 0.15) !important;
  background-color: #fff;
}

/* 辅助选项 (记住我/忘记密码) */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 0 5px;
}

.forgot-pwd {
  font-size: 13px;
}

/* 4. 按钮样式 */
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 2px;
  font-weight: 600;
  background: linear-gradient(90deg, #409eff 0%, #3a8ee6 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.25);
  transition: transform 0.2s, box-shadow 0.2s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.35);
  background: linear-gradient(90deg, #53a8ff 0%, #3a8ee6 100%);
}

.login-btn:active {
  transform: translateY(0);
}

/* 5. 底部页脚 */
.login-footer {
  margin-top: 20px;
  text-align: center;
}

.divider {
  position: relative;
  text-align: center;
  margin-bottom: 15px;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 35%;
  height: 1px;
  background: #ebeef5;
}

.divider::before { left: 0; }
.divider::after { right: 0; }

.divider span {
  background: transparent;
  padding: 0 10px;
  color: #909399;
  font-size: 12px;
}

.action-links {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
}

.splitter {
  margin: 0 12px;
  color: #dcdfe6;
}

/* 动画定义 */
@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
  100% { transform: translateY(0px); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translate3d(0, 30px, 0); }
  to { opacity: 1; transform: translate3d(0, 0, 0); }
}

/* 移动端适配 */
@media screen and (max-width: 480px) {
  .login-card {
    max-width: 90%;
  }
}
</style>