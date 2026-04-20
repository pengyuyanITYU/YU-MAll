<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'
import type { SliderCaptchaChallengeResponse, SliderCaptchaTrackPoint, SliderCaptchaVerifyRequest } from '@/api/captcha'

const props = defineProps<{
  modelValue: boolean
  challenge: SliderCaptchaChallengeResponse | null
  loading: boolean
  verifying: boolean
  errorMessage?: string
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'refresh'): void
  (event: 'submit', payload: SliderCaptchaVerifyRequest): void
}>()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const trackRef = ref<HTMLElement | null>(null)
const dragging = ref(false)
const dragStartX = ref(0)
const dragStartTime = ref(0)
const offsetX = ref(0)
const localMessage = ref('')

const fallbackTrackWidth = 320
const fallbackHandleWidth = 56

const trackWidth = computed(() => props.challenge?.trackWidth || fallbackTrackWidth)
const handleWidth = computed(() => props.challenge?.handleWidth || fallbackHandleWidth)
const maxOffset = computed(() => Math.max(trackWidth.value - handleWidth.value, 0))

const displayScale = computed(() => {
  const displayWidth = trackRef.value?.clientWidth || trackWidth.value
  return displayWidth / trackWidth.value
})

const displayHandleWidth = computed(() => Math.round(handleWidth.value * displayScale.value))
const displayMaxOffset = computed(() => Math.max((trackRef.value?.clientWidth || trackWidth.value) - displayHandleWidth.value, 0))

const displayTargetLeft = computed(() => Math.round((props.challenge?.targetLeft || 0) * displayScale.value))
const displayTargetWidth = computed(() => Math.max(Math.round((props.challenge?.targetWidth || 0) * displayScale.value), 44))

const targetStyle = computed(() => ({
  left: `${displayTargetLeft.value}px`,
  width: `${displayTargetWidth.value}px`
}))

const progressStyle = computed(() => ({
  width: `${Math.min(offsetX.value + displayHandleWidth.value, (trackRef.value?.clientWidth || trackWidth.value))}px`
}))

const handleStyle = computed(() => ({
  width: `${displayHandleWidth.value}px`,
  transform: `translateX(${offsetX.value}px)`
}))

const statusText = computed(() => {
  if (props.verifying) {
    return '正在校验'
  }
  if (localMessage.value) {
    return localMessage.value
  }
  if (props.errorMessage) {
    return props.errorMessage
  }
  return '拖动滑块到高亮区域'
})

const isErrorState = computed(() => Boolean(localMessage.value || props.errorMessage))

const clamp = (value: number, min: number, max: number) => Math.min(Math.max(value, min), max)

const clearLocalState = () => {
  dragging.value = false
  dragStartX.value = 0
  dragStartTime.value = 0
  offsetX.value = 0
  localMessage.value = ''
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', handlePointerUp)
}

const toSourceX = (displayX: number) => {
  const width = trackRef.value?.clientWidth || trackWidth.value
  return Math.round((displayX * trackWidth.value) / width)
}

const buildTrackPoints = (finalX: number, duration: number): SliderCaptchaTrackPoint[] => {
  const safeDuration = Math.max(duration, 130)
  return Array.from({ length: 8 }, (_, index) => {
    const ratio = index / 7
    return {
      x: Math.round(finalX * ratio),
      y: 0,
      t: Math.max(index === 0 ? 0 : 1, Math.round(safeDuration * ratio))
    }
  })
}

function handlePointerMove(event: PointerEvent) {
  if (!dragging.value) {
    return
  }
  const deltaX = clamp(event.clientX - dragStartX.value, 0, displayMaxOffset.value)
  offsetX.value = Math.round(deltaX)
}

function handlePointerUp() {
  if (!dragging.value || !props.challenge) {
    clearLocalState()
    return
  }

  dragging.value = false
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', handlePointerUp)

  const duration = Date.now() - dragStartTime.value
  const releaseX = offsetX.value
  const targetMin = displayTargetLeft.value
  const targetMax = displayTargetLeft.value + displayTargetWidth.value

  if (releaseX < targetMin || releaseX > targetMax) {
    localMessage.value = '未进入目标区域，请再试一次'
    offsetX.value = 0
    return
  }

  const sourceX = toSourceX(releaseX)
  emit('submit', {
    captchaId: props.challenge.captchaId,
    offsetX: sourceX,
    dragTrack: buildTrackPoints(sourceX, duration)
  })
}

const handlePointerDown = (event: PointerEvent) => {
  if (props.loading || props.verifying || !props.challenge) {
    return
  }
  localMessage.value = ''
  dragging.value = true
  dragStartX.value = event.clientX - offsetX.value
  dragStartTime.value = Date.now()
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', handlePointerUp)
}

watch(
  () => props.challenge?.captchaId,
  () => {
    clearLocalState()
  }
)

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      clearLocalState()
    }
  }
)
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="完成安全验证"
    width="420px"
    destroy-on-close
    append-to-body
    :close-on-click-modal="false"
  >
    <div class="captcha-dialog">
      <div class="captcha-header">
        <div>
          <p class="captcha-title">拖动滑块到高亮区域</p>
          <p class="captcha-subtitle">通过后会自动继续当前操作</p>
        </div>
        <el-button link type="primary" :icon="RefreshRight" :disabled="loading || verifying" @click="$emit('refresh')">
          换一题
        </el-button>
      </div>

      <div class="captcha-card">
        <div class="card-topline">
          <span class="scene-tag">安全校验</span>
          <span class="hint-text">松手即可完成</span>
        </div>

        <div ref="trackRef" class="captcha-track">
          <div class="track-progress" :style="progressStyle"></div>
          <div class="track-target" :style="targetStyle"></div>
          <button
            type="button"
            class="track-handle"
            :class="{ verifying }"
            :style="handleStyle"
            :disabled="loading || verifying"
            @pointerdown.prevent="handlePointerDown"
          >
            <span>{{ verifying ? '...' : '>>' }}</span>
          </button>
        </div>

        <div class="status-line" :class="{ error: isErrorState }">
          {{ statusText }}
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped lang="scss">
.captcha-dialog {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.captcha-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.captcha-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.captcha-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.captcha-card {
  padding: 18px;
  border: 1px solid #dbe7ff;
  border-radius: 18px;
  background: linear-gradient(180deg, #f8fbff 0%, #f2f7ff 100%);
}

.card-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.scene-tag {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(22, 96, 220, 0.1);
  color: #1660dc;
  font-size: 12px;
  font-weight: 700;
}

.hint-text {
  font-size: 12px;
  color: #8a93a3;
}

.captcha-track {
  position: relative;
  height: 56px;
  border-radius: 18px;
  overflow: hidden;
  border: 1px solid #d9e6ff;
  background: linear-gradient(90deg, #f8fbff 0%, #eef4ff 100%);
}

.track-progress {
  position: absolute;
  inset: 0 auto 0 0;
  min-width: 56px;
  background: linear-gradient(90deg, rgba(22, 96, 220, 0.14), rgba(22, 96, 220, 0.22));
  transition: width 0.15s ease;
}

.track-target {
  position: absolute;
  top: 8px;
  bottom: 8px;
  border-radius: 14px;
  border: 1px dashed rgba(22, 96, 220, 0.45);
  background: rgba(22, 96, 220, 0.12);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.3);
}

.track-handle {
  position: absolute;
  inset: 0 auto 0 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 16px;
  background: linear-gradient(180deg, #1660dc 0%, #0f57d3 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 800;
  cursor: grab;
  touch-action: none;
  box-shadow: 0 14px 24px rgba(22, 96, 220, 0.18);
}

.track-handle.verifying {
  cursor: progress;
}

.track-handle:disabled {
  opacity: 0.78;
  cursor: not-allowed;
}

.status-line {
  margin-top: 14px;
  font-size: 13px;
  color: #516074;
}

.status-line.error {
  color: #d14343;
}
</style>
