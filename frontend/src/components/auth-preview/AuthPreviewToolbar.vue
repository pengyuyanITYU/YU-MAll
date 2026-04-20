<script setup lang="ts">
import type { AuthPreviewMode, AuthPreviewTheme, AuthPreviewThemeId } from './authPreviewThemes'

const props = defineProps<{
  themes: AuthPreviewTheme[]
  activeThemeId: AuthPreviewThemeId
  activeMode: AuthPreviewMode
}>()

const emit = defineEmits<{
  (event: 'update:theme', value: AuthPreviewThemeId): void
  (event: 'update:mode', value: AuthPreviewMode): void
}>()

const previewModes: AuthPreviewMode[] = ['login', 'register']
</script>

<template>
  <section class="preview-toolbar">
    <div class="toolbar-copy">
      <span class="eyebrow">YU-MALL / AUTH PREVIEW</span>
      <h1>登录与注册界面 {{ props.themes.length }} 套视觉预览</h1>
      <p>先看风格，再决定替换正式 `/login` 和 `/register`。</p>
    </div>

    <div class="toolbar-panels">
      <div class="mode-switch">
        <button
          v-for="modeItem in previewModes"
          :key="modeItem"
          type="button"
          class="mode-button"
          :class="{ active: activeMode === modeItem }"
          @click="emit('update:mode', modeItem)"
        >
          {{ modeItem === 'login' ? '登录预览' : '注册预览' }}
        </button>
      </div>

      <div class="theme-list">
        <button
          v-for="theme in themes"
          :key="theme.id"
          type="button"
          class="theme-card"
          :class="{ active: activeThemeId === theme.id }"
          @click="emit('update:theme', theme.id)"
        >
          <strong>{{ theme.name }}</strong>
          <span>{{ theme.tag }}</span>
          <small>{{ theme.summary }}</small>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.preview-toolbar {
  display: grid;
  gap: 24px;
}

.toolbar-copy {
  display: grid;
  gap: 10px;
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: #7d7468;
}

.toolbar-copy h1 {
  margin: 0;
  font-size: clamp(30px, 3vw, 42px);
  line-height: 1.1;
  color: #1f1b16;
}

.toolbar-copy p {
  margin: 0;
  color: #645b4f;
  font-size: 15px;
}

.toolbar-panels {
  display: grid;
  gap: 16px;
}

.mode-switch {
  display: inline-flex;
  gap: 10px;
  flex-wrap: wrap;
}

.mode-button {
  border: 1px solid rgba(30, 30, 30, 0.12);
  background: rgba(255, 255, 255, 0.72);
  color: #3b3228;
  padding: 12px 18px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-size: 14px;
}

.mode-button.active {
  background: #1f1b16;
  color: #fff;
  border-color: #1f1b16;
  box-shadow: 0 10px 22px rgba(31, 27, 22, 0.18);
}

.theme-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.theme-card {
  display: grid;
  gap: 6px;
  text-align: left;
  border: 1px solid rgba(30, 30, 30, 0.1);
  background: rgba(255, 255, 255, 0.8);
  border-radius: 22px;
  padding: 18px;
  cursor: pointer;
  transition: all 0.25s ease;
  color: #2a2219;
}

.theme-card strong {
  font-size: 16px;
}

.theme-card span,
.theme-card small {
  color: #70665b;
}

.theme-card.active {
  transform: translateY(-3px);
  border-color: rgba(30, 30, 30, 0.2);
  box-shadow: 0 18px 28px rgba(29, 22, 14, 0.12);
}

@media (max-width: 960px) {
  .theme-list {
    grid-template-columns: 1fr;
  }
}
</style>
