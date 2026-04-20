<template>
  <button class="ai-entry" type="button" @click="openAssistant">
    <span class="entry-glow"></span>
    <el-icon class="entry-icon"><ChatDotRound /></el-icon>
    <span class="entry-text">
      <strong>AI 助手</strong>
      <small>图片和文档都能问</small>
    </span>
  </button>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()

const openAssistant = () => {
  router.push('/ai-assistant')
}
</script>

<style scoped lang="scss">
.ai-entry {
  position: fixed;
  right: 24px;
  bottom: 28px;
  z-index: 1200;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  color: #fff;
  background: linear-gradient(135deg, #1d8cf8 0%, #16c79a 100%);
  box-shadow: 0 18px 40px rgba(29, 140, 248, 0.28);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-3px) scale(1.01);
    box-shadow: 0 22px 46px rgba(22, 199, 154, 0.3);
  }
}

.entry-glow {
  position: absolute;
  inset: 1px;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.24), rgba(255, 255, 255, 0));
  pointer-events: none;
}

.entry-icon,
.entry-text {
  position: relative;
  z-index: 1;
}

.entry-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  font-size: 20px;
  backdrop-filter: blur(8px);
}

.entry-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.05;

  strong {
    font-size: 15px;
    font-weight: 800;
    letter-spacing: 0.02em;
  }

  small {
    margin-top: 4px;
    font-size: 11px;
    opacity: 0.86;
  }
}

@media (max-width: 768px) {
  .ai-entry {
    right: 16px;
    bottom: 20px;
    padding: 0;
    width: 58px;
    height: 58px;
    justify-content: center;
  }

  .entry-icon {
    width: 44px;
    height: 44px;
    margin: 0;
  }

  .entry-text {
    display: none;
  }
}
</style>