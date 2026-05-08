export const FALLBACK_PRODUCT_IMAGE = '/placeholder-image.svg'

const KNOWN_UNSTABLE_IMAGE_PATTERNS = [
  /300x300\.png\?text=/i,
  /via\.placeholder\.com/i,
  /dummyimage\.com/i,
  /placehold\.co/i
]

export const resolveProductImage = (image?: string | null) => {
  const normalized = typeof image === 'string' ? image.trim() : ''
  if (!normalized) {
    return FALLBACK_PRODUCT_IMAGE
  }
  return KNOWN_UNSTABLE_IMAGE_PATTERNS.some((pattern) => pattern.test(normalized))
    ? FALLBACK_PRODUCT_IMAGE
    : normalized
}

export const applyProductImageFallback = (event: Event) => {
  const target = event.target as HTMLImageElement | null
  if (!target) {
    return
  }
  if (target.getAttribute('src') === FALLBACK_PRODUCT_IMAGE) {
    return
  }
  target.setAttribute('src', FALLBACK_PRODUCT_IMAGE)
}
