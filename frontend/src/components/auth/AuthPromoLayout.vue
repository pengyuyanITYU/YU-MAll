<script setup lang="ts">
import { computed, useSlots } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { authPromoContent, type AuthPromoMode } from './authPromoContent'

const props = defineProps<{
  mode: AuthPromoMode
  formTitle: string
  formDescription: string
  footerText: string
  footerActionText: string
  footerActionTo: string
}>()

const slots = useSlots()
const content = computed(() => authPromoContent[props.mode])
const hasHelper = computed(() => Boolean(slots.helper))
</script>

<template>
  <section class="auth-promo-page" :class="`mode-${mode}`">
    <div class="bg-glow glow-a"></div>
    <div class="bg-glow glow-b"></div>
    <div class="bg-grid"></div>

    <div class="auth-shell">
      <aside class="hero-panel">
        <span class="hero-eyebrow">{{ content.eyebrow }}</span>
        <h1>{{ content.title }}</h1>
        <p class="hero-subtitle">{{ content.subtitle }}</p>
        <p class="hero-note">{{ content.note }}</p>

        <div class="hero-chip-row">
          <span v-for="chip in content.chips" :key="chip" class="hero-chip">
            {{ chip }}
          </span>
        </div>

        <div class="hero-sale-board">
          <span class="sale-label">{{ content.offerLabel }}</span>
          <strong class="sale-value">{{ content.offerValue }}</strong>
          <span class="sale-note">{{ content.offerNote }}</span>
        </div>

        <div class="stat-grid">
          <article v-for="stat in content.stats" :key="stat.label" class="stat-card">
            <strong>{{ stat.value }}</strong>
            <span>{{ stat.label }}</span>
          </article>
        </div>

        <div class="promo-grid">
          <article v-for="card in content.cards" :key="card.title" class="promo-card">
            <span class="promo-label">{{ card.label }}</span>
            <strong>{{ card.title }}</strong>
            <p>{{ card.description }}</p>
          </article>
        </div>
      </aside>

      <div class="form-column">
        <div class="form-card">
          <div class="panel-top">
            <span class="panel-badge">{{ content.panelBadge }}</span>
            <div class="panel-tags">
              <span v-for="chip in content.chips.slice(0, 2)" :key="chip">{{ chip }}</span>
            </div>
          </div>

          <h2>{{ formTitle }}</h2>
          <p class="form-description">{{ formDescription }}</p>

          <slot />

          <div v-if="hasHelper" class="helper-block">
            <slot name="helper" />
          </div>

          <div class="panel-footer">
            <span>{{ footerText }}</span>
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
.auth-promo-page {
  --promo-bg: linear-gradient(135deg, #fff6ef 0%, #ffe8db 48%, #ffc8b5 100%);
  --promo-surface: rgba(255, 252, 250, 0.9);
  --promo-border: rgba(241, 106, 79, 0.16);
  --promo-text: #311a15;
  --promo-muted: #7f564d;
  --promo-accent: #f56c6c;
  --promo-accent-strong: #ff7a45;
  --promo-soft: rgba(245, 108, 108, 0.12);
  --promo-shadow: 0 28px 60px rgba(193, 95, 73, 0.22);
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  padding: 36px 28px;
  background: var(--promo-bg);
  color: var(--promo-text);
}

.mode-register {
  --promo-bg: linear-gradient(135deg, #fff8f1 0%, #ffe6d7 42%, #ffcab0 100%);
  --promo-accent: #ff7a45;
  --promo-accent-strong: #ff5f45;
  --promo-soft: rgba(255, 122, 69, 0.12);
}

.bg-glow,
.bg-grid {
  pointer-events: none;
  position: absolute;
}

.bg-glow {
  border-radius: 999px;
  filter: blur(24px);
  opacity: 0.55;
}

.glow-a {
  top: -100px;
  right: -80px;
  width: 300px;
  height: 300px;
  background: rgba(255, 122, 69, 0.18);
}

.glow-b {
  bottom: -120px;
  left: -60px;
  width: 360px;
  height: 360px;
  background: rgba(245, 108, 108, 0.2);
}

.bg-grid {
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.14) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.14) 1px, transparent 1px);
  background-size: 84px 84px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.72), transparent);
}

.auth-shell {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(360px, 500px);
  align-items: center;
  gap: 28px;
  max-width: 1320px;
  min-height: calc(100vh - 72px);
  margin: 0 auto;
}

.hero-panel {
  display: grid;
  gap: 22px;
  padding: 18px 12px 18px 8px;
}

.hero-eyebrow {
  display: inline-flex;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.52);
  border: 1px solid rgba(255, 255, 255, 0.62);
  font-size: 12px;
  letter-spacing: 0.18em;
  color: var(--promo-muted);
}

.hero-panel h1 {
  margin: 0;
  max-width: 12ch;
  font-size: clamp(40px, 5.2vw, 68px);
  line-height: 1.04;
  font-weight: 700;
}

.hero-subtitle,
.hero-note {
  max-width: 640px;
  margin: 0;
  line-height: 1.8;
}

.hero-subtitle {
  font-size: 16px;
}

.hero-note {
  color: var(--promo-muted);
  font-size: 14px;
}

.hero-chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.62);
  color: #7d433b;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 10px 22px rgba(226, 127, 102, 0.14);
}

.hero-sale-board {
  display: grid;
  gap: 6px;
  width: min(100%, 360px);
  padding: 22px 24px;
  border-radius: 28px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(255, 242, 237, 0.88));
  border: 1px solid rgba(255, 255, 255, 0.86);
  box-shadow: var(--promo-shadow);
}

.sale-label,
.sale-note {
  color: var(--promo-muted);
}

.sale-label {
  font-size: 13px;
  letter-spacing: 0.08em;
}

.sale-value {
  font-size: clamp(34px, 4vw, 48px);
  line-height: 1;
  color: var(--promo-accent-strong);
}

.sale-note {
  font-size: 14px;
}

.stat-grid,
.promo-grid {
  display: grid;
  gap: 14px;
}

.stat-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.promo-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.stat-card,
.promo-card {
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.56);
  background: rgba(255, 255, 255, 0.52);
  backdrop-filter: blur(12px);
}

.stat-card {
  display: grid;
  gap: 6px;
  padding: 18px;
}

.stat-card strong {
  font-size: 24px;
  color: var(--promo-accent-strong);
}

.stat-card span {
  color: var(--promo-muted);
  font-size: 13px;
}

.promo-card {
  display: grid;
  gap: 10px;
  padding: 20px;
}

.promo-label {
  width: fit-content;
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--promo-soft);
  color: var(--promo-accent-strong);
  font-size: 12px;
  font-weight: 700;
}

.promo-card strong {
  font-size: 18px;
}

.promo-card p {
  margin: 0;
  color: var(--promo-muted);
  font-size: 14px;
  line-height: 1.7;
}

.form-column {
  display: flex;
  justify-content: center;
}

.form-card {
  width: min(100%, 500px);
  display: grid;
  gap: 18px;
  padding: 28px;
  border-radius: 30px;
  background: var(--promo-surface);
  border: 1px solid var(--promo-border);
  box-shadow: var(--promo-shadow);
  backdrop-filter: blur(18px);
}

.panel-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-badge {
  display: inline-flex;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--promo-accent), var(--promo-accent-strong));
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.panel-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.panel-tags span {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
  color: var(--promo-muted);
  font-size: 12px;
}

.form-card h2 {
  margin: 0;
  font-size: 30px;
  line-height: 1.2;
}

.form-description {
  margin: 0;
  color: var(--promo-muted);
  font-size: 14px;
  line-height: 1.8;
}

.helper-block {
  margin-top: -2px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(248, 130, 103, 0.12);
}

.panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--promo-muted);
  font-size: 14px;
}

.panel-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--promo-accent-strong);
  font-weight: 700;
  text-decoration: none;
}

:deep(.auth-form) {
  display: grid;
  gap: 2px;
}

:deep(.auth-form .el-form-item) {
  margin-bottom: 16px;
}

:deep(.auth-form .el-input__wrapper) {
  min-height: 52px;
  border-radius: 16px;
  background: #fff7f4;
  box-shadow: 0 0 0 1px rgba(245, 108, 108, 0.1) inset;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

:deep(.auth-form .el-input__wrapper:hover) {
  transform: translateY(-1px);
}

:deep(.auth-form .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px rgba(255, 122, 69, 0.5) inset, 0 0 0 4px rgba(255, 122, 69, 0.1);
}

:deep(.auth-form .el-input__inner) {
  color: var(--promo-text);
}

:deep(.auth-form .el-input__prefix-inner) {
  color: #d27461;
}

:deep(.auth-form .el-checkbox) {
  height: auto;
}

:deep(.auth-form .el-checkbox__label) {
  color: var(--promo-muted);
}

:deep(.auth-form .submit-item) {
  margin-bottom: 0;
}

:deep(.auth-form .submit-button) {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 18px;
  background: linear-gradient(135deg, var(--promo-accent), var(--promo-accent-strong));
  box-shadow: 0 16px 28px rgba(245, 108, 108, 0.26);
  font-size: 16px;
  font-weight: 700;
}

@media (max-width: 1100px) {
  .auth-shell {
    grid-template-columns: 1fr;
    gap: 22px;
  }

  .hero-panel {
    padding: 8px 2px 0;
  }

  .hero-panel h1 {
    max-width: none;
  }

  .promo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .auth-promo-page {
    padding: 18px 14px 24px;
  }

  .auth-shell {
    min-height: auto;
  }

  .hero-panel {
    gap: 18px;
  }

  .hero-panel h1 {
    font-size: 36px;
  }

  .stat-grid,
  .promo-grid {
    grid-template-columns: 1fr;
  }

  .form-card {
    padding: 22px 18px;
    border-radius: 24px;
  }

  .panel-top,
  .panel-footer {
    align-items: flex-start;
    flex-direction: column;
  }

  .panel-tags {
    justify-content: flex-start;
  }
}
</style>
