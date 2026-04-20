<template>
  <div class="pro-chat" :class="{ 'panel-open': showPanel }">
    <!-- 历史会话侧边栏 -->
    <Transition name="slide-panel">
      <aside v-if="showPanel" class="history-panel">
        <div class="panel-header">
          <span class="panel-title">历史对话</span>
          <button class="new-chat-btn" @click="newChat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <path d="M12 5v14M5 12h14"/>
            </svg>
            新对话
          </button>
        </div>
        <div class="history-list">
          <button
            v-for="h in history"
            :key="h.id"
            class="history-item"
            :class="{ active: h.id === activeId }"
            @click="loadHistory(h.id)"
          >
            <div class="item-indicator"></div>
            <div class="item-content">
              <span class="item-title">{{ h.title }}</span>
              <span class="item-time">{{ h.time }}</span>
            </div>
          </button>
        </div>
      </aside>
    </Transition>

    <!-- 主聊天区域 -->
    <main class="chat-main">
      <!-- 顶部栏 -->
      <header class="top-bar">
        <button class="menu-btn" @click="showPanel = !showPanel">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12h18M3 6h18M3 18h18"/>
          </svg>
        </button>
        
        <div class="ai-identity">
          <div class="ai-logo">
            <span class="logo-text">YU</span>
          </div>
          <div class="ai-info">
            <h1>智能客服助手</h1>
            <span class="ai-status">
              <i class="status-dot"></i>
              在线
            </span>
          </div>
        </div>

        <div class="style-switcher">
          <button
            v-for="s in styles"
            :key="s.value"
            class="style-btn"
            :class="{ active: currentStyle === s.value }"
            @click="currentStyle = s.value"
          >
            {{ s.label }}
          </button>
        </div>
      </header>

      <!-- 消息区域 -->
      <div ref="scrollArea" class="messages-area">
        <TransitionGroup name="msg-fade">
          <div
            v-for="m in msgs"
            :key="m.id"
            class="message-row"
            :class="m.role"
          >
            <!-- AI 消息 -->
            <template v-if="m.role === 'assistant'">
              <div class="msg-avatar ai">
                <span class="avatar-text">YU</span>
              </div>
              <div class="msg-content">
                <div class="msg-header">
                  <span class="sender-name">YU 助手</span>
                  <span v-if="m.model" class="model-tag">{{ m.model }}</span>
                </div>
                <div class="msg-bubble ai">
                  <!-- 打字动画 -->
                  <div v-if="m.streaming && !m.content" class="typing-indicator">
                    <i></i><i></i><i></i>
                  </div>
                  <div
                    v-else
                    class="msg-text"
                    :class="{ error: m.isError }"
                    v-html="md(m.content)"
                  ></div>
                </div>
                <!-- 快捷追问 -->
                <div v-if="!m.streaming && m.content && m.role === 'assistant' && !m.isError" class="follow-ups">
                  <button
                    v-for="(q, idx) in m.followUps || followUpTemplates"
                    :key="idx"
                    class="follow-btn"
                    @click="askFollowUp(q)"
                  >
                    {{ q }}
                  </button>
                </div>
              </div>
            </template>

            <!-- 用户消息 -->
            <template v-else>
              <div class="msg-content user-side">
                <div class="msg-bubble user">
                  <div class="msg-text" v-html="md(m.content || '请分析附件')"></div>
                  <div v-if="m.files?.length" class="attached-files">
                    <span v-for="f in m.files" :key="f.id" class="file-tag" :class="f.kind">
                      {{ f.kind === 'image' ? '🖼' : '📄' }} {{ f.name }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="msg-avatar user">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
              </div>
            </template>
          </div>
        </TransitionGroup>
      </div>

      <!-- 输入区域 -->
      <footer class="input-area">
        <Transition name="pop">
          <div v-if="files.length" class="preview-row">
            <div v-for="f in files" :key="f.id" class="preview-chip">
              <span class="chip-icon">{{ f.kind === 'image' ? '🖼' : '📄' }}</span>
              <span class="chip-name">{{ f.name }}</span>
              <button class="chip-remove" @click="delFile(f.id)">×</button>
            </div>
          </div>
        </Transition>

        <div class="input-container">
          <div class="input-tools">
            <label class="tool-btn">
              <input
                type="file"
                multiple
                accept=".png,.jpg,.jpeg,.webp,.pdf,.doc,.docx,.xls,.xlsx"
                hidden
                :disabled="busy"
                @change="onFilePick"
              />
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
              </svg>
            </label>
            <span class="paste-tip">Ctrl+V 粘贴图片</span>
          </div>

          <textarea
            ref="txtRef"
            v-model="text"
            :placeholder="busy ? '正在思考...' : '输入您的问题...'"
            :disabled="busy"
            rows="1"
            @keydown="onKey"
            @input="resize"
          ></textarea>

          <button
            class="send-btn"
            :class="{ ready: canGo, stop: busy }"
            :disabled="!canGo && !busy"
            @click="busy ? halt() : send()"
          >
            <svg v-if="!busy" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
            </svg>
            <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
              <rect x="6" y="6" width="12" height="12" rx="2"/>
            </svg>
          </button>
        </div>

        <div class="input-hints">
          <span>Enter 发送</span>
          <span>Shift+Enter 换行</span>
        </div>
      </footer>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { isAbortError, streamAiChat, type AiAttachmentPayload } from '@/api/ai'
import { uploadFile } from '@/api/upload'
import { renderMarkdown } from '@/utils/markdown'

interface FileItem {
  id: string
  name: string
  url: string
  contentType?: string
  kind: 'image' | 'document'
  label: string
}

interface Msg {
  id: string
  role: 'assistant' | 'user'
  content: string
  files?: FileItem[]
  model?: string
  isError?: boolean
  streaming?: boolean
  followUps?: string[]
}

interface Hist {
  id: string
  title: string
  time: string
  msgs: Msg[]
}

const MAX_FILES = 5

const router = useRouter()
const text = ref('')
const busy = ref(false)
const uploading = ref(0)
const files = ref<FileItem[]>([])
const txtRef = ref<HTMLTextAreaElement>()
const scrollArea = ref<HTMLElement>()
const ctrl = ref<AbortController | null>(null)
const stopped = ref(false)
const showPanel = ref(false)
const activeId = ref(id())

// 回答风格切换
const styles = [
  { label: '简洁', value: 'brief' },
  { label: '标准', value: 'normal' },
  { label: '详细', value: 'detailed' }
]
const currentStyle = ref('normal')

// 追问模板（前端模拟，后端支持后可替换）
const followUpTemplates = [
  '还有其他建议吗？',
  '能详细说明一下吗？',
  '如何操作？'
]

const msgs = ref<Msg[]>([
  { 
    id: id(), 
    role: 'assistant', 
    content: '您好！我是 YU-Mall 智能助手，可以帮您解答商品咨询、订单查询、售后服务等问题。您也可以发送图片或文档让我分析。',
    followUps: ['如何查询订单？', '如何申请退换货？', '推荐热销商品']
  }
])

const history = ref<Hist[]>([])

const canGo = computed(() =>
  (text.value.trim() || files.value.length > 0) && !busy.value && uploading.value === 0
)

function onFilePick(e: Event) {
  const list = (e.target as HTMLInputElement).files
  if (!list) return
  Array.from(list).forEach(uploadOne)
  ;(e.target as HTMLInputElement).value = ''
}

async function uploadOne(f: File) {
  if (!needLogin('上传')) return
  if (!checkFile(f)) return
  uploading.value++
  try {
    const res: any = await uploadFile(f)
    const url = res?.url || res?.data?.url || res?.data?.previewUrl
    if (!url) throw new Error('上传失败')
    files.value.push({
      id: id(),
      name: f.name,
      url,
      contentType: f.type || mimeOf(f.name),
      kind: f.type.startsWith('image/') ? 'image' : 'document',
      label: f.type.startsWith('image/') ? '图片' : '文档'
    })
  } catch (e: any) {
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    uploading.value--
  }
}

function checkFile(f: File): boolean {
  if (busy.value) { ElMessage.warning('请等待回答结束'); return false }
  const ok = /^image\/|pdf|word|excel|spreadsheet|msword/i.test(f.type) ||
    /\.(png|jpe?g|webp|pdf|docx?|xlsx?)$/i.test(f.name)
  if (!ok) { ElMessage.warning('仅支持图片、PDF、Word、Excel'); return false }
  if (f.size > 10 * 1024 * 1024) { ElMessage.warning('文件需小于 10MB'); return false }
  if (files.value.length + uploading.value >= MAX_FILES) { ElMessage.warning('最多 5 个附件'); return false }
  return true
}

function mimeOf(n: string): string {
  const m: Record<string, string> = {
    png: 'image/png', jpg: 'image/jpeg', jpeg: 'image/jpeg', webp: 'image/webp',
    pdf: 'application/pdf', doc: 'application/msword',
    docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    xls: 'application/vnd.ms-excel',
    xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  }
  return m[n.split('.').pop()?.toLowerCase() || ''] || ''
}

async function onPaste(e: ClipboardEvent) {
  const items = Array.from(e.clipboardData?.items || [])
    .filter(i => i.kind === 'file' && i.type.startsWith('image/'))
    .map(i => i.getAsFile())
    .filter((f): f is File => !!f)
  if (!items.length) return
  e.preventDefault()
  if (!needLogin('粘贴')) return
  for (const f of items) if (checkFile(f)) await uploadOne(f)
}

async function send() {
  const t = text.value.trim()
  if (!t && !files.value.length) return
  if (busy.value || uploading.value > 0) return
  if (!needLogin('发送')) return

  const fs = files.value.map(x => ({ ...x }))
  msgs.value.push({ id: id(), role: 'user', content: t || '请分析附件', files: fs })

  text.value = ''
  files.value = []
  if (txtRef.value) txtRef.value.style.height = 'auto'
  busy.value = true
  stopped.value = false

  const aid = id()
  msgs.value.push({ id: aid, role: 'assistant', content: '', streaming: true })
  down()

  try {
    const payload = {
      message: t,
      model: fs.some(f => f.kind === 'image') ? 'qwen-vl-plus' : undefined,
      temperature: 0.2,
      style: currentStyle.value, // 预留风格参数
      attachments: fs.map<AiAttachmentPayload>(a => ({ url: a.url, fileName: a.name, contentType: a.contentType }))
    }
    ctrl.value = new AbortController()
    await streamAiChat(payload, {
      onStart: ev => { const m = getMsg(aid); if (m) m.model = ev.model || payload.model || '默认模型' },
      onDelta: ev => { const m = getMsg(aid); if (m) { m.content += ev.content || ''; m.streaming = true }; down() },
      onEnd: ev => {
        const m = getMsg(aid)
        if (m) { 
          m.content = ev.reply || m.content || '无内容'
          m.model = ev.model || m.model || payload.model || '默认模型'
          m.streaming = false
          // 生成追问（前端模拟）
          m.followUps = generateFollowUps(m.content)
        }
      },
      onError: ev => {
        const m = getMsg(aid)
        if (m) { m.streaming = false; m.isError = true; m.content = 'AI 服务暂时不可用，请稍后再试' }
        ElMessage.error('AI 服务暂时不可用，请稍后再试')
      }
    }, ctrl.value.signal)

    if (stopped.value) { const m = getMsg(aid); if (m) { m.streaming = false; if (!m.content.trim()) m.content = '已停止' } }
    saveHist()
  } catch (e: any) {
    if (!isAbortError(e)) {
      const m = getMsg(aid)
      if (m) { m.content = 'AI 服务暂时不可用，请稍后再试'; m.isError = true; m.streaming = false }
      ElMessage.error('AI 服务暂时不可用，请稍后再试')
    }
  } finally {
    const m = getMsg(aid); if (m) m.streaming = false
    ctrl.value = null
    busy.value = false
    down()
  }
}

// 前端模拟追问生成（后端支持后替换为 API 调用）
function generateFollowUps(content: string): string[] {
  if (content.includes('订单')) return ['如何查询物流？', '如何取消订单？', '订单支付问题']
  if (content.includes('商品')) return ['查看商品详情', '相关推荐', '库存查询']
  if (content.includes('退') || content.includes('换')) return ['退换货流程', '退款时效', '联系客服']
  return ['还有其他问题吗？', '详细说明一下', '转人工客服']
}

function askFollowUp(q: string) {
  text.value = q
  send()
}

function halt() {
  if (ctrl.value) { stopped.value = true; ctrl.value.abort() }
}

function onKey(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() }
}

function resize() {
  const el = txtRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function down() {
  nextTick(() => { const el = scrollArea.value; if (el) el.scrollTop = el.scrollHeight })
}

function getMsg(id: string) { return msgs.value.find(m => m.id === id) }

function delFile(id: string) { files.value = files.value.filter(f => f.id !== id) }

function needLogin(act: string): boolean {
  if (localStorage.getItem('Authorization')) return true
  ElMessage.warning(`${act}需要登录`)
  router.push('/login')
  return false
}

function id() { return `${Date.now()}-${Math.random().toString(16).slice(2)}` }

function newChat() {
  activeId.value = id()
  msgs.value = [{ 
    id: id(), 
    role: 'assistant', 
    content: '您好！我是 YU-Mall 智能助手，可以帮您解答商品咨询、订单查询、售后服务等问题。',
    followUps: ['如何查询订单？', '如何申请退换货？', '推荐热销商品']
  }]
}

function loadHistory(hid: string) {
  const h = history.value.find(x => x.id === hid)
  if (h) { activeId.value = hid; msgs.value = [...h.msgs] }
}

function saveHist() {
  const title = msgs.value.find(m => m.role === 'user')?.content?.slice(0, 25) || '新对话'
  const item: Hist = {
    id: activeId.value,
    title: title + (title.length >= 25 ? '...' : ''),
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    msgs: [...msgs.value]
  }
  const idx = history.value.findIndex(h => h.id === activeId.value)
  if (idx >= 0) history.value[idx] = item
  else { history.value.unshift(item); if (history.value.length > 15) history.value.pop() }
}

const md = renderMarkdown

onMounted(() => txtRef.value?.addEventListener('paste', onPaste))
onUnmounted(() => txtRef.value?.removeEventListener('paste', onPaste))
watch(() => msgs.value.length, down)
</script>

<style lang="scss">
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Source+Sans+3:wght@400;500;600&display=swap');

.pro-chat {
  --bg-page: #f5f5f7;
  --bg-panel: #ffffff;
  --bg-card: #ffffff;
  --bg-hover: #f0f0f2;
  --bg-active: #e8e8eb;
  --border: rgba(0, 0, 0, 0.08);
  --text-primary: #1d1d1f;
  --text-secondary: #6e6e73;
  --text-tertiary: #aeaeb2;
  --accent: #0071e3;
  --accent-light: rgba(0, 113, 227, 0.08);
  --user-accent: #5856d6;
  --user-light: rgba(88, 86, 214, 0.08);
  --success: #34c759;
  --error: #ff3b30;
  
  position: fixed;
  inset: 0;
  display: flex;
  background: var(--bg-page);
  font-family: 'Source Sans 3', -apple-system, BlinkMacSystemFont, sans-serif;
  color: var(--text-primary);
}

// ========== 历史面板 ==========
.history-panel {
  width: 280px;
  background: var(--bg-panel);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  z-index: 10;
  flex-shrink: 0;
}

.panel-header {
  padding: 20px 16px;
  border-bottom: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-title {
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-tertiary);
}

.new-chat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  background: var(--accent);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  
  &:hover { background: #0077ed; }
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  
  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: var(--border); border-radius: 2px; }
}

.history-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: transparent;
  border: none;
  border-radius: 8px;
  text-align: left;
  cursor: pointer;
  transition: background 0.12s;
  
  &:hover { background: var(--bg-hover); }
  &.active {
    background: var(--bg-active);
    .item-indicator { background: var(--accent); }
  }
}

.item-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--text-tertiary);
  flex-shrink: 0;
  transition: background 0.12s;
}

.item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-time {
  font-size: 11px;
  color: var(--text-tertiary);
}

// ========== 主区域 ==========
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  position: relative;
}

// ========== 顶部栏 ==========
.top-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: var(--bg-panel);
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.menu-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.12s;
  
  &:hover { background: var(--bg-hover); color: var(--text-primary); }
}

.ai-identity {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.ai-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--accent), var(--user-accent));
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 113, 227, 0.2);
}

.logo-text {
  color: white;
  font-family: 'DM Sans', sans-serif;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.ai-info h1 {
  font-family: 'DM Sans', sans-serif;
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 2px;
  color: var(--text-primary);
}

.ai-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--success);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.style-switcher {
  display: flex;
  background: var(--bg-hover);
  border-radius: 8px;
  padding: 3px;
}

.style-btn {
  padding: 6px 14px;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.12s;
  
  &:hover { color: var(--text-primary); }
  &.active {
    background: var(--bg-panel);
    color: var(--text-primary);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  }
}

// ========== 消息区域 ==========
.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 32px 48px;
  scroll-behavior: smooth;
  
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
}

.message-row {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
  
  &.assistant { align-items: flex-start; }
  &.user { align-items: flex-start; flex-direction: row-reverse; }
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  
  &.ai {
    background: linear-gradient(135deg, var(--accent), var(--user-accent));
    color: white;
  }
  
  &.user {
    background: var(--bg-active);
    color: var(--text-secondary);
  }
}

.avatar-text {
  font-family: 'DM Sans', sans-serif;
  font-size: 12px;
  font-weight: 700;
}

.msg-content {
  flex: 1;
  min-width: 0;
  
  &.user-side {
    display: flex;
    justify-content: flex-end;
  }
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.sender-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.model-tag {
  padding: 2px 8px;
  background: var(--bg-hover);
  border-radius: 4px;
  font-size: 10px;
  color: var(--text-tertiary);
}

.msg-bubble {
  max-width: 70%;
  padding: 14px 18px;
  border-radius: 16px;
  
  &.ai {
    background: var(--bg-panel);
    border: 1px solid var(--border);
    border-top-left-radius: 4px;
  }
  
  &.user {
    background: var(--accent);
    color: white;
    border-top-right-radius: 4px;
  }
}

.msg-text {
  font-size: 14px;
  line-height: 1.7;
  
  &.error { color: var(--error); }
  
  :deep(p) { margin: 0 0 10px; }
  :deep(p:last-child) { margin: 0; }
  :deep(ul), :deep(ol) { padding-left: 18px; margin: 0 0 10px; }
  :deep(code) {
    background: var(--bg-hover);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 13px;
    font-family: 'SF Mono', Monaco, monospace;
  }
  :deep(pre) {
    background: var(--text-primary);
    color: #f5f5f7;
    padding: 14px;
    border-radius: 10px;
    overflow-x: auto;
    margin: 0 0 10px;
    
    code { background: transparent; color: inherit; padding: 0; }
  }
  :deep(blockquote) {
    margin: 0 0 10px;
    padding-left: 12px;
    border-left: 3px solid var(--accent);
    color: var(--text-secondary);
  }
  
  .msg-bubble.user & {
    :deep(code) { background: rgba(255, 255, 255, 0.2); }
    :deep(pre) { background: rgba(0, 0, 0, 0.1); }
    :deep(a) { color: rgba(255, 255, 255, 0.9); }
  }
}

.attached-files {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.file-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
}

// 快捷追问
.follow-ups {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.follow-btn {
  padding: 8px 14px;
  background: var(--bg-hover);
  border: 1px solid var(--border);
  border-radius: 20px;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.12s;
  
  &:hover {
    background: var(--accent-light);
    border-color: var(--accent);
    color: var(--accent);
  }
}

// 打字动画
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
  
  i {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--text-tertiary);
    animation: bounce 1.2s ease-in-out infinite;
    
    &:nth-child(2) { animation-delay: 0.15s; }
    &:nth-child(3) { animation-delay: 0.3s; }
  }
}

@keyframes bounce {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-4px); opacity: 1; }
}

// ========== 输入区域 ==========
.input-area {
  padding: 20px 48px 28px;
  background: linear-gradient(180deg, transparent, var(--bg-page));
  flex-shrink: 0;
}

.preview-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.preview-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 12px;
}

.chip-icon { font-size: 14px; }
.chip-name { max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.chip-remove {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 16px;
  cursor: pointer;
  line-height: 1;
  
  &:hover { color: var(--error); }
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-panel);
  border: 1px solid var(--border);
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: border-color 0.15s, box-shadow 0.15s;
  
  &:focus-within {
    border-color: var(--accent);
    box-shadow: 0 2px 16px rgba(0, 113, 227, 0.1);
  }
}

.input-tools {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding-bottom: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: var(--bg-hover);
  border-radius: 8px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.12s;
  
  &:hover { background: var(--accent-light); color: var(--accent); }
}

.paste-tip {
  font-size: 9px;
  color: var(--text-tertiary);
  white-space: nowrap;
}

.input-container textarea {
  flex: 1;
  min-height: 24px;
  max-height: 160px;
  padding: 8px 0;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  
  &::placeholder { color: var(--text-tertiary); }
  &:focus { outline: none; }
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: var(--bg-hover);
  border: none;
  border-radius: 10px;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.15s;
  
  &.ready {
    background: var(--accent);
    color: white;
    box-shadow: 0 2px 10px rgba(0, 113, 227, 0.25);
    
    &:hover { transform: scale(1.05); }
  }
  
  &.stop {
    background: var(--error);
    color: white;
  }
  
  &:disabled { opacity: 0.4; cursor: not-allowed; }
}

.input-hints {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-tertiary);
}

// ========== 过渡动画 ==========
.slide-panel-enter-active, .slide-panel-leave-active { transition: all 0.2s ease; }
.slide-panel-enter-from, .slide-panel-leave-to { transform: translateX(-100%); opacity: 0; }

.pop-enter-active, .pop-leave-active { transition: all 0.15s ease; }
.pop-enter-from, .pop-leave-to { opacity: 0; transform: scale(0.95); }

.msg-fade-enter-active { animation: msgIn 0.25s ease; }
@keyframes msgIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

// ========== 响应式 ==========
@media (max-width: 768px) {
  .history-panel {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    box-shadow: 4px 0 20px rgba(0, 0, 0, 0.08);
  }
  
  .messages-area { padding: 20px 16px; }
  .input-area { padding: 16px; }
  .msg-bubble { max-width: 85%; }
  .top-bar { padding: 12px 16px; }
  
  .style-switcher {
    display: none; // 移动端隐藏风格切换
  }
}
</style>
