<script setup lang="ts">
import { computed, onMounted, reactive, ref, shallowRef, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatLineRound, Goods, House, ShoppingCart, Van } from '@element-plus/icons-vue'
import { addItem2Cart } from '@/api/cart'
import { type CommentVO, listComments } from '@/api/comment'
import { itemApi, type ItemDetailModel, type ItemSkuModel, type ItemSpecTemplateItem } from '@/api/item'
import { isHandledRequestError } from '@/utils/request'

const route = useRoute()
const router = useRouter()

const loading = shallowRef(false)
const commentsLoading = shallowRef(false)
const actionLoading = shallowRef(false)
const quantity = shallowRef(1)
const previewImage = shallowRef('')

const itemData = ref<ItemDetailModel | null>(null)
const comments = ref<CommentVO[]>([])
const selectedSpecs = reactive<Record<string, string>>({})

type SkuImageThumb = {
  key: string
  image: string
  sku: ItemSkuModel
  skuIds: number[]
}

const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') {
    return '0.00'
  }
  return (Number(price) / 100).toFixed(2)
}

const normalizeImageUrl = (image?: string | null) => {
  return image?.trim() || ''
}

const getItemId = () => Number(route.params.id)

const buildSpecTemplateFromSkus = (skuList?: ItemSkuModel[]) => {
  if (!Array.isArray(skuList) || !skuList.length) {
    return []
  }
  const map = new Map<string, Set<string>>()
  skuList.forEach((sku) => {
    Object.entries(sku.specs || {}).forEach(([key, value]) => {
      if (!map.has(key)) {
        map.set(key, new Set())
      }
      map.get(key)?.add(value)
    })
  })
  return Array.from(map.entries()).map(([name, values]) => ({
    name,
    values: Array.from(values)
  }))
}

const specTemplate = computed<ItemSpecTemplateItem[]>(() => {
  if (itemData.value?.specTemplate?.length) {
    return itemData.value.specTemplate
  }
  return buildSpecTemplateFromSkus(itemData.value?.skuList)
})

const currentSku = computed<ItemSkuModel | null>(() => {
  const skuList = itemData.value?.skuList || []
  if (!skuList.length) {
    return null
  }
  const specNames = specTemplate.value.map(item => item.name)
  if (!specNames.length) {
    return skuList[0]
  }
  return skuList.find((sku) =>
    specNames.every((name) => sku.specs?.[name] === selectedSpecs[name])
  ) || null
})

const currentPrice = computed(() => currentSku.value?.price ?? itemData.value?.price ?? 0)

const currentStock = computed(() => {
  if (currentSku.value) {
    return currentSku.value.stock || 0
  }
  if (typeof itemData.value?.stock === 'number') {
    return itemData.value.stock
  }
  return (itemData.value?.skuList || []).reduce((sum, sku) => sum + (sku.stock || 0), 0)
})

const fallbackGalleryImages = computed(() => {
  const images = new Set<string>()
  ;(itemData.value?.bannerImages || []).forEach((image) => {
    const normalizedImage = normalizeImageUrl(image)
    if (normalizedImage) {
      images.add(normalizedImage)
    }
  })
  const itemImage = normalizeImageUrl(itemData.value?.image)
  if (itemImage) {
    images.add(itemImage)
  }
  return Array.from(images)
})

const getSkuMatchScore = (sku: ItemSkuModel) => {
  return Object.entries(selectedSpecs).reduce((score, [key, value]) => {
    return sku.specs?.[key] === value ? score + 1 : score
  }, 0)
}

const pickBestSkuForImage = (candidates: ItemSkuModel[]) => {
  const availableCandidates = candidates.filter(sku => (sku.stock || 0) > 0)
  const source = availableCandidates.length ? availableCandidates : candidates
  return [...source].sort((a, b) => getSkuMatchScore(b) - getSkuMatchScore(a))[0]
}

const skuImageThumbs = computed<SkuImageThumb[]>(() => {
  const skuList = itemData.value?.skuList || []
  if (!skuList.length) {
    return []
  }
  const imageGroupMap = new Map<string, ItemSkuModel[]>()
  skuList.forEach((sku) => {
    const image = normalizeImageUrl(sku.image)
    if (!image) {
      return
    }
    if (!imageGroupMap.has(image)) {
      imageGroupMap.set(image, [])
    }
    imageGroupMap.get(image)?.push(sku)
  })
  return Array.from(imageGroupMap.entries()).map(([image, skuGroup]) => {
    const bestSku = pickBestSkuForImage(skuGroup)
    return {
      key: `${image}-${bestSku.id}`,
      image,
      sku: bestSku,
      skuIds: skuGroup.map(sku => sku.id)
    }
  })
})

const displayImage = computed(() => {
  if (previewImage.value) {
    return previewImage.value
  }
  return normalizeImageUrl(currentSku.value?.image) || fallbackGalleryImages.value[0] || ''
})

const canBuy = computed(() => Boolean(itemData.value) && currentStock.value > 0)
const canEnterShop = computed(() => Boolean(itemData.value?.shopId))

const trustItems = computed(() => {
  const data = itemData.value
  if (!data) {
    return []
  }
  return [
    { label: '店铺', value: data.shopName || '未绑定店铺', icon: House },
    { label: '发货', value: data.shippingDesc || '运费规则待补充', icon: Van },
    { label: '销量', value: `已售 ${data.sold || 0}`, icon: Goods },
    {
      label: '口碑',
      value: data.commentCount ? `${data.positiveRate ?? 0}%好评 / ${data.commentCount}条评价` : '暂无评价',
      icon: ChatLineRound
    }
  ]
})

const reviewSummary = computed(() => {
  const data = itemData.value
  if (!data?.commentCount) {
    return '暂无评价'
  }
  return `${data.positiveRate ?? 0}%好评`
})

const formatSpecs = (specs?: Record<string, string>) => {
  if (!specs) {
    return ''
  }
  return Object.entries(specs).map(([key, value]) => `${key}: ${value}`).join(' / ')
}

const formatCommentSpecs = (specs?: Record<string, string> | string | null) => {
  if (!specs) {
    return ''
  }
  if (typeof specs === 'string') {
    return specs
  }
  return formatSpecs(specs)
}

const initSelectedSpecs = () => {
  Object.keys(selectedSpecs).forEach((key) => delete selectedSpecs[key])
  const firstAvailableSku = (itemData.value?.skuList || []).find(sku => sku.stock > 0) || itemData.value?.skuList?.[0]
  if (!firstAvailableSku?.specs) {
    return
  }
  Object.assign(selectedSpecs, firstAvailableSku.specs)
}

const isSpecSelected = (specName: string, value: string) => selectedSpecs[specName] === value

const isSpecDisabled = (specName: string, value: string) => {
  const skuList = itemData.value?.skuList || []
  if (!skuList.length) {
    return false
  }
  const nextSelection = { ...selectedSpecs, [specName]: value }
  return !skuList.some((sku) => {
    if (sku.stock <= 0) {
      return false
    }
    return Object.entries(nextSelection).every(([key, selected]) => !selected || sku.specs?.[key] === selected)
  })
}

const selectSpec = (specName: string, value: string) => {
  if (isSpecDisabled(specName, value)) {
    return
  }
  selectedSpecs[specName] = value
  quantity.value = 1
  previewImage.value = ''
}

const selectSkuThumb = (thumb: SkuImageThumb) => {
  Object.keys(selectedSpecs).forEach((key) => delete selectedSpecs[key])
  Object.assign(selectedSpecs, thumb.sku.specs || {})
  quantity.value = 1
  previewImage.value = ''
}

const isSkuThumbActive = (thumb: SkuImageThumb) => {
  return currentSku.value ? thumb.skuIds.includes(currentSku.value.id) : displayImage.value === thumb.image
}

const loadItemDetail = async () => {
  const id = getItemId()
  if (!id) {
    itemData.value = null
    return
  }

  loading.value = true
  try {
    const res: any = await itemApi.getItemById(id)
    itemData.value = res?.data || null
    initSelectedSpecs()
    previewImage.value = ''
  } catch (error) {
    console.error('加载商品详情失败', error)
    itemData.value = null
    if (!isHandledRequestError(error)) {
      ElMessage.error('当前内容加载失败，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  const id = getItemId()
  if (!id) {
    comments.value = []
    return
  }

  commentsLoading.value = true
  try {
    const res: any = await listComments(id)
    comments.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (error) {
    console.error('加载评论失败', error)
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const buildCartPayload = () => {
  if (!itemData.value) {
    return null
  }
  return {
    itemId: itemData.value.id,
    skuId: currentSku.value?.id || 0,
    num: quantity.value,
    price: currentPrice.value,
    name: itemData.value.name,
    image: normalizeImageUrl(currentSku.value?.image) || displayImage.value || normalizeImageUrl(itemData.value.image) || '',
    spec: JSON.stringify(currentSku.value?.specs || {})
  }
}

const addToCart = async () => {
  if (!canBuy.value) {
    ElMessage.warning('当前商品不可购买')
    return false
  }
  const token = localStorage.getItem('Authorization')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }

  const payload = buildCartPayload()
  if (!payload) {
    return false
  }

  actionLoading.value = true
  try {
    await addItem2Cart(payload)
    ElMessage.success('已加入购物车')
    return true
  } catch (error) {
    console.error('加入购物车失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('加入购物车失败，请稍后重试')
    }
    return false
  } finally {
    actionLoading.value = false
  }
}

const buyNow = async () => {
  const success = await addToCart()
  if (success) {
    router.push('/cart')
  }
}

const goToShop = () => {
  const shopId = itemData.value?.shopId
  if (!shopId) {
    ElMessage.warning('当前商品未绑定店铺')
    return
  }
  router.push(`/shop/${shopId}`)
}

watch(() => route.params.id, async () => {
  await loadItemDetail()
  await loadComments()
})

onMounted(async () => {
  await loadItemDetail()
  await loadComments()
})
</script>

<template>
  <div class="detail-page">
    <div class="detail-container">
      <div class="breadcrumb-row">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>商品详情</el-breadcrumb-item>
          <el-breadcrumb-item>{{ itemData?.name || '详情加载中' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <section v-loading="loading" class="hero-card">
        <template v-if="itemData">
          <div class="gallery-panel">
            <div class="main-image-wrap">
              <img class="main-image" :src="displayImage || '/placeholder-image.svg'" :alt="itemData.name">
            </div>
            <div v-if="skuImageThumbs.length > 1" class="thumb-list">
              <button
                v-for="thumb in skuImageThumbs"
                :key="thumb.key"
                class="thumb-btn"
                :class="{ active: isSkuThumbActive(thumb) }"
                @mouseenter="previewImage = thumb.image"
                @mouseleave="previewImage = ''"
                @click="selectSkuThumb(thumb)"
              >
                <img :src="thumb.image" :alt="itemData.name">
              </button>
            </div>
            <div v-else-if="fallbackGalleryImages.length > 1" class="thumb-list">
              <button
                v-for="image in fallbackGalleryImages"
                :key="image"
                class="thumb-btn"
                :class="{ active: displayImage === image }"
                @mouseenter="previewImage = image"
                @mouseleave="previewImage = ''"
                @click="previewImage = image"
              >
                <img :src="image" :alt="itemData.name">
              </button>
            </div>
          </div>

          <div class="info-panel">
            <div class="trust-top">
              <div class="shop-line">
                <span class="shop-name">{{ itemData.shopName || '未绑定店铺' }}</span>
                <span v-if="itemData.isSelf === 1" class="self-tag">自营</span>
              </div>
              <span class="shipping-chip">{{ itemData.shippingDesc || '运费规则待补充' }}</span>
            </div>

            <h1 class="item-title">{{ itemData.name }}</h1>
            <p v-if="itemData.subTitle" class="item-subtitle">{{ itemData.subTitle }}</p>

            <div class="price-card">
              <div class="price-main">
                <span class="currency">¥</span>
                <span class="amount">{{ formatPrice(currentPrice) }}</span>
                <span v-if="itemData.originalPrice && itemData.originalPrice > currentPrice" class="original-price">
                  ¥{{ formatPrice(itemData.originalPrice) }}
                </span>
              </div>
              <div class="price-side">
                <span class="stat-pill">{{ reviewSummary }}</span>
                <span class="stat-pill">已售 {{ itemData.sold || 0 }}</span>
              </div>
            </div>

            <div class="trust-grid">
              <div v-for="trust in trustItems" :key="trust.label" class="trust-item">
                <el-icon class="trust-icon"><component :is="trust.icon" /></el-icon>
                <div class="trust-content">
                  <div class="trust-label">{{ trust.label }}</div>
                  <div class="trust-value">{{ trust.value }}</div>
                </div>
              </div>
            </div>

            <div class="base-info">
              <span v-if="itemData.brand" class="base-tag">品牌：{{ itemData.brand }}</span>
              <span v-if="itemData.category" class="base-tag">分类：{{ itemData.category }}</span>
              <span class="base-tag">库存：{{ currentStock }}</span>
            </div>

            <div v-if="specTemplate.length" class="selector-section">
              <div v-for="group in specTemplate" :key="group.name" class="selector-row">
                <span class="selector-label">{{ group.name }}</span>
                <div class="selector-options">
                  <button
                    v-for="value in group.values"
                    :key="value"
                    class="selector-option"
                    :class="{ active: isSpecSelected(group.name, value), disabled: isSpecDisabled(group.name, value) }"
                    @click="selectSpec(group.name, value)"
                  >
                    {{ value }}
                  </button>
                </div>
              </div>
            </div>

            <div class="selector-row">
              <span class="selector-label">数量</span>
              <el-input-number v-model="quantity" :min="1" :max="Math.max(currentStock, 1)" :disabled="!canBuy" />
            </div>

            <div class="action-row">
              <el-button class="shop-entry-btn" size="large" plain :disabled="!canEnterShop" @click="goToShop">
                进店
              </el-button>
              <el-button plain size="large" :loading="actionLoading" :disabled="!canBuy" @click="addToCart">
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button type="primary" size="large" :disabled="!canBuy" @click="buyNow">
                立即购买
              </el-button>
            </div>
          </div>
        </template>

        <el-empty v-else description="商品不存在或已下架" :image-size="180" />
      </section>

      <section v-if="itemData" class="detail-sections">
        <el-tabs>
          <el-tab-pane label="商品评价">
            <div v-loading="commentsLoading" class="review-list">
              <div v-if="comments.length" class="review-cards">
                <article v-for="comment in comments" :key="comment.id" class="review-card">
                  <div class="review-head">
                    <div class="review-user">
                      <img class="avatar" :src="comment.userAvatar || '/placeholder-avatar.svg'" :alt="comment.userNickname">
                      <div>
                        <div class="nickname">{{ comment.userNickname || '匿名用户' }}</div>
                        <div class="time">{{ comment.updateTime || comment.createTime }}</div>
                      </div>
                    </div>
                    <el-rate :model-value="comment.rating" disabled text-color="#f59e0b" />
                  </div>
                  <div v-if="comment.skuSpecs" class="review-spec">{{ formatCommentSpecs(comment.skuSpecs) }}</div>
                  <p class="review-content">{{ comment.content }}</p>
                  <div v-if="comment.images?.length" class="review-images">
                    <img v-for="image in comment.images" :key="image" class="review-image" :src="image" :alt="comment.userNickname">
                  </div>
                  <div v-if="comment.merchantReplyContent" class="reply-box">
                    <span class="reply-title">商家回复</span>
                    <p>{{ comment.merchantReplyContent }}</p>
                  </div>
                </article>
              </div>
              <el-empty v-else description="暂无评价" :image-size="120" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="规格参数">
            <div class="spec-panel">
              <div class="spec-summary">
                <div class="spec-summary-item"><span>店铺</span><strong>{{ itemData.shopName || '未绑定店铺' }}</strong></div>
                <div class="spec-summary-item"><span>品牌</span><strong>{{ itemData.brand || '暂无' }}</strong></div>
                <div class="spec-summary-item"><span>分类</span><strong>{{ itemData.category || '暂无' }}</strong></div>
                <div class="spec-summary-item"><span>运费</span><strong>{{ itemData.shippingDesc || '未配置' }}</strong></div>
              </div>

              <el-table v-if="itemData.skuList?.length" :data="itemData.skuList" border>
                <el-table-column label="规格">
                  <template #default="{ row }">{{ formatSpecs(row.specs) }}</template>
                </el-table-column>
                <el-table-column label="价格" width="160">
                  <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
                </el-table-column>
                <el-table-column label="库存" prop="stock" width="120" />
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="图文详情">
            <div v-if="itemData.detailHtml" class="detail-html" v-html="itemData.detailHtml" />
            <el-empty v-else description="暂无图文详情" :image-size="120" />
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>
</template>

<style scoped lang="scss">
.detail-page {
  min-height: calc(100vh - 120px);
  background:
    radial-gradient(circle at top left, rgba(249, 115, 22, 0.18), transparent 24%),
    linear-gradient(180deg, #fffaf5 0%, #f4f7fb 45%, #f3f4f6 100%);
  padding: 28px 0 40px;
}

.detail-container {
  width: min(1240px, calc(100% - 32px));
  margin: 0 auto;
}

.breadcrumb-row {
  margin-bottom: 18px;
}

.hero-card,
.detail-sections {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
}

.hero-card {
  display: grid;
  grid-template-columns: minmax(320px, 520px) minmax(0, 1fr);
  gap: 28px;
  padding: 28px;
}

.gallery-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.main-image-wrap {
  overflow: hidden;
  border-radius: 24px;
  background: linear-gradient(135deg, #fff7ed 0%, #fef2f2 100%);
  aspect-ratio: 1;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.thumb-btn {
  width: 78px;
  height: 78px;
  padding: 0;
  overflow: hidden;
  border: 2px solid transparent;
  border-radius: 18px;
  background: #fff;
  cursor: pointer;
}

.thumb-btn.active {
  border-color: #f97316;
}

.thumb-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.trust-top,
.shop-line,
.price-card,
.action-row,
.selector-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.shop-name {
  color: #111827;
  font-size: 15px;
  font-weight: 700;
}

.self-tag,
.shipping-chip,
.base-tag,
.stat-pill {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
}

.self-tag {
  background: #fee2e2;
  color: #b91c1c;
}

.shipping-chip {
  background: #eff6ff;
  color: #1d4ed8;
}

.item-title {
  margin: 0;
  color: #111827;
  font-size: 30px;
  line-height: 1.4;
}

.item-subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 15px;
  line-height: 1.8;
}

.price-card {
  padding: 20px 24px;
  border-radius: 22px;
  background: linear-gradient(135deg, #fff7ed 0%, #fff1f2 100%);
}

.price-main {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.currency,
.amount {
  color: #dc2626;
  font-weight: 700;
}

.currency {
  font-size: 22px;
}

.amount {
  font-size: 40px;
  line-height: 1;
}

.original-price {
  color: #9ca3af;
  font-size: 15px;
  text-decoration: line-through;
}

.price-side {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.stat-pill {
  background: rgba(255, 255, 255, 0.72);
  color: #7c2d12;
}

.trust-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.trust-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 20px;
  background: #fff;
}

.trust-icon {
  color: #f97316;
  font-size: 20px;
}

.trust-label {
  color: #9ca3af;
  font-size: 12px;
}

.trust-value {
  margin-top: 6px;
  color: #111827;
  font-weight: 600;
  line-height: 1.6;
}

.base-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.base-tag {
  background: #f3f4f6;
  color: #374151;
}

.selector-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.selector-label {
  min-width: 56px;
  color: #6b7280;
  font-weight: 600;
}

.selector-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.selector-option {
  min-width: 78px;
  height: 38px;
  padding: 0 14px;
  border: 1px solid #d1d5db;
  border-radius: 14px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.selector-option.active {
  border-color: #f97316;
  background: #fff7ed;
  color: #c2410c;
}

.selector-option.disabled {
  color: #9ca3af;
  background: #f3f4f6;
  cursor: not-allowed;
}

.action-row {
  justify-content: flex-start;
}

.detail-sections {
  margin-top: 24px;
  padding: 24px;
}

.review-list,
.spec-panel {
  padding-top: 8px;
}

.review-cards {
  display: grid;
  gap: 16px;
}

.review-card {
  padding: 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
  background: #fff;
}

.review-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.nickname {
  color: #111827;
  font-weight: 600;
}

.time,
.review-spec {
  color: #9ca3af;
  font-size: 13px;
}

.review-spec {
  margin-top: 12px;
}

.review-content {
  margin: 12px 0 0;
  color: #374151;
  line-height: 1.8;
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.review-image {
  width: 90px;
  height: 90px;
  border-radius: 14px;
  object-fit: cover;
}

.reply-box {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff7ed;
}

.reply-title {
  color: #c2410c;
  font-weight: 600;
}

.reply-box p {
  margin: 8px 0 0;
  color: #7c2d12;
  line-height: 1.7;
}

.spec-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.spec-summary-item {
  display: flex;
  justify-content: space-between;
  padding: 14px 16px;
  border-radius: 18px;
  background: #f9fafb;
  color: #374151;
}

.spec-summary-item span {
  color: #6b7280;
}

.detail-html {
  color: #374151;
  line-height: 1.9;
}

:deep(.detail-html img) {
  max-width: 100%;
  border-radius: 16px;
}

@media (max-width: 992px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .trust-grid,
  .spec-summary {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .price-card,
  .trust-top,
  .selector-row,
  .review-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
