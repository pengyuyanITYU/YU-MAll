<script setup lang="ts">
import { computed, reactive } from 'vue'
import { ElCheckbox, ElInput, ElMessage } from 'element-plus'
import { ArrowRight, Iphone, Lock, User } from '@element-plus/icons-vue'
import type { AuthPreviewMode, AuthPreviewTheme } from './authPreviewThemes'

const props = defineProps<{
  theme: AuthPreviewTheme
  mode: AuthPreviewMode
}>()

const loginForm = reactive({
  username: '',
  password: '',
  remember: true
})

const registerForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const isLogin = computed(() => props.mode === 'login')
const formTitle = computed(() => (isLogin.value ? props.theme.loginTitle : props.theme.registerTitle))
const formDescription = computed(() =>
  isLogin.value ? props.theme.loginDescription : props.theme.registerDescription
)
const heroBullets = computed(() => props.theme.bullets ?? [])
const showMetrics = computed(() => props.theme.metrics.length > 0)
const showHighlights = computed(() => props.theme.highlights.length > 0)
const previewActionLabel = computed(() => (isLogin.value ? '立即登录' : '立即注册'))

const handlePreviewAction = () => {
  ElMessage.info(`当前是 ${props.theme.name} 的${isLogin.value ? '登录' : '注册'}预览`)
}
</script>

<template>
  <section class="preview-stage" :class="[`theme-${theme.id}`, `mode-${mode}`]" :style="theme.variables">
    <div class="stage-orb orb-a"></div>
    <div class="stage-orb orb-b"></div>

    <div class="preview-grid">
      <aside class="hero-panel">
        <div class="hero-shell">
          <span class="hero-eyebrow">{{ theme.heroEyebrow }}</span>
          <h2>{{ theme.heroTitle }}</h2>
          <p class="hero-subtitle">{{ theme.heroSubtitle }}</p>
          <p class="hero-note">{{ theme.heroNote }}</p>

          <ul v-if="heroBullets.length" class="hero-bullet-list">
            <li v-for="bullet in heroBullets" :key="bullet" class="hero-bullet">
              <span class="bullet-dot"></span>
              <span>{{ bullet }}</span>
            </li>
          </ul>

          <div v-if="showMetrics" class="metric-list">
            <article v-for="metric in theme.metrics" :key="metric.label" class="metric-card">
              <strong>{{ metric.value }}</strong>
              <span>{{ metric.label }}</span>
            </article>
          </div>

          <div v-if="showHighlights" class="highlight-list">
            <article v-for="highlight in theme.highlights" :key="highlight.title" class="highlight-card">
              <strong>{{ highlight.title }}</strong>
              <p>{{ highlight.description }}</p>
            </article>
          </div>
        </div>
      </aside>

      <div class="form-frame">
        <div class="form-panel">
          <div class="window-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>

          <span class="form-badge">{{ theme.formBadge }}</span>
          <h3>{{ formTitle }}</h3>
          <p class="form-description">{{ formDescription }}</p>

          <form class="form-stack auth-preview-form" @submit.prevent="handlePreviewAction">
            <template v-if="isLogin">
              <label class="field">
                <span>账号</span>
                <ElInput
                  v-model="loginForm.username"
                  placeholder="用户名 / 手机号"
                  :prefix-icon="User"
                  size="large"
                />
              </label>

              <label class="field">
                <span>密码</span>
                <ElInput
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  show-password
                  size="large"
                />
              </label>

              <div class="action-row">
                <ElCheckbox v-model="loginForm.remember">保持登录</ElCheckbox>
                <button type="button" class="ghost-link">忘记密码</button>
              </div>
            </template>

            <template v-else>
              <label class="field">
                <span>账号</span>
                <ElInput
                  v-model="registerForm.username"
                  placeholder="设置登录账号"
                  :prefix-icon="User"
                  size="large"
                />
              </label>

              <label class="field">
                <span>昵称</span>
                <ElInput
                  v-model="registerForm.nickname"
                  placeholder="请输入昵称"
                  :prefix-icon="User"
                  size="large"
                />
              </label>

              <label class="field">
                <span>手机号</span>
                <ElInput
                  v-model="registerForm.phone"
                  placeholder="请输入手机号"
                  :prefix-icon="Iphone"
                  size="large"
                />
              </label>

              <div class="field-grid">
                <label class="field">
                  <span>密码</span>
                  <ElInput
                    v-model="registerForm.password"
                    type="password"
                    placeholder="6-20 位密码"
                    :prefix-icon="Lock"
                    show-password
                    size="large"
                  />
                </label>

                <label class="field">
                  <span>确认密码</span>
                  <ElInput
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="再次输入密码"
                    :prefix-icon="Lock"
                    show-password
                    size="large"
                  />
                </label>
              </div>

              <div class="avatar-hint">
                <div class="avatar-token">N</div>
                <div>
                  <strong>昵称优先展示</strong>
                  <p>这一版注册框把昵称放回主流程，后续个人中心再补头像与更多资料。</p>
                </div>
              </div>
            </template>

            <button type="submit" class="primary-button">
              {{ previewActionLabel }}
              <ArrowRight class="button-icon" />
            </button>
          </form>

          <div class="panel-footer">
            <span>预览实验页</span>
            <p>先确认风格，再正式替换当前的 `/login` 和 `/register`。</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.preview-stage {
  position: relative;
  overflow: hidden;
  min-height: max(760px, calc(100vh - 190px));
  padding: 28px;
  border-radius: 36px;
  background: var(--auth-bg);
  box-shadow: var(--auth-shadow);
  color: var(--auth-text);
}

.preview-stage::before {
  content: '';
  position: absolute;
  inset: 18px;
  border: 1px solid rgba(255, 255, 255, 0.36);
  border-radius: 30px;
  pointer-events: none;
}

.stage-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(24px);
  opacity: 0.5;
  pointer-events: none;
}

.orb-a {
  top: -80px;
  right: -40px;
  width: 280px;
  height: 280px;
  background: var(--auth-accent-soft);
}

.orb-b {
  bottom: -120px;
  left: -60px;
  width: 340px;
  height: 340px;
  background: var(--auth-hero-soft);
}

.preview-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(360px, 520px);
  gap: 24px;
  min-height: inherit;
}

.hero-panel,
.form-frame {
  min-width: 0;
}

.hero-shell {
  display: grid;
  align-content: center;
  gap: 24px;
  height: 100%;
  padding: 26px;
  font-family: var(--auth-body-font);
}

.hero-eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 8px 14px;
  border: 1px solid rgba(255, 255, 255, 0.32);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.42);
  color: var(--auth-muted);
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.hero-shell h2 {
  margin: 0;
  max-width: 12ch;
  color: var(--auth-hero);
  font-family: var(--auth-display-font);
  font-size: clamp(40px, 5vw, 64px);
  line-height: 1.04;
}

.hero-subtitle,
.hero-note {
  max-width: 560px;
  margin: 0;
  font-size: 16px;
  line-height: 1.75;
}

.hero-subtitle {
  color: var(--auth-text);
}

.hero-note {
  color: var(--auth-muted);
}

.hero-bullet-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.hero-bullet {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.4);
}

.bullet-dot {
  flex: none;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: var(--auth-accent);
  box-shadow: 0 0 0 6px var(--auth-accent-soft);
}

.metric-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.metric-card,
.highlight-card {
  border: 1px solid rgba(255, 255, 255, 0.34);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.34);
  backdrop-filter: blur(10px);
}

.metric-card {
  display: grid;
  gap: 6px;
  padding: 18px;
}

.metric-card strong {
  color: var(--auth-hero);
  font-size: 24px;
}

.metric-card span {
  color: var(--auth-muted);
  font-size: 13px;
}

.highlight-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.highlight-card {
  display: grid;
  gap: 10px;
  padding: 20px;
}

.highlight-card strong {
  color: var(--auth-hero);
  font-size: 17px;
}

.highlight-card p {
  margin: 0;
  color: var(--auth-muted);
  font-size: 14px;
  line-height: 1.7;
}

.form-frame {
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-panel {
  display: grid;
  gap: 18px;
  width: min(100%, 500px);
  padding: 28px;
  border: 1px solid var(--auth-border);
  border-radius: var(--auth-card-radius);
  background: var(--auth-surface);
  box-shadow: var(--auth-shadow);
  backdrop-filter: blur(18px);
  font-family: var(--auth-body-font);
}

.window-dots {
  display: inline-flex;
  gap: 8px;
}

.window-dots span {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: rgba(31, 31, 31, 0.16);
}

.form-badge {
  display: inline-flex;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: var(--auth-accent-soft);
  color: var(--auth-accent);
  font-size: 13px;
  font-weight: 600;
}

.form-panel h3 {
  margin: 0;
  color: var(--auth-text);
  font-family: var(--auth-display-font);
  font-size: clamp(28px, 3vw, 36px);
  line-height: 1.15;
}

.form-description {
  margin: 0;
  color: var(--auth-muted);
  line-height: 1.7;
}

.form-stack {
  display: grid;
  gap: 16px;
}

.field {
  display: grid;
  gap: 8px;
  color: var(--auth-text);
  font-size: 14px;
}

.field span {
  font-weight: 600;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--auth-muted);
}

.ghost-link {
  padding: 0;
  border: none;
  background: transparent;
  color: var(--auth-accent);
  cursor: pointer;
}

.avatar-hint {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1px solid var(--auth-border);
  border-radius: 20px;
  background: var(--auth-accent-soft);
}

.avatar-token {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: var(--auth-accent);
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}

.avatar-hint strong {
  display: block;
  margin-bottom: 4px;
}

.avatar-hint p,
.panel-footer p {
  margin: 0;
  color: var(--auth-muted);
  line-height: 1.65;
}

.primary-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 20px;
  border: none;
  border-radius: var(--auth-button-radius);
  background: var(--auth-accent);
  color: #fff;
  box-shadow: 0 18px 28px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  font-size: 16px;
  font-weight: 700;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.primary-button:hover {
  transform: translateY(-2px);
}

.button-icon {
  width: 16px;
  height: 16px;
}

.panel-footer {
  display: grid;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--auth-border);
}

.panel-footer span {
  color: var(--auth-muted);
  font-size: 13px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

:deep(.el-input__wrapper) {
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.68);
  box-shadow: 0 0 0 1px rgba(17, 17, 17, 0.05) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--auth-accent) inset;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  border-color: var(--auth-accent);
  background: var(--auth-accent);
}

:deep(.el-checkbox__label) {
  color: var(--auth-muted);
}

.theme-playful .hero-shell h2 {
  max-width: 11ch;
  transform: rotate(-1deg);
}

.theme-playful .metric-card,
.theme-playful .highlight-card {
  background: rgba(255, 255, 255, 0.54);
}

.theme-playful .primary-button {
  box-shadow: 0 18px 28px rgba(239, 91, 46, 0.28);
}

.theme-minimal::before {
  border-color: rgba(0, 0, 0, 0.06);
}

.theme-minimal .metric-card,
.theme-minimal .highlight-card {
  background: rgba(255, 255, 255, 0.55);
}

.theme-minimal .hero-shell h2 {
  max-width: 13ch;
  letter-spacing: -0.04em;
}

.theme-promoClean .preview-grid,
.theme-pureMinimal .preview-grid {
  grid-template-columns: minmax(0, 1fr) minmax(360px, 460px);
}

.theme-promoClean .hero-shell,
.theme-pureMinimal .hero-shell,
.theme-homeShowcase .hero-shell,
.theme-categoryGlass .hero-shell,
.theme-cartReturn .hero-shell {
  gap: 18px;
}

.theme-promoClean .hero-shell h2,
.theme-pureMinimal .hero-shell h2,
.theme-homeShowcase .hero-shell h2,
.theme-categoryGlass .hero-shell h2,
.theme-cartReturn .hero-shell h2 {
  max-width: 11ch;
  font-size: clamp(34px, 4vw, 52px);
}

.theme-promoClean .avatar-hint,
.theme-pureMinimal .avatar-hint {
  display: none;
}

.theme-pureMinimal {
  min-height: 700px;
}

.theme-pureMinimal::before {
  inset: 24px;
  border-color: rgba(0, 0, 0, 0.04);
}

.theme-pureMinimal .stage-orb {
  opacity: 0.22;
  filter: blur(32px);
}

.theme-pureMinimal .hero-eyebrow,
.theme-pureMinimal .hero-bullet,
.theme-pureMinimal :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.72);
}

.theme-pureMinimal .window-dots,
.theme-pureMinimal .panel-footer {
  display: none;
}

.theme-pureMinimal .form-panel {
  gap: 14px;
  border-width: 1px;
}

.theme-pureMinimal .hero-note {
  max-width: 480px;
}

.theme-homeShowcase .metric-card,
.theme-homeShowcase .highlight-card {
  border-color: rgba(64, 158, 255, 0.12);
  background: rgba(255, 255, 255, 0.5);
}

.theme-homeShowcase .primary-button {
  background: linear-gradient(135deg, #409eff, #69b1ff);
  box-shadow: 0 18px 28px rgba(64, 158, 255, 0.26);
}

.theme-categoryGlass .metric-card,
.theme-categoryGlass .highlight-card {
  background: rgba(255, 255, 255, 0.46);
  backdrop-filter: blur(14px);
}

.theme-categoryGlass .hero-eyebrow,
.theme-categoryGlass .hero-bullet {
  background: rgba(255, 255, 255, 0.52);
}

.theme-categoryGlass .primary-button {
  box-shadow: 0 18px 28px rgba(54, 169, 214, 0.24);
}

.theme-cartReturn .hero-bullet {
  background: rgba(255, 255, 255, 0.5);
}

.theme-cartReturn .form-panel {
  border-radius: 24px;
}

.theme-cartReturn .primary-button {
  box-shadow: 0 18px 28px rgba(76, 121, 166, 0.22);
}

@media (max-width: 1200px) {
  .preview-grid {
    grid-template-columns: 1fr;
  }

  .hero-shell {
    padding: 8px;
  }

  .form-frame {
    justify-content: stretch;
  }

  .form-panel {
    width: 100%;
  }
}

@media (max-width: 760px) {
  .preview-stage {
    min-height: auto;
    padding: 18px;
    border-radius: 28px;
  }

  .metric-list,
  .highlight-list,
  .field-grid {
    grid-template-columns: 1fr;
  }

  .hero-shell h2 {
    font-size: 36px;
  }

  .form-panel {
    padding: 22px;
  }
}
</style>
