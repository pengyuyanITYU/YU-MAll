<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthPreviewStage from '@/components/auth-preview/AuthPreviewStage.vue'
import AuthPreviewToolbar from '@/components/auth-preview/AuthPreviewToolbar.vue'
import {
  authPreviewThemes,
  defaultAuthPreviewThemeId,
  type AuthPreviewMode,
  type AuthPreviewThemeId
} from '@/components/auth-preview/authPreviewThemes'

const route = useRoute()
const router = useRouter()

const resolveMode = (value: unknown): AuthPreviewMode => {
  return value === 'register' ? 'register' : 'login'
}

const resolveThemeId = (value: unknown): AuthPreviewThemeId => {
  return authPreviewThemes.some(theme => theme.id === value)
    ? (value as AuthPreviewThemeId)
    : defaultAuthPreviewThemeId
}

const activeMode = computed<AuthPreviewMode>(() => resolveMode(route.query.mode))
const activeThemeId = computed<AuthPreviewThemeId>(() => resolveThemeId(route.query.theme))
const activeTheme = computed(() => {
  return authPreviewThemes.find(theme => theme.id === activeThemeId.value) ?? authPreviewThemes[0]
})

const updateQuery = (payload: Partial<{ mode: AuthPreviewMode; theme: AuthPreviewThemeId }>) => {
  router.replace({
    query: {
      ...route.query,
      mode: payload.mode ?? activeMode.value,
      theme: payload.theme ?? activeThemeId.value
    }
  })
}
</script>

<template>
  <div class="auth-preview-lab">
    <AuthPreviewToolbar
      :themes="authPreviewThemes"
      :active-theme-id="activeThemeId"
      :active-mode="activeMode"
      @update:theme="updateQuery({ theme: $event })"
      @update:mode="updateQuery({ mode: $event })"
    />

    <AuthPreviewStage :theme="activeTheme" :mode="activeMode" />
  </div>
</template>

<style scoped>
.auth-preview-lab {
  min-height: 100vh;
  padding: 32px;
  background:
    radial-gradient(circle at top left, rgba(255, 230, 196, 0.55), transparent 24%),
    linear-gradient(180deg, #fbf7f1 0%, #f1ede7 100%);
  display: grid;
  gap: 28px;
}

@media (max-width: 760px) {
  .auth-preview-lab {
    padding: 18px;
    gap: 20px;
  }
}
</style>
