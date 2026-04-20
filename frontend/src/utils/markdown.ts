function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function renderInline(text: string) {
  let html = escapeHtml(text)
  html = html.replace(/\[([^\]]+)]\((https?:\/\/[^\s)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')
  html = html.replace(/`([^`\n]+)`/g, '<code>$1</code>')
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/__([^_]+)__/g, '<strong>$1</strong>')
  html = html.replace(/\*([^*\n]+)\*/g, '<em>$1</em>')
  html = html.replace(/_([^_\n]+)_/g, '<em>$1</em>')
  return html
}

export function renderMarkdown(source: string) {
  if (!source) {
    return ''
  }

  const lines = source.replace(/\r/g, '').split('\n')
  const html: string[] = []
  let paragraphLines: string[] = []
  let listType: '' | 'ul' | 'ol' = ''
  let quoteLines: string[] = []
  let codeFenceLanguage = ''
  let codeLines: string[] = []

  const flushParagraph = () => {
    if (!paragraphLines.length) {
      return
    }
    html.push(`<p>${paragraphLines.map(renderInline).join('<br>')}</p>`)
    paragraphLines = []
  }

  const flushList = () => {
    if (!listType) {
      return
    }
    html.push(`</${listType}>`)
    listType = ''
  }

  const flushQuote = () => {
    if (!quoteLines.length) {
      return
    }
    html.push(`<blockquote><p>${quoteLines.map(renderInline).join('<br>')}</p></blockquote>`)
    quoteLines = []
  }

  const flushCodeBlock = () => {
    if (!codeLines.length && !codeFenceLanguage) {
      return
    }
    const languageClass = codeFenceLanguage ? ` class="language-${escapeHtml(codeFenceLanguage)}"` : ''
    html.push(`<pre><code${languageClass}>${escapeHtml(codeLines.join('\n'))}</code></pre>`)
    codeFenceLanguage = ''
    codeLines = []
  }

  const flushBlocks = () => {
    flushParagraph()
    flushQuote()
    flushList()
  }

  for (const line of lines) {
    if (codeFenceLanguage !== '') {
      if (line.startsWith('```')) {
        flushCodeBlock()
      } else {
        codeLines.push(line)
      }
      continue
    }

    const codeFenceMatch = line.match(/^```([\w-]*)\s*$/)
    if (codeFenceMatch) {
      flushBlocks()
      codeFenceLanguage = codeFenceMatch[1] || 'plain'
      codeLines = []
      continue
    }

    const headingMatch = line.match(/^(#{1,6})\s+(.+)$/)
    if (headingMatch) {
      flushBlocks()
      const level = headingMatch[1].length
      html.push(`<h${level}>${renderInline(headingMatch[2])}</h${level}>`)
      continue
    }

    const quoteMatch = line.match(/^>\s?(.*)$/)
    if (quoteMatch) {
      flushParagraph()
      flushList()
      quoteLines.push(quoteMatch[1])
      continue
    }

    const unorderedMatch = line.match(/^[-*]\s+(.+)$/)
    if (unorderedMatch) {
      flushParagraph()
      flushQuote()
      if (listType !== 'ul') {
        flushList()
        listType = 'ul'
        html.push('<ul>')
      }
      html.push(`<li>${renderInline(unorderedMatch[1])}</li>`)
      continue
    }

    const orderedMatch = line.match(/^\d+\.\s+(.+)$/)
    if (orderedMatch) {
      flushParagraph()
      flushQuote()
      if (listType !== 'ol') {
        flushList()
        listType = 'ol'
        html.push('<ol>')
      }
      html.push(`<li>${renderInline(orderedMatch[1])}</li>`)
      continue
    }

    if (!line.trim()) {
      flushBlocks()
      continue
    }

    paragraphLines.push(line)
  }

  if (codeFenceLanguage !== '') {
    flushCodeBlock()
  } else {
    flushBlocks()
  }

  return html.join('')
}
