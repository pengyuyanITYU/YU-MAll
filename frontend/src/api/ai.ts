export interface AiAttachmentPayload {
  url: string
  fileName?: string
  contentType?: string
}

export interface AiChatPayload {
  message?: string
  model?: string
  temperature?: number
  attachments?: AiAttachmentPayload[]
}

export interface AiStreamStartEvent {
  messageId?: string
  model?: string
}

export interface AiStreamDeltaEvent {
  content?: string
}

export interface AiStreamEndEvent {
  messageId?: string
  model?: string
  reply?: string
}

export interface AiStreamErrorEvent {
  message?: string
}

interface ParsedSseEvent {
  event: string
  data: string
}

interface ParsedSseResult {
  events: ParsedSseEvent[]
  remainder: string
}

export interface AiStreamHandlers {
  onStart?: (payload: AiStreamStartEvent) => void
  onDelta?: (payload: AiStreamDeltaEvent) => void
  onEnd?: (payload: AiStreamEndEvent) => void
  onError?: (payload: AiStreamErrorEvent) => void
}

export async function streamAiChat(
  data: AiChatPayload,
  handlers: AiStreamHandlers,
  signal?: AbortSignal
) {
  const token = localStorage.getItem('Authorization')
  const response = await fetch('/api/ai/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      ...(token ? { Authorization: token } : {})
    },
    body: JSON.stringify(data),
    signal
  })

  if (!response.ok) {
    throw new Error(await resolveErrorMessage(response))
  }

  const contentType = response.headers.get('content-type') || ''
  if (!contentType.includes('text/event-stream')) {
    throw new Error(await resolveErrorMessage(response))
  }

  if (!response.body) {
    throw new Error('AI service returned no stream body')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        break
      }
      buffer += decoder.decode(value, { stream: true })
      const parsed = parseSseBuffer(buffer)
      buffer = parsed.remainder
      dispatchEvents(parsed.events, handlers)
    }
    buffer += decoder.decode()
    const parsed = parseSseBuffer(`${buffer}\n\n`)
    dispatchEvents(parsed.events, handlers)
  } catch (error) {
    if (isAbortError(error)) {
      return
    }
    throw error
  } finally {
    reader.releaseLock()
  }
}

export function isAbortError(error: unknown) {
  return error instanceof DOMException && error.name === 'AbortError'
}

function dispatchEvents(events: ParsedSseEvent[], handlers: AiStreamHandlers) {
  for (const item of events) {
    const payload = parseJsonPayload(item.data)
    if (item.event === 'start') {
      handlers.onStart?.(payload)
      continue
    }
    if (item.event === 'delta') {
      handlers.onDelta?.(payload)
      continue
    }
    if (item.event === 'end') {
      handlers.onEnd?.(payload)
      continue
    }
    if (item.event === 'error') {
      handlers.onError?.(payload)
    }
  }
}

function parseSseBuffer(buffer: string): ParsedSseResult {
  const normalized = buffer.replace(/\r/g, '')
  const boundaryIndex = normalized.lastIndexOf('\n\n')
  if (boundaryIndex === -1) {
    return {
      events: [],
      remainder: normalized
    }
  }

  const complete = normalized.slice(0, boundaryIndex)
  const remainder = normalized.slice(boundaryIndex + 2)
  const events = complete
    .split('\n\n')
    .map(parseSseEventBlock)
    .filter((item): item is ParsedSseEvent => item !== null)

  return {
    events,
    remainder
  }
}

function parseSseEventBlock(block: string): ParsedSseEvent | null {
  const lines = block.split('\n')
  let event = 'message'
  const dataLines: string[] = []

  for (const line of lines) {
    if (!line || line.startsWith(':')) {
      continue
    }
    if (line.startsWith('event:')) {
      event = line.slice(6).trim()
      continue
    }
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }

  if (!dataLines.length) {
    return null
  }

  return {
    event,
    data: dataLines.join('\n')
  }
}

function parseJsonPayload(data: string) {
  try {
    return data ? JSON.parse(data) : {}
  } catch {
    return {}
  }
}

async function resolveErrorMessage(response: Response) {
  const contentType = response.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    try {
      const data = await response.json()
      return data?.msg || data?.message || `Request failed with status ${response.status}`
    } catch {
      return `Request failed with status ${response.status}`
    }
  }

  try {
    const text = await response.text()
    return text || `Request failed with status ${response.status}`
  } catch {
    return `Request failed with status ${response.status}`
  }
}
