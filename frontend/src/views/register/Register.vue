<script setup lang="ts">
import { reactive, ref, shallowRef } from 'vue'
import {
  ArrowRight,
  CameraFilled,
  CircleCheckFilled,
  Edit,
  Iphone,
  Loading,
  Lock,
  Postcard,
  Shop,
  User
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import SliderCaptchaDialog from '@/components/auth/SliderCaptchaDialog.vue'
import { register } from '@/api/register'
import { uploadFile } from '@/api/upload'
import { useSliderCaptcha } from '@/composables/useSliderCaptcha'
import { useUserStore } from '@/stores/useUserStore'
import { resolveSimpleAuthErrorMessage } from '@/utils/request'

interface RegisterPageForm {
  avatar: string
  avatarUrl: string
  username: string
  nickName: string
  phone: string
  password: string
  confirmPassword: string
  agree: boolean
  captchaTicket: string
}

const router = useRouter()
const ruleFormRef = ref<FormInstance>()
const loading = shallowRef(false)
const avatarLoading = shallowRef(false)
const userStore = useUserStore()
const {
  captchaVisible,
  captchaChallenge,
  captchaLoading,
  captchaVerifying,
  captchaErrorMessage,
  openCaptcha,
  refreshCaptcha,
  submitCaptcha
} = useSliderCaptcha()

const form = reactive<RegisterPageForm>({
  avatar: '',
  avatarUrl: '',
  username: '',
  nickName: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agree: false,
  captchaTicket: ''
})

const trustBadges = ['新人福利', '账号安全', '购物更快']

const goLogin = () => {
  router.push('/login')
}

const openAgreement = () => {
  window.open('https://www.jd.com/help/question-61.html', '_blank')
}

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }

  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }

  return true
}

const customUploadRequest = async (options: UploadRequestOptions) => {
  avatarLoading.value = true

  try {
    const res: any = await uploadFile(options.file as File)
    const uploadedUrl = res?.url || res?.data?.url

    if (!uploadedUrl) {
      throw new Error('返回结果缺少图片地址')
    }

    form.avatar = uploadedUrl
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    ElMessage.error('头像上传失败，请稍后重试')
  } finally {
    avatarLoading.value = false
  }
}

const validatePass = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
    return
  }

  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }

  callback()
}

const phoneReg = /^1[3-9]\d{9}$/

const rules = reactive<FormRules<RegisterPageForm>>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 15, message: '长度在 2 到 15 个字符', trigger: 'blur' }
  ],
  nickName: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 10, message: '昵称最长 10 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phoneReg, message: '手机号格式不正确', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass, trigger: 'blur' }],
  agree: [
    {
      validator: (_rule: unknown, value: boolean, callback: (error?: Error) => void) => {
        value ? callback() : callback(new Error('请先勾选用户服务协议'))
      },
      trigger: 'change'
    }
  ]
})

const resolveRegisterErrorMessage = (error: any) => {
  return resolveSimpleAuthErrorMessage(error, '注册失败')
}

const performRegister = async () => {
  const response: any = await register({
    username: form.username,
    phone: form.phone,
    password: form.password,
    avatar: form.avatar,
    nickName: form.nickName,
    captchaTicket: form.captchaTicket
  })
  const result = response?.data

  if (!result) {
    throw new Error('注册失败')
  }

  userStore.updateUserInfo({
    ...result,
    username: form.username,
    phone: form.phone
  })

  if (result.token) {
    localStorage.setItem('Authorization', result.token)
  }

  form.captchaTicket = ''
  ElMessage.success('注册成功，欢迎加入 YU-Mall')
  await router.push('/')
}

const ensureCaptchaTicket = async () => {
  if (!form.captchaTicket) {
    form.captchaTicket = await openCaptcha('REGISTER')
  }
}

const submitRegisterFlow = async (retryCount = 0) => {
  await ensureCaptchaTicket()
  try {
    await performRegister()
  } catch (error: any) {
    if (error?.code === 428 && retryCount < 1) {
      form.captchaTicket = ''
      form.captchaTicket = await openCaptcha('REGISTER')
      return submitRegisterFlow(retryCount + 1)
    }
    throw error
  }
}

const handleRegister = async (formEl: FormInstance | undefined) => {
  if (!formEl) return

  const valid = await formEl.validate().then(() => true).catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await submitRegisterFlow()
  } catch (error: any) {
    if (!error?.cancelled) {
      ElMessage.error(resolveRegisterErrorMessage(error))
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <header class="brand-strip">
      <div class="brand-icon">
        <el-icon><Shop /></el-icon>
      </div>
      <span class="brand-name">YU-Mall</span>
    </header>

    <main class="auth-stage">
      <section class="auth-shell">
        <aside class="hero-panel">
          <div class="hero-logo">
            <el-icon><Shop /></el-icon>
          </div>
          <h1 class="hero-title">新用户注册</h1>
          <p class="hero-subtitle">创建账户后继续你的商城浏览、下单与支付流程</p>

          <div class="hero-badges">
            <span v-for="badge in trustBadges" :key="badge" class="hero-badge">
              <el-icon><CircleCheckFilled /></el-icon>
              {{ badge }}
            </span>
          </div>
        </aside>

        <section class="form-panel">
          <div class="auth-tabs">
            <button type="button" class="tab-button active">账号注册</button>
            <button type="button" class="tab-button tab-link" @click="goLogin">已有账号</button>
          </div>

          <div class="register-heading">
            <h2>创建你的 YU-Mall 账号</h2>
            <p>提交后会进行一次简短安全验证，用来拦截异常批量注册。</p>
          </div>

          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action="#"
              :http-request="customUploadRequest"
              :show-file-list="false"
              :before-upload="beforeUpload"
              accept="image/*"
            >
              <div v-if="form.avatar" class="avatar-preview-box">
                <img :src="form.avatar" alt="用户头像" class="avatar-img" />
                <div class="avatar-hover-mask">
                  <el-icon><Edit /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>

              <div v-else class="avatar-upload-trigger">
                <div class="trigger-circle">
                  <el-icon v-if="avatarLoading" class="is-loading"><Loading /></el-icon>
                  <el-icon v-else><CameraFilled /></el-icon>
                </div>
                <span class="trigger-label">
                  {{ avatarLoading ? '上传中...' : '上传头像' }}
                </span>
              </div>
            </el-upload>
          </div>

          <el-form
            ref="ruleFormRef"
            :model="form"
            :rules="rules"
            size="large"
            class="auth-form"
            status-icon
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="设置登录账号"
                :prefix-icon="User"
              />
            </el-form-item>

            <el-form-item prop="nickName">
              <el-input
                v-model="form.nickName"
                placeholder="设置昵称"
                :prefix-icon="Postcard"
              />
            </el-form-item>

            <el-form-item prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号"
                :prefix-icon="Iphone"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="设置密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="再次确认密码"
                :prefix-icon="Lock"
                show-password
              />
            </el-form-item>

            <el-form-item prop="agree" class="agree-item">
              <div class="agree-wrapper">
                <el-checkbox v-model="form.agree">我已阅读并同意</el-checkbox>
                <button type="button" class="text-link" @click="openAgreement">
                  《用户服务协议》
                </button>
              </div>
            </el-form-item>

            <el-form-item class="submit-item">
              <el-button
                type="primary"
                class="submit-button"
                :loading="loading"
                @click="handleRegister(ruleFormRef)"
              >
                立即注册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-footer">
            <span>已有账号？</span>
            <button type="button" class="footer-link" @click="goLogin">
              立即登录
              <el-icon><ArrowRight /></el-icon>
            </button>
          </div>
        </section>
      </section>
    </main>

    <SliderCaptchaDialog
      v-model="captchaVisible"
      :challenge="captchaChallenge"
      :loading="captchaLoading"
      :verifying="captchaVerifying"
      :error-message="captchaErrorMessage"
      @refresh="refreshCaptcha"
      @submit="submitCaptcha"
    />
  </div>
</template>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  padding: 34px 24px 40px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.96) 0, rgba(255, 255, 255, 0.4) 22%, transparent 48%),
    linear-gradient(180deg, #f7f8fb 0%, #eef2f8 100%);
}

.brand-strip {
  display: flex;
  align-items: center;
  gap: 14px;
  width: min(100%, 1320px);
  margin: 0 auto;
}

.brand-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #1660dc;
  color: #fff;
  font-size: 20px;
  box-shadow: 0 12px 24px rgba(22, 96, 220, 0.24);
}

.brand-name {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #161c2d;
}

.auth-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 108px);
  padding-top: 28px;
}

.auth-shell {
  width: min(100%, 1200px);
  min-height: 612px;
  display: grid;
  grid-template-columns: minmax(390px, 1.08fr) minmax(500px, 1.42fr);
  background: #fff;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 32px 80px rgba(29, 55, 112, 0.16);
  margin-bottom: 75px;
}

.hero-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 40px;
  background: linear-gradient(180deg, #1c60dc 0%, #1858cb 100%);
  color: #fff;
  overflow: hidden;
}

.hero-panel::before,
.hero-panel::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
}

.hero-panel::before {
  top: -78px;
  left: -84px;
  width: 270px;
  height: 270px;
}

.hero-panel::after {
  right: -112px;
  bottom: -136px;
  width: 356px;
  height: 356px;
}

.hero-logo {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.22), rgba(255, 255, 255, 0.12));
  border: 1px solid rgba(255, 255, 255, 0.14);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2);
  font-size: 42px;
}

.hero-title {
  position: relative;
  z-index: 1;
  margin: 28px 0 12px;
  font-size: 42px;
  font-weight: 800;
  letter-spacing: 0.01em;
  text-align: center;
}

.hero-subtitle {
  position: relative;
  z-index: 1;
  margin: 0;
  max-width: 280px;
  font-size: 18px;
  line-height: 1.7;
  text-align: center;
  color: rgba(255, 255, 255, 0.88);
}

.hero-badges {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 18px;
  margin-top: 40px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.86);
}

.form-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 42px 58px 36px;
}

.auth-tabs {
  display: flex;
  gap: 28px;
  margin-bottom: 20px;
}

.tab-button {
  padding: 0 2px 12px;
  border: 0;
  border-bottom: 4px solid transparent;
  background: transparent;
  color: #b1b6c0;
  font-size: 16px;
  font-weight: 700;
  cursor: default;
}

.tab-button.active {
  color: #151d29;
  border-bottom-color: #151d29;
}

.tab-link {
  cursor: pointer;
}

.register-heading {
  margin-bottom: 22px;
}

.register-heading h2 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 800;
  color: #151d29;
}

.register-heading p {
  margin: 0;
  color: #7c8492;
  font-size: 14px;
  line-height: 1.7;
}

.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.avatar-uploader {
  cursor: pointer;
}

.avatar-upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.trigger-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(180deg, #eef4ff 0%, #f7faff 100%);
  border: 1px solid #d7e4ff;
  color: #1660dc;
  font-size: 28px;
  box-shadow: 0 12px 28px rgba(22, 96, 220, 0.12);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.avatar-upload-trigger:hover .trigger-circle {
  transform: translateY(-2px);
  box-shadow: 0 18px 32px rgba(22, 96, 220, 0.16);
}

.trigger-label {
  color: #6f7684;
  font-size: 13px;
  font-weight: 600;
}

.avatar-preview-box {
  position: relative;
  width: 96px;
  height: 96px;
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(180deg, #f2f7ff 0%, #fff 100%);
  box-shadow: 0 12px 30px rgba(22, 96, 220, 0.16);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-hover-mask {
  position: absolute;
  inset: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 50%;
  background: rgba(16, 24, 40, 0.58);
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s ease;
  font-size: 12px;
}

.avatar-preview-box:hover .avatar-hover-mask {
  opacity: 1;
}

.agree-wrapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  line-height: 1.7;
}

.text-link,
.footer-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  background: transparent;
  padding: 0;
  color: #1660dc;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.submit-button {
  width: 100%;
  height: 48px;
  border: 0;
  border-radius: 14px;
  background: linear-gradient(180deg, #1660dc 0%, #0f57d3 100%);
  box-shadow: 0 18px 32px rgba(22, 96, 220, 0.2);
  font-size: 18px;
  font-weight: 700;
}

.register-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 18px;
  color: #7c8492;
  font-size: 14px;
}

:deep(.auth-form .el-form-item) {
  margin-bottom: 16px;
}

:deep(.auth-form .agree-item) {
  margin-bottom: 18px;
}

:deep(.auth-form .submit-item) {
  margin-bottom: 0;
}

:deep(.auth-form .el-input__wrapper) {
  min-height: 46px;
  border-radius: 14px;
  padding: 0 16px;
  background: #f4f6fa;
  box-shadow: none;
  transition: background-color 0.2s ease, box-shadow 0.2s ease;
}

:deep(.auth-form .el-input__wrapper:hover) {
  background: #f1f4f9;
}

:deep(.auth-form .el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px rgba(22, 96, 220, 0.4), 0 0 0 4px rgba(22, 96, 220, 0.1);
}

:deep(.auth-form .el-input__prefix-inner) {
  color: #bfc6d2;
}

:deep(.auth-form .el-input__inner::placeholder) {
  color: #b7beca;
}

:deep(.auth-form .el-checkbox__label) {
  color: #8b93a0;
}

@media (max-width: 960px) {
  .auth-page {
    padding: 24px 16px 28px;
  }

  .auth-stage {
    min-height: auto;
    padding-top: 18px;
  }

  .auth-shell {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    min-height: 280px;
    padding: 40px 28px;
  }

  .form-panel {
    padding: 34px 24px 28px;
  }
}

@media (max-width: 640px) {
  .brand-name {
    font-size: 18px;
  }

  .brand-icon {
    width: 38px;
    height: 38px;
  }

  .hero-panel {
    min-height: 240px;
    padding: 34px 22px;
  }

  .hero-logo {
    width: 82px;
    height: 82px;
    border-radius: 22px;
    font-size: 36px;
  }

  .hero-title {
    margin-top: 20px;
    font-size: 34px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .hero-badges {
    gap: 12px;
    margin-top: 30px;
  }

  .form-panel {
    padding: 28px 18px 24px;
  }

  .auth-tabs {
    gap: 20px;
  }

  .register-heading h2 {
    font-size: 24px;
  }
}
</style>
