<script setup lang="ts">
import { ArrowRight, CircleCheck, Service, ShoppingBag, Van } from '@element-plus/icons-vue'

defineProps<{
  formBadge: string
  formTitle: string
  formDescription: string
  footerText: string
  footerActionText: string
  footerActionTo: string
}>()

const brandFeatures = [
  { icon: CircleCheck, label: '官方正品' },
  { icon: Van, label: '极速发货' },
  { icon: Service, label: '售后无忧' }
]
</script>

<template>
  <section class="auth-blue-page">
    <div class="page-header-logo">
      <div class="header-logo-icon">
        <el-icon><ShoppingBag /></el-icon>
      </div>
      <span class="header-logo-text">YU-Mall</span>
    </div>

    <div class="bg-circle circle-1"></div>
    <div class="bg-circle circle-2"></div>

    <div class="auth-shell">
      <aside class="brand-panel">
        <div class="decor-shape shape-1"></div>
        <div class="decor-shape shape-2"></div>
        <div class="decor-shape shape-3"></div>

        <div class="brand-content">
          <div class="logo-box">
            <el-icon class="logo-icon"><ShoppingBag /></el-icon>
          </div>

          <h1 class="brand-name">YU-Mall</h1>
          <p class="brand-slogan">发现值得购买的好物，也把认证入口留在商城主场景里。</p>

          <div class="brand-intro">
            <p>从首页活动、购物车到订单中心，登录后继续你的完整购物流程。</p>
            <p>新人注册后也能直接领取权益，不需要在页面之间反复切换。</p>
          </div>

          <div class="brand-features">
            <span
              v-for="feature in brandFeatures"
              :key="feature.label"
              class="brand-feature"
            >
              <el-icon><component :is="feature.icon" /></el-icon>
              {{ feature.label }}
            </span>
          </div>
        </div>
      </aside>

      <div class="form-panel">
        <div class="form-container">
          <div class="form-header">
            <span class="form-badge">{{ formBadge }}</span>
            <h2 class="form-title">{{ formTitle }}</h2>
            <p class="form-description">{{ formDescription }}</p>
          </div>

          <slot />

          <div class="panel-footer">
            <span class="footer-text">{{ footerText }}</span>
            <router-link :to="footerActionTo" class="panel-link">
              {{ footerActionText }}
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped lang="scss">
.auth-blue-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.42), transparent 30%),
    linear-gradient(135deg, #edf4ff 0%, #d9e6ff 42%, #f5f8ff 100%);
}

.page-header-logo {
  position: absolute;
  top: 34px;
  left: 42px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #0052d9;
  color: #fff;
  font-size: 20px;
}

.header-logo-text {
  color: #111;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.bg-circle {
  position: absolute;
  z-index: 0;
  border-radius: 50%;
  filter: blur(100px);
}

.circle-1 {
  top: -14%;
  left: -6%;
  width: 520px;
  height: 520px;
  background: rgba(255, 255, 255, 0.88);
  animation: float 8s ease-in-out infinite;
}

.circle-2 {
  right: 6%;
  bottom: -16%;
  width: 480px;
  height: 480px;
  background: rgba(0, 82, 217, 0.08);
  animation: float 10s ease-in-out infinite reverse;
}

.auth-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(420px, 0.92fr);
  min-height: 100vh;
}

.brand-panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 110px clamp(32px, 6vw, 88px) 64px;
  background:
    linear-gradient(160deg, rgba(0, 82, 217, 0.96), rgba(18, 103, 255, 0.92)),
    linear-gradient(135deg, #0052d9, #3c8cff);
  color: #fff;
}

.decor-shape {
  position: absolute;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.shape-1 {
  top: -70px;
  left: -60px;
  width: 260px;
  height: 260px;
}

.shape-2 {
  right: -120px;
  bottom: -120px;
  width: 380px;
  height: 380px;
}

.shape-3 {
  top: 28%;
  right: 8%;
  width: 140px;
  height: 140px;
  background: rgba(255, 255, 255, 0.12);
}

.brand-content {
  position: relative;
  z-index: 1;
  width: min(100%, 520px);
}

.logo-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  margin: 0 0 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
}

.logo-icon {
  color: #fff;
  font-size: 36px;
}

.brand-name {
  margin: 0 0 14px;
  font-size: clamp(40px, 4vw, 58px);
  font-weight: 800;
  letter-spacing: 0.04em;
}

.brand-slogan {
  margin: 0;
  max-width: 18ch;
  font-size: clamp(28px, 2.8vw, 42px);
  line-height: 1.22;
  font-weight: 700;
}

.brand-intro {
  display: grid;
  gap: 12px;
  margin: 26px 0 34px;
  max-width: 520px;
}

.brand-intro p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 16px;
  line-height: 1.8;
}

.brand-features {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 14px;
}

.brand-feature {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.16);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 110px clamp(24px, 5vw, 88px) 64px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.92));
  box-sizing: border-box;
}

.form-container {
  width: min(100%, 460px);
  padding: clamp(28px, 3vw, 40px);
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 24px 60px rgba(35, 76, 163, 0.14);
  backdrop-filter: blur(18px);
}

.form-header {
  margin-bottom: 28px;
}

.form-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(0, 82, 217, 0.08);
  color: #0052d9;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.form-title {
  margin: 16px 0 10px;
  color: #111;
  font-size: 28px;
  font-weight: 800;
  line-height: 1.2;
}

.form-description {
  margin: 0;
  color: #667085;
  font-size: 14px;
  line-height: 1.8;
}

.panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 22px;
  font-size: 14px;
}

.footer-text {
  color: #666;
}

.panel-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0052d9;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.panel-link:hover {
  color: #266fe8;
}

:deep(.auth-form .el-form-item) {
  margin-bottom: 22px;
}

:deep(.auth-form .el-input__wrapper) {
  padding: 0 16px;
  border-radius: 12px;
  background-color: #f4f6f8;
  box-shadow: none !important;
  transition: all 0.3s ease;
}

:deep(.auth-form .el-input__wrapper:hover) {
  background-color: #eceef2;
}

:deep(.auth-form .el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: inset 0 0 0 1px #0052d9, 0 0 0 3px rgba(0, 82, 217, 0.1) !important;
}

:deep(.auth-form .el-input__inner) {
  height: 52px;
  color: #111;
  font-size: 15px;
  font-weight: 500;
}

:deep(.auth-form .el-input__inner::placeholder) {
  color: #a8abb2;
  font-weight: 400;
}

:deep(.auth-form .el-input__prefix-inner) {
  margin-right: 4px;
  color: #a8abb2;
  font-size: 18px;
}

:deep(.auth-form .submit-item) {
  margin-bottom: 0;
}

:deep(.auth-form .submit-button) {
  width: 100%;
  height: 54px;
  border: none;
  border-radius: 12px;
  background: #0052d9;
  box-shadow: 0 8px 20px rgba(0, 82, 217, 0.2);
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.3s ease;
}

:deep(.auth-form .submit-button:hover) {
  background: #0046b8;
  box-shadow: 0 10px 24px rgba(0, 82, 217, 0.3);
  transform: translateY(-1px);
}

:deep(.auth-form .submit-button:active) {
  transform: translateY(1px);
  box-shadow: 0 4px 10px rgba(0, 82, 217, 0.2);
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(30px);
  }
}

@media screen and (max-width: 960px) {
  .auth-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .brand-panel {
    min-height: 42vh;
    padding: 110px 24px 40px;
  }

  .form-panel {
    padding: 32px 18px 28px;
  }
}

@media screen and (max-width: 640px) {
  .page-header-logo {
    top: 20px;
    left: 20px;
  }

  .brand-panel {
    min-height: 38vh;
    padding-top: 88px;
  }

  .brand-slogan {
    max-width: none;
    font-size: 30px;
  }

  .brand-intro p {
    font-size: 14px;
  }

  .form-panel {
    padding: 18px 14px 24px;
  }

  .form-container {
    padding: 24px 18px;
    border-radius: 24px;
  }

  .form-title {
    font-size: 24px;
  }

  .panel-footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
