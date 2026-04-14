<template>
  <div class="login-page">
    <!-- 极简模式下去除装饰性网格和光球，保留节点以防影响原结构 -->
    <div class="login-grid" style="display: none;"></div>
    <div class="bg-orb orb-left" style="display: none;"></div>
    <div class="bg-orb orb-right" style="display: none;"></div>

    <div class="auth-shell">
      <section class="auth-hero">
        <div class="hero-brand">
          <div class="hero-brand-mark">YU</div>
          <div class="hero-brand-copy">
            <span class="hero-brand-kicker">YU-MALL ADMIN</span>
            <h1 class="hero-brand-title">全场景电商管理后台</h1>
            <p class="hero-brand-text">
              一站式统筹商品、订单与运营数据。
            </p>
          </div>
        </div>

        <div class="hero-metrics">
          <article v-for="metric in heroMetrics" :key="metric.label" class="hero-metric">
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
                      placeholder="请输入手机号"
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
                创建管理员并进入
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
  { title: '全链路业务监控', icon: Monitor },
  { title: '核心数据多维分析', icon: DataAnalysis },
  { title: '企业级安全权限', icon: Lock }
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
/* 极简背景：纯粹的灰白色 */
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  padding: 24px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* 极简外壳：无阴影或极微弱阴影，细边框 */
.auth-shell {
  width: 100%;
  max-width: 1024px;
  min-height: 640px;
  display: grid;
  grid-template-columns: 1fr 420px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #eaeaea;
  box-shadow: 0 10px 40px -10px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

/* 左侧信息区：排版为主，去色 */
.auth-hero {
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: #ffffff;
  border-right: 1px solid #f0f0f0;
}

.hero-brand {
  display: grid;
  gap: 20px;
}

.hero-brand-mark {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #000000;
  color: #ffffff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.05em;
}

.hero-brand-copy {
  display: grid;
  gap: 12px;
}

.hero-brand-kicker {
  font-size: 12px;
  letter-spacing: 0.15em;
  color: #888888;
  font-weight: 600;
}

.hero-brand-title {
  margin: 0;
  font-size: clamp(24px, 3vw, 32px);
  line-height: 1.2;
  color: #111111;
  font-weight: 600;
}

.hero-brand-text {
  margin: 0;
  max-width: 440px;
  color: #666666;
  font-size: 14px;
  line-height: 1.6;
}

/* 数据指标：去图标化，极简线条分割 */
.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
  margin-top: 24px;
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
}

.hero-metric {
  display: grid;
  gap: 4px;
}

.hero-metric-value {
  font-size: 24px;
  color: #111111;
  line-height: 1;
  font-weight: 500;
}

.hero-metric-label {
  color: #888888;
  font-size: 12px;
}

/* 功能列表：精简为单行标签 */
.hero-feature-list {
  display: grid;
  gap: 0;
  border-top: 1px solid #f0f0f0;
}

.hero-feature-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f9f9f9;
}

.hero-feature-card:last-child {
  border-bottom: none;
}

.hero-feature-icon {
  font-size: 16px;
  color: #000000;
  display: flex;
  align-items: center;
}

.hero-feature-copy {
  display: flex;
  align-items: center;
}

.hero-feature-title {
  color: #333333;
  font-size: 14px;
  font-weight: 500;
}

/* 右侧表单区：纯白背景，居中对齐 */
.auth-panel {
  padding: 48px;
  display: flex;
  align-items: center;
  background: #fafafa;
}

.auth-panel-surface {
  width: 100%;
  max-width: 320px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

/* 切换按钮：下划线极简 Tab */
.auth-mode-switch {
  display: flex;
  gap: 24px;
  border-bottom: 1px solid #eaeaea;
  margin-bottom: 32px;
}

.auth-mode-button {
  background: transparent;
  border: none;
  padding: 0 0 12px 0;
  color: #888888;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  transition: color 0.2s ease;
}

.auth-mode-button.is-active {
  color: #111111;
}

.auth-mode-button.is-active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background: #111111;
}

.auth-panel-header {
  margin-bottom: 32px;
}

.auth-panel-title {
  margin: 0 0 8px;
  color: #111111;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
}

.auth-panel-description {
  margin: 0;
  color: #666666;
  font-size: 13px;
  line-height: 1.5;
}

.auth-form {
  display: grid;
  gap: 4px;
}

.auth-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.auth-form-row-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: -8px;
  margin-bottom: 16px;
}

.auth-form-hint {
  color: #888888;
  font-size: 12px;
}

.auth-register-tip {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border-radius: 6px;
  padding: 12px 14px;
  background: #f0f0f0;
  color: #333333;
  font-size: 12px;
  line-height: 1.5;
  margin-bottom: 24px;
}

.auth-register-tip-icon {
  margin-top: 2px;
  color: #111111;
}

/* 提交按钮：纯黑，微圆角 */
.submit-btn {
  width: 100%;
  min-height: 48px;
  border: none;
  border-radius: 6px;
  background: #000000;
  color: #ffffff;
  font-size: 15px;
  font-weight: 500;
  box-shadow: none;
  transition: background-color 0.2s ease;
}

.submit-btn:hover,
.submit-btn:focus {
  background: #333333;
  color: #ffffff;
}

.auth-panel-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding-top: 24px;
}

.auth-panel-footer-text {
  color: #888888;
  font-size: 13px;
}

.auth-panel-footer-action {
  border: none;
  background: transparent;
  color: #111111;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
  text-underline-offset: 4px;
}

.auth-panel-footer-action:hover {
  color: #666666;
}

/* Element Plus 表单组件极简覆盖 */
.auth-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.auth-form :deep(.el-form-item__label) {
  padding-bottom: 6px;
  color: #111111;
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
}

.auth-form :deep(.el-input__wrapper) {
  min-height: 44px;
  border-radius: 6px;
  background: #ffffff;
  box-shadow: inset 0 0 0 1px #d9d9d9;
  padding: 0 12px;
  transition: box-shadow 0.2s ease;
}

.auth-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: inset 0 0 0 1px #000000 !important;
}

.auth-form :deep(.el-input__prefix-inner),
.auth-form :deep(.el-input__icon) {
  color: #888888;
  font-size: 16px;
}

.auth-form :deep(.el-input__wrapper.is-focus .el-input__icon) {
  color: #111111;
}

.auth-form :deep(.el-checkbox) {
  min-height: 24px;
  color: #666666;
}

.auth-form :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #000000;
  border-color: #000000;
}

.auth-form :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  color: #111111;
}

.auth-form :deep(.el-form-item.is-error .el-input__wrapper) {
  box-shadow: inset 0 0 0 1px #e60000 !important;
}

/* 切换动画 */
.form-switch-enter-active,
.form-switch-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.form-switch-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.form-switch-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 响应式适配 */
@media (max-width: 1080px) {
  .login-page {
    padding: 16px;
  }
  .auth-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }
  .auth-hero {
    padding: 40px 32px;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }
}

@media (max-width: 720px) {
  .login-page {
    padding: 12px;
  }
  .auth-shell {
    border-radius: 8px;
  }
  .auth-hero {
    padding: 32px 20px;
  }
  .hero-brand-title {
    font-size: 22px;
  }
  .hero-metrics {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .auth-form-grid {
    grid-template-columns: 1fr;
  }
  .auth-panel {
    padding: 32px 20px;
  }
  .auth-form-row-between {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>