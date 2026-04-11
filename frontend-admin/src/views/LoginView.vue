<template>
  <div class="login-page">
    <div class="login-grid"></div>
    <div class="bg-orb orb-left"></div>
    <div class="bg-orb orb-right"></div>

    <div class="auth-shell">
      <section class="auth-hero">
        <div class="hero-brand">
          <div class="hero-brand-mark">YU</div>
          <div class="hero-brand-copy">
            <span class="hero-brand-kicker">YU-MALL ADMIN</span>
            <h1 class="hero-brand-title">让后台登录页看起来像真正的控制台入口</h1>
            <p class="hero-brand-text">
              统一接入商品、订单、分类、评论与店铺管理，关键链路在进入控制台前就建立清晰认知。
            </p>
          </div>
        </div>

        <div class="hero-metrics">
          <article v-for="metric in heroMetrics" :key="metric.label" class="hero-metric">
            <component :is="metric.icon" class="hero-metric-icon" />
            <strong class="hero-metric-value">{{ metric.value }}</strong>
            <span class="hero-metric-label">{{ metric.label }}</span>
          </article>
        </div>

        <div class="hero-feature-list">
          <article v-for="feature in heroFeatures" :key="feature.title" class="hero-feature-card">
            <div class="hero-feature-icon">
              <component :is="feature.icon" />
            </div>
            <div class="hero-feature-copy">
              <strong class="hero-feature-title">{{ feature.title }}</strong>
              <p class="hero-feature-text">{{ feature.text }}</p>
            </div>
          </article>
        </div>
      </section>

      <section class="auth-panel">
        <div class="auth-panel-surface">
          <div class="auth-mode-switch" role="tablist" aria-label="认证模式切换">
            <button
              type="button"
              class="auth-mode-button"
              :class="{ 'is-active': activeMode === 'login' }"
              :aria-pressed="activeMode === 'login'"
              @click="switchAuthMode('login')"
            >
              登录
            </button>
            <button
              type="button"
              class="auth-mode-button"
              :class="{ 'is-active': activeMode === 'register' }"
              :aria-pressed="activeMode === 'register'"
              @click="switchAuthMode('register')"
            >
              注册
            </button>
          </div>

          <div class="auth-panel-header">
            <span class="auth-panel-kicker">{{ panelKicker }}</span>
            <h2 class="auth-panel-title">{{ panelTitle }}</h2>
            <p class="auth-panel-description">{{ panelDescription }}</p>
          </div>

          <Transition name="form-switch" mode="out-in">
            <el-form
              v-if="activeMode === 'login'"
              ref="loginFormRef"
              key="login-form"
              :model="loginForm"
              :rules="loginRules"
              label-position="top"
              class="auth-form"
              @keyup.enter="submit"
            >
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model.trim="loginForm.username"
                  size="large"
                  placeholder="请输入管理员用户名"
                  autocomplete="username"
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="loginForm.password"
                  size="large"
                  type="password"
                  show-password
                  placeholder="请输入登录密码"
                  autocomplete="current-password"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <div class="auth-form-row auth-form-row-between">
                <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
                <span class="auth-form-hint">仅限当前设备使用</span>
              </div>

              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="submit"
              >
                进入控制台
              </el-button>
            </el-form>

            <el-form
              v-else
              ref="registerFormRef"
              key="register-form"
              :model="registerForm"
              :rules="registerRules"
              label-position="top"
              class="auth-form"
              @keyup.enter="submit"
            >
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model.trim="registerForm.username"
                  size="large"
                  placeholder="设置管理员登录名"
                  autocomplete="username"
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <div class="auth-form-grid">
                <el-form-item label="显示名称" prop="nickName">
                  <el-input
                    v-model.trim="registerForm.nickName"
                    size="large"
                    placeholder="用于页面展示"
                    autocomplete="nickname"
                  >
                    <template #prefix>
                      <el-icon><Promotion /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <el-form-item label="手机号" prop="phone">
                  <el-input
                    v-model.trim="registerForm.phone"
                    size="large"
                    placeholder="请输入 11 位手机号"
                    autocomplete="tel"
                    inputmode="numeric"
                  >
                    <template #prefix>
                      <el-icon><Iphone /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </div>

              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="registerForm.password"
                  size="large"
                  type="password"
                  show-password
                  placeholder="至少 6 位，注册后自动登录"
                  autocomplete="new-password"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <div class="auth-register-tip">
                <el-icon class="auth-register-tip-icon"><CircleCheckFilled /></el-icon>
                <span>头像可在进入控制台后补充，当前先使用昵称首字母作为默认头像。</span>
              </div>

              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="submit"
              >
                创建管理员并进入控制台
              </el-button>
            </el-form>
          </Transition>

          <div class="auth-panel-footer">
            <span class="auth-panel-footer-text">{{ footerText }}</span>
            <button type="button" class="auth-panel-footer-action" @click="switchAuthMode(nextMode)">
              {{ footerAction }}
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  CircleCheckFilled,
  DataAnalysis,
  Iphone,
  Lock,
  Monitor,
  Promotion,
  User
} from '@element-plus/icons-vue';
import { computed, nextTick, reactive, ref, shallowRef, watch } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRoute, useRouter } from 'vue-router';
import { login, register } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';
import type { LoginForm, RegisterForm } from '@/types/domain';

type AuthMode = 'login' | 'register';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const loginFormRef = ref<FormInstance>();
const registerFormRef = ref<FormInstance>();
const activeMode = shallowRef<AuthMode>('login');
const loading = shallowRef(false);

const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
  rememberMe: false
});

const registerForm = reactive<RegisterForm>({
  username: '',
  nickName: '',
  phone: '',
  password: '',
  avatar: ''
});

const heroMetrics = [
  { value: '7 x 24', label: '核心链路监控', icon: Monitor },
  { value: '4', label: '后台关键模块', icon: DataAnalysis },
  { value: '99.9%', label: '认证链路目标', icon: CircleCheckFilled }
];

const heroFeatures = [
  {
    title: '订单与发货节奏',
    text: '进入控制台前就强调数据监控属性，减少普通表单页的割裂感。',
    icon: Monitor
  },
  {
    title: '商品与分类协同',
    text: '把日常高频操作映射到入口页面，让认证页更像后台产品的一部分。',
    icon: DataAnalysis
  },
  {
    title: '权限入口更清晰',
    text: '登录和注册在一个壳层内切换，避免来回跳页打断操作节奏。',
    icon: Lock
  }
];

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度需在 2 到 20 位之间', trigger: 'blur' }
  ],
  nickName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的 11 位手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
};

const panelKicker = computed(() => (activeMode.value === 'login' ? 'AUTH SIGN IN' : 'AUTH REGISTER'));
const panelTitle = computed(() => (activeMode.value === 'login' ? '登录管理端' : '注册管理员'));
const panelDescription = computed(() =>
  activeMode.value === 'login'
    ? '输入管理员账号后直接进入控制台，查看商品、订单、评论与店铺运营状态。'
    : '新管理员创建完成后会自动登录，无需再次输入账号密码。'
);
const nextMode = computed<AuthMode>(() => (activeMode.value === 'login' ? 'register' : 'login'));
const footerText = computed(() =>
  activeMode.value === 'login' ? '还没有管理员账号？' : '已经有管理员账号？'
);
const footerAction = computed(() => (activeMode.value === 'login' ? '立即注册' : '返回登录'));

watch(
  () => route.path,
  (path) => {
    activeMode.value = path === '/register' ? 'register' : 'login';
  },
  { immediate: true }
);

watch(activeMode, () => {
  nextTick(() => {
    loginFormRef.value?.clearValidate();
    registerFormRef.value?.clearValidate();
  });
});

function switchAuthMode(mode: AuthMode) {
  if (activeMode.value === mode) {
    return;
  }
  activeMode.value = mode;
  if (mode === 'register') {
    router.replace('/register');
    return;
  }
  router.replace('/login');
}

async function submit() {
  const formRef = activeMode.value === 'login' ? loginFormRef.value : registerFormRef.value;
  if (!formRef) {
    return;
  }

  const valid = await formRef.validate().catch(() => false);
  if (!valid || loading.value) {
    return;
  }

  loading.value = true;
  try {
    if (activeMode.value === 'login') {
      await submitLogin();
      return;
    }
    await submitRegister();
  } finally {
    loading.value = false;
  }
}

async function submitLogin() {
  const res = await login({ ...loginForm });
  if (res.code !== 200 || !res.data?.token) {
    ElMessage.error(res.msg || '登录失败');
    return;
  }

  authStore.setLogin({
    token: res.data.token,
    userId: res.data.userId,
    nickName: res.data.nickName,
    avatar: res.data.avatar
  });
  ElMessage.success('登录成功');
  router.replace('/dashboard');
}

async function submitRegister() {
  const payload: RegisterForm = {
    ...registerForm,
    avatar: ''
  };
  const res = await register(payload);
  if (res.code !== 200 || !res.data?.token) {
    ElMessage.error(res.msg || '注册失败');
    return;
  }

  authStore.setLogin({
    token: res.data.token,
    userId: res.data.userId,
    nickName: res.data.nickName ?? registerForm.nickName,
    avatar: res.data.avatar
  });
  ElMessage.success('注册成功');
  router.replace('/dashboard');
}
</script>

<style scoped lang="scss">
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 32px;
  background:
    radial-gradient(circle at 12% 18%, rgba(56, 189, 248, 0.22), transparent 22%),
    radial-gradient(circle at 88% 24%, rgba(14, 165, 233, 0.16), transparent 26%),
    linear-gradient(135deg, #07111d 0%, #0c2037 48%, #12365a 100%);
}

.login-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.85), transparent 92%);
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(4px);
}

.orb-left {
  top: -120px;
  left: -60px;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.32), transparent 70%);
}

.orb-right {
  right: -120px;
  bottom: -160px;
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.28), transparent 68%);
}

.auth-shell {
  position: relative;
  z-index: 1;
  width: min(1160px, 100%);
  min-height: calc(100vh - 64px);
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(360px, 460px);
  border-radius: 32px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(7, 16, 29, 0.42);
  box-shadow: 0 28px 80px rgba(3, 10, 20, 0.34);
  backdrop-filter: blur(18px);
}

.auth-hero {
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 32px;
}

.hero-brand {
  display: grid;
  gap: 22px;
}

.hero-brand-mark {
  width: 68px;
  height: 68px;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0ea5e9, #38bdf8);
  color: #f8fafc;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0.08em;
  box-shadow: 0 18px 34px rgba(14, 165, 233, 0.24);
}

.hero-brand-copy {
  display: grid;
  gap: 16px;
}

.hero-brand-kicker {
  font-size: 12px;
  letter-spacing: 0.22em;
  color: #7dd3fc;
  font-weight: 700;
}

.hero-brand-title {
  margin: 0;
  max-width: 520px;
  font-size: clamp(32px, 4vw, 50px);
  line-height: 1.08;
  color: #f8fafc;
}

.hero-brand-text {
  margin: 0;
  max-width: 520px;
  color: rgba(226, 232, 240, 0.82);
  font-size: 15px;
  line-height: 1.8;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.hero-metric {
  border-radius: 22px;
  border: 1px solid rgba(125, 211, 252, 0.14);
  background: linear-gradient(180deg, rgba(8, 18, 34, 0.72), rgba(8, 18, 34, 0.46));
  padding: 20px;
  display: grid;
  gap: 10px;
}

.hero-metric-icon {
  font-size: 18px;
  color: #7dd3fc;
}

.hero-metric-value {
  font-size: 26px;
  color: #f8fafc;
  line-height: 1;
}

.hero-metric-label {
  color: rgba(148, 163, 184, 0.92);
  font-size: 13px;
}

.hero-feature-list {
  display: grid;
  gap: 16px;
}

.hero-feature-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 16px;
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  background: rgba(15, 23, 42, 0.32);
}

.hero-feature-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #38bdf8;
  background: rgba(14, 165, 233, 0.12);
  font-size: 20px;
}

.hero-feature-copy {
  display: grid;
  gap: 8px;
}

.hero-feature-title {
  color: #f8fafc;
  font-size: 15px;
}

.hero-feature-text {
  margin: 0;
  color: rgba(148, 163, 184, 0.92);
  font-size: 13px;
  line-height: 1.7;
}

.auth-panel {
  padding: 20px;
  display: flex;
}

.auth-panel-surface {
  width: 100%;
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.92));
  padding: 28px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 24px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.auth-mode-switch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  padding: 6px;
  border-radius: 18px;
  background: #e2e8f0;
}

.auth-mode-button {
  min-height: 46px;
  border: none;
  border-radius: 14px;
  background: transparent;
  color: #475569;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.22s ease, color 0.22s ease, box-shadow 0.22s ease, transform 0.22s ease;
}

.auth-mode-button:focus-visible,
.auth-panel-footer-action:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.22);
}

.auth-mode-button.is-active {
  background: linear-gradient(135deg, #0f172a, #1d4ed8);
  color: #f8fafc;
  box-shadow: 0 14px 26px rgba(29, 78, 216, 0.24);
}

.auth-panel-header {
  display: grid;
  gap: 12px;
}

.auth-panel-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: #0284c7;
}

.auth-panel-title {
  margin: 0;
  color: #0f172a;
  font-size: 30px;
  line-height: 1.15;
}

.auth-panel-description {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}

.auth-form {
  display: grid;
  gap: 4px;
}

.auth-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.auth-form-row {
  display: flex;
  align-items: center;
}

.auth-form-row-between {
  justify-content: space-between;
  gap: 12px;
  margin-top: -2px;
  margin-bottom: 8px;
}

.auth-form-hint {
  color: #64748b;
  font-size: 12px;
}

.auth-register-tip {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border-radius: 16px;
  padding: 14px 16px;
  background: #e0f2fe;
  color: #0c4a6e;
  font-size: 13px;
  line-height: 1.7;
}

.auth-register-tip-icon {
  margin-top: 2px;
  color: #0284c7;
}

.submit-btn {
  width: 100%;
  min-height: 52px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #0f172a, #1d4ed8);
  box-shadow: 0 16px 30px rgba(29, 78, 216, 0.22);
  font-weight: 700;
}

.auth-panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 4px;
}

.auth-panel-footer-text {
  color: #64748b;
  font-size: 13px;
}

.auth-panel-footer-action {
  border: none;
  background: transparent;
  color: #0369a1;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.22s ease, transform 0.22s ease;
}

.auth-panel-footer-action:hover {
  color: #1d4ed8;
}

.auth-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.auth-form :deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: #0f172a;
  font-size: 13px;
  font-weight: 700;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 52px;
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.98);
  box-shadow: inset 0 0 0 1px #d7e2ee;
  transition: box-shadow 0.22s ease, background-color 0.22s ease, transform 0.22s ease;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  box-shadow:
    inset 0 0 0 1px #0ea5e9,
    0 0 0 4px rgba(14, 165, 233, 0.12);
}

.auth-form :deep(.el-input__prefix-inner),
.auth-form :deep(.el-input__icon),
.auth-form :deep(.el-checkbox__label) {
  color: #64748b;
}

.auth-form :deep(.el-checkbox) {
  min-height: 24px;
}

.auth-form :deep(.el-form-item.is-error .el-input__wrapper) {
  box-shadow:
    inset 0 0 0 1px #ef4444,
    0 0 0 4px rgba(239, 68, 68, 0.08);
}

.form-switch-enter-active,
.form-switch-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.form-switch-enter-from {
  opacity: 0;
  transform: translateX(18px);
}

.form-switch-leave-to {
  opacity: 0;
  transform: translateX(-18px);
}

@media (max-width: 1080px) {
  .login-page {
    padding: 18px;
  }

  .auth-shell {
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .auth-hero {
    padding: 28px 28px 0;
  }

  .hero-metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .login-page {
    padding: 12px;
  }

  .auth-shell {
    border-radius: 24px;
  }

  .auth-hero {
    padding: 22px 22px 0;
    gap: 20px;
  }

  .hero-brand-title {
    font-size: 30px;
  }

  .hero-metrics,
  .auth-form-grid {
    grid-template-columns: 1fr;
  }

  .hero-feature-card,
  .auth-panel-surface {
    border-radius: 20px;
  }

  .auth-panel {
    padding: 12px;
  }

  .auth-panel-surface {
    padding: 22px 18px;
  }

  .auth-panel-footer,
  .auth-form-row-between {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (prefers-reduced-motion: reduce) {
  .auth-mode-button,
  .auth-panel-footer-action,
  .auth-form :deep(.el-input__wrapper),
  .form-switch-enter-active,
  .form-switch-leave-active {
    transition: none;
  }
}
</style>
