<template>
  <div class="product-detail-page">
    <div class="container">
      
      <!-- 面包屑导航 -->
      <div class="breadcrumb-area">
        <el-breadcrumb separator-icon="ArrowRight" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>商品详情</el-breadcrumb-item>
          <el-breadcrumb-item class="current-product-name" :title="itemData?.name">{{ itemData?.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <div class="main-card" v-loading="loading">
        <el-row :gutter="40" v-if="itemData">
          
          <!-- 左侧：图片展示 -->
          <el-col :xs="24" :sm="12" :md="11">
            <div class="gallery-container">
              <!-- 大图区域 -->
              <div class="main-img-wrap">
                <el-image 
                  :src="displayImage" 
                  fit="contain" 
                  class="hero-image"
                  :preview-src-list="previewImageList"
                />
              </div>
              
              <!-- 缩略图列表（根据选中规格动态变化） -->
              <div 
                class="thumb-row" 
                v-if="filteredThumbImages && filteredThumbImages.length > 0"
              >
                <div 
                  v-for="(img, index) in filteredThumbImages" 
                  :key="index"
                  class="thumb-box"
                  :class="{ active: displayImage === img }"
                  @mouseenter="tempImage = img"
                  @mouseleave="tempImage = ''"
                  @click="handleThumbClick(img)"
                >
                  <img :src="img" />
                </div>
              </div>
            </div>
          </el-col>

          <!-- 右侧：信息与交互 -->
          <el-col :xs="24" :sm="12" :md="13">
            <div class="info-container">
              <h1 class="prod-title">{{ itemData.name }}</h1>
              <p class="prod-desc">{{ itemData.subTitle }}</p>

              <!-- 价格区 -->
              <div class="price-box">
                <div class="price-main">
                  <span class="currency">¥</span>
                  <span class="amount">{{ formatPrice(currentSku ? currentSku.price  : itemData.price ) }}</span>
                  <span v-if="itemData.originalPrice" class="original-price">
                    ¥{{ formatPrice(itemData.originalPrice ) }}
                  </span>
                </div>
                <div class="tags-row">
                  <el-tag v-if="itemData.tags" type="danger" size="small" round>{{ itemData.tags.split(',')[0] }}</el-tag>
                  <span class="stock-text">
                    库存 {{ currentSku ? currentSku.stock : itemData.skuList?.reduce((a, b) => a + b.stock, 0) }} 件
                  </span>
                  <span class="sold-text">已售 {{ itemData.sold }}</span>
                </div>
              </div>

              <el-divider border-style="dashed" />

              <!-- 规格选择器 -->
              <div class="sku-selector" v-if="itemData.specTemplate && itemData.specTemplate.length">
                <div 
                  class="sku-line" 
                  v-for="(specRow, rowIndex) in itemData.specTemplate" 
                  :key="specRow.name"
                >
                  <span class="label">{{ specRow.name }}</span>
                  <div class="options">
                    <span 
                      v-for="(val, valIndex) in specRow.values" 
                      :key="val"
                      class="option-pill"
                      :class="{ 
                        active: isSpecSelected(specRow.name, val),
                        disabled: isSpecDisabled(specRow.name, val)
                      }"
                      @click="selectSpec(specRow.name, val)"
                    >
                      {{ val }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="sku-selector">
                <div class="sku-line">
                  <span class="label">购买数量</span>
                  <el-input-number 
                    v-model="quantity" 
                    :min="1" 
                    :max="buyMax" 
                    :disabled="!canBuy"
                  />
                </div>
              </div>

              <!-- 按钮 -->
              <div class="btn-group">
                <el-button 
                  class="btn-cart" 
                  size="large" 
                  plain 
                  round 
                  @click="addToCart" 
                  :disabled="!canBuy"
                  :loading="btnLoading"
                >
                  加入购物车
                </el-button>
                <el-button class="btn-buy" size="large" type="primary" round @click="buyNow" :disabled="!canBuy">立即购买</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-empty v-else description="未找到商品信息"></el-empty>
      </div>

      <!-- 底部 Tabs -->
      <div class="details-card detail-tabs" v-if="itemData">
        <el-tabs v-model="activeTab" class="centered-tabs" type="card">
          
          <!-- Tab 1: 商品详情 -->
          <el-tab-pane label="商品详情" name="detail">
             <div class="detail-content">
               <div v-if="itemData.detailHtml" v-html="itemData.detailHtml" class="detail-html-content"></div>
               <div v-else class="no-detail">暂无图文详情</div>
             </div>
          </el-tab-pane>
          
          <!-- Tab 2: 规格参数 -->
          <el-tab-pane label="规格参数" name="specs">
             <div class="specs-content">
                <el-table :data="itemData.skuList" border style="width: 100%">
                  <el-table-column label="型号组合">
                    <template #default="scope">{{ formatSpecsMap(scope.row.specs) }}</template>
                  </el-table-column>
                  <el-table-column prop="price" label="价格">
                    <template #default="scope">¥{{ formatPrice(scope.row.price) }}</template>
                  </el-table-column>
                  <el-table-column prop="stock" label="库存" />
                </el-table>
             </div>
          </el-tab-pane>

          <!-- ★★★ Tab 3: 用户评价 (已按要求修改) ★★★ -->
          <el-tab-pane label="用户评价" name="comments">
            <div class="comments-container" v-loading="commentLoading">
              
              <!-- 评价统计头 -->
              <div class="comments-header" v-if="commentList.length > 0">
                <div class="header-left">
                  <span class="big-score">{{ averageRating }}</span>
                  <div class="score-meta">
                    <span class="meta-label">综合评分</span>
                    <el-rate v-model="averageRating" disabled size="small" text-color="#ff9900" />
                  </div>
                </div>
                <div class="header-right">
                  <span class="total-count">共 {{ commentList.length }} 条评价</span>
                </div>
              </div>

              <!-- 评价列表 -->
              <div class="comment-list" v-if="commentList.length > 0">
                <div v-for="comment in commentList" :key="comment.id" class="comment-item">
                  
                  <!-- 用户信息行 -->
                  <div class="user-row">
                    <el-avatar :size="40" :src="comment.userAvatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" class="user-avatar" />
                    <div class="user-info-col">
                      <div class="nickname">{{ comment.userNickname || '匿名用户' }}</div>
                      <div class="meta-row">
                        <el-rate 
                          v-model="comment.rating" 
                          disabled 
                          size="small" 
                          text-color="#ff9900"
                          class="mini-rate"
                        />
                        <span class="separator">|</span>
                        <span class="time">{{ comment.createTime }}</span>
                      </div>
                    </div>
                  </div>

                  <!-- 评价内容 -->
                  <div class="content-text">
                    {{ comment.content }}
                  </div>
                  
                  <!-- 晒图 -->
                  <div class="review-images" v-if="comment.images && comment.images.length">
                    <el-image 
                      v-for="(img, idx) in comment.images" 
                      :key="idx"
                      :src="img"
                      :preview-src-list="comment.images"
                      class="review-img"
                      fit="cover"
                      lazy
                    />
                  </div>

                  <!-- ★★★ 商品信息 Footer (仿 UserCenter 风格) ★★★ -->
                  <div class="card-footer-mini">
                    <div class="footer-product">
                      <el-icon><Goods /></el-icon>
                      <span class="product-link">
                        {{ itemData.name }}
                      </span>
                      
                      <!-- SKU 标签 -->
                      <span class="sku-text" v-if="comment.skuSpecs">
                        <el-tag 
                          class="sku-tag" 
                          type="info" 
                          effect="plain" 
                          size="small"
                        >
                          {{ formatSku(comment.skuSpecs) }}
                        </el-tag>
                      </span>

                      <!-- 商品小图
                      <el-image 
                        v-if="itemData.bannerImages && itemData.bannerImages.length"
                        :src="itemData.bannerImages[0]" 
                        class="footer-item-img" 
                        fit="cover"
                      ></el-image> -->
                    </div>
                  </div>

                  <!-- 商家回复 -->
                  <div class="merchant-reply" v-if="comment.merchantReplyContent">
                    <div class="reply-header">
                       <span class="shop-badge">商家</span>
                       <span class="reply-title">回复：</span>
                    </div>
                    <div class="reply-content">{{ comment.merchantReplyContent }}</div>
                  </div>

                </div>
              </div>

              <el-empty v-else description="暂无评价，快来抢沙发吧~" :image-size="120" />
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
// 引入 Goods 图标
import { Goods } from '@element-plus/icons-vue' 
import { itemApi } from '@/api/item'
import { addItem2Cart } from '@/api/cart'
import { listComments, type CommentVO } from '@/api/comment' 

const router = useRouter();
const route = useRoute()

// --- 接口定义 ---
interface SpecTemplateItem {
  name: string
  values: string[]
}
interface SkuVO {
  id: number
  specs: Record<string, string>
  price: number
  stock: number
  image: string
}
interface ItemDetailVO {
  id: number
  name: string
  subTitle: string
  price: number
  originalPrice?: number
  tags?: string
  sold?: number
  bannerImages: string[]
  detailHtml?: string
  specTemplate: SpecTemplateItem[]
  skuList: SkuVO[]
  itemName: string
  itemImage: string
}

// --- 状态 ---
const loading = ref(false)
const btnLoading = ref(false)
const itemData = ref<ItemDetailVO | null>()
const selectedSpecs = reactive<Record<string, string>>({})
const quantity = ref(1)
const activeTab = ref('detail')
const tempImage = ref('')

// ★★★ 评价状态 ★★★
const commentList = ref<CommentVO[]>([])
const commentLoading = ref(false)

// --- 核心逻辑 ---

const fetchProductDetail = async () => {
  loading.value = true
  try {
    const productId = Number(route.params.id) || 1
    const res = await itemApi.getItemById(productId)
    if (res && res.data) {
      itemData.value = res.data
      initDefaultSelection()
      fetchComments() 
    }
  } catch (err) {
    console.error(err)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// ★★★ 获取评论逻辑 ★★★
const fetchComments = async () => {
  commentLoading.value = true
  try {
    const res: any = await listComments(itemData.value!.id)
    const allComments = Array.isArray(res) ? res : (res.data || [])
    commentList.value = allComments
  } catch (error) {
    console.error('获取评论失败', error)
  } finally {
    commentLoading.value = false
  }
}

// 计算平均分
const averageRating = computed(() => {
  if (commentList.value.length === 0) return 5
  const total = commentList.value.reduce((sum, c) => sum + (c.rating || 5), 0)
  return Number((total / commentList.value.length).toFixed(1))
})

// ★★★ 格式化 SKU 字符串 (去除 JSON 符号) ★★★
const formatSku = (skuStr: string) => {
  if (!skuStr) return '';
  try {
    const cleanStr = skuStr.replace(/[{"}]/g, '').replace(/"/g, '').replace(/:/g, ': ');
    return cleanStr;
  } catch (e) {
    return skuStr;
  }
}

const initDefaultSelection = () => {
  if (!itemData.value?.skuList?.length) return
  const firstValidSku = itemData.value.skuList.find(s => s.stock > 0) || itemData.value.skuList[0]
  if (firstValidSku && firstValidSku.specs) {
    Object.assign(selectedSpecs, firstValidSku.specs)
  }
}

const hasColorSpec = computed(() => {
  if (!itemData.value?.specTemplate) return false
  return itemData.value.specTemplate.some(spec => spec.name.includes('颜色'))
})

const currentSku = computed(() => {
  if (!itemData.value) return null
  return itemData.value.skuList.find(sku => {
    const skuSpecs = sku.specs
    if (Object.keys(skuSpecs).length !== Object.keys(selectedSpecs).length) return false
    for (const key in skuSpecs) {
      if (skuSpecs[key] !== selectedSpecs[key]) return false
    }
    return true
  })
})

const canBuy = computed(() => {
  return currentSku.value && currentSku.value.stock > 0
})

const buyMax = computed(() => {
  if (!currentSku.value) return 1
  const stock = currentSku.value.stock
  return stock > 0 ? stock : 1
})

const displayImage = computed(() => {
  if (tempImage.value) return tempImage.value
  if (currentSku.value?.image) return currentSku.value.image
  
  // ★★★ 如果当前SKU没有图片，但选中了颜色规格，优先显示该颜色的第一张图片 ★★★
  const colorSpecName = itemData.value?.specTemplate?.find(s => s.name.includes('颜色'))?.name
  if (colorSpecName && selectedSpecs[colorSpecName] && itemData.value?.skuList?.length) {
    const matchedSkus = itemData.value.skuList.filter(sku => {
      return sku.specs[colorSpecName] === selectedSpecs[colorSpecName]
    })
    const matchedImages = matchedSkus
      .map(sku => sku.image)
      .filter(img => img) // 过滤空值
    if (matchedImages.length > 0) {
      return matchedImages[0] // 返回该颜色的第一张图片
    }
  }
  
  if (itemData.value?.bannerImages?.length) return itemData.value.bannerImages[0]
  return ''
})

const previewImageList = computed(() => {
  const images: string[] = []
  if (currentSku.value?.image) images.push(currentSku.value.image)
  if (itemData.value?.bannerImages) images.push(...itemData.value.bannerImages)
  return [...new Set(images)]
})

// ★★★ 根据选中规格动态过滤缩略图列表 ★★★
// 规则：只有存在颜色规格时，才根据颜色过滤；否则显示商品的所有图片（bannerImages）
const filteredThumbImages = computed(() => {
  if (!itemData.value) {
    return []
  }

  // 检查是否有颜色规格
  const colorSpecName = itemData.value.specTemplate?.find(s => s.name.includes('颜色'))?.name
  
  // 如果有颜色规格，且已选中颜色，则根据颜色过滤显示匹配的SKU图片
  if (colorSpecName && selectedSpecs[colorSpecName] && itemData.value.skuList?.length) {
    const matchedSkus = itemData.value.skuList.filter(sku => {
      return sku.specs[colorSpecName] === selectedSpecs[colorSpecName]
    })
    
    const matchedImages = matchedSkus
      .map(sku => sku.image)
      .filter(img => img) // 过滤空值
    
    // 如果有匹配的SKU图片，返回这些图片；否则回退到bannerImages
    if (matchedImages.length > 0) {
      return [...new Set(matchedImages)]
    }
  }

  // 如果没有颜色规格，或者颜色匹配失败，显示商品的所有图片（bannerImages）
  return itemData.value.bannerImages || []
})

// --- 交互方法 ---

// ★★★ 优化：点击缩略图时，自动选择对应的所有规格 ★★★
const handleThumbClick = (imgUrl: string) => {
  if (!itemData.value?.skuList) return
  const targetSku = itemData.value.skuList.find(sku => sku.image === imgUrl)
  
  if (targetSku && targetSku.specs) {
    // 自动选择该SKU对应的所有规格
    Object.keys(targetSku.specs).forEach(specName => {
      if (targetSku.specs[specName]) {
        selectedSpecs[specName] = targetSku.specs[specName]
    }
    })
    quantity.value = 1
    // 清除临时图片，让主图显示当前SKU的图片
    tempImage.value = ''
  }
}

const isSpecSelected = (specName: string, val: string) => selectedSpecs[specName] === val
const isSpecDisabled = (specName: string, val: string) => false

const selectSpec = (specName: string, val: string) => {
  selectedSpecs[specName] = val
  quantity.value = 1
}

const formatPrice = (price?: number) => {
  if (price === undefined || price === null) return '0.00'
  return (price / 100).toFixed(2)
}

const formatSpecsMap = (specs: Record<string, string>) => Object.values(specs).join(' / ')

const addToCart = async () => {
  const Auth = localStorage.getItem('Authorization')
  if (!Auth) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  if (!currentSku.value) {
    ElMessage.warning('请选择完整的规格')
    return
  }
  if (!itemData.value) return

  const params = {
    itemId: itemData.value.id,
    skuId: currentSku.value.id,
    num: quantity.value,
    spec: JSON.stringify(currentSku.value.specs),
    price: currentSku.value.price,
    image: currentSku.value.image,
    name: itemData.value.name
  }
  
  try {
    btnLoading.value = true
    await addItem2Cart(params)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error(error)
    ElMessage.error('加入购物车失败')
  } finally {
    btnLoading.value = false
  }
}

const buyNow = async () => {
  if (!currentSku.value) {
    ElMessage.warning('请选择完整的规格')
    return
  }
  const authToken = localStorage.getItem('Authorization')
  if (!authToken) {
    ElMessage.error('请先登录')
    router.push('/login')
    return
  }
  if (!itemData.value || !currentSku.value) {
    ElMessage.warning('商品信息不完整')
    return
  }
  if (currentSku.value.stock < quantity.value) {
    ElMessage.error(`库存不足，当前库存：${currentSku.value.stock}`)
    return
  }
  
  try {
    btnLoading.value = true
    const params = {
      itemId: itemData.value.id,
      skuId: currentSku.value.id,
      num: quantity.value,
      spec: JSON.stringify(currentSku.value.specs),
      price: currentSku.value.price,
      image: currentSku.value.image,
      name: itemData.value.name
    }
    
    await addItem2Cart(params)
    ElMessage.success('商品已添加到购物车')
    router.push('/cart')
  } catch (error) {
    console.error('立即购买失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    btnLoading.value = false
  }
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped lang="scss">
$primary: #333;
$accent: #409eff;
$bg-body: #f7f8fa;
$bg-card: #ffffff;
$border: #ebeef5;

.product-detail-page { background: $bg-body; min-height: 100vh; padding-bottom: 60px; color: #333; }
.container { max-width: 1100px; margin: 0 auto; padding: 20px; }
.breadcrumb-area { margin-bottom: 20px; }

.main-card {
  background: $bg-card; border-radius: 12px; padding: 30px; 
  box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 24px;
  min-height: 400px;
}

.gallery-container {
  .main-img-wrap {
    width: 100%; height: 400px; background: #fbfbfb; border-radius: 8px; margin-bottom: 12px;
    display: flex; align-items: center; justify-content: center; overflow: hidden;
    .hero-image { width: 100%; height: 100%; }
  }
  .thumb-row {
    display: flex; gap: 10px; justify-content: flex-start; overflow-x: auto; padding-bottom: 5px;
    .thumb-box {
      width: 60px; height: 60px; flex-shrink: 0;
      border: 1px solid $border; border-radius: 6px; cursor: pointer; padding: 2px;
      transition: all 0.2s;
      &.active { border-color: $accent; border-width: 2px; }
      img { width: 100%; height: 100%; object-fit: contain; }
    }
  }
}

.info-container {
  padding-left: 20px;
  .prod-title { font-size: 24px; font-weight: 700; margin-bottom: 8px; line-height: 1.4; }
  .prod-desc { color: #999; font-size: 14px; margin-bottom: 15px; }

  .price-box {
    background: #f9f9f9; padding: 15px; border-radius: 8px; margin-bottom: 20px;
    display: flex; align-items: flex-end;
    .price-main { 
      color: #e4393c; font-weight: bold; font-size: 28px; margin-right: 15px;
      .currency { font-size: 16px; margin-right: 2px; }
      .original-price { font-size: 14px; color: #999; text-decoration: line-through; margin-left: 8px; font-weight: normal; }
    }
    .tags-row { 
      display: flex; align-items: center; gap: 10px; margin-bottom: 6px;
      .stock-text, .sold-text { font-size: 12px; color: #666; }
    }
  }

  .sku-selector {
    margin-bottom: 20px;
    .sku-line {
      margin-bottom: 15px;
      .label { display: block; margin-bottom: 8px; font-weight: bold; font-size: 14px; color: #606266; }
      .options {
        display: flex; flex-wrap: wrap; gap: 10px;
        .option-pill {
          padding: 6px 18px; 
          border: 1px solid #dcdfe6; 
          border-radius: 4px; 
          cursor: pointer; 
          font-size: 13px; 
          color: #606266;
          background: #fff;
          transition: all 0.2s ease;
          
          &:hover:not(.disabled) { border-color: $accent; color: $accent; }
          &.active { border-color: $accent; color: $accent; background: #ecf5ff; }
          &.disabled { cursor: not-allowed; background-color: #f5f7fa; color: #c0c4cc; border-color: #ebeef5; }
        }
      }
    }
  }

  .btn-group { 
    margin-top: 30px;
    display: flex; gap: 15px; 
    .el-button { flex: 1; height: 45px; font-size: 16px; font-weight: bold;}
  }
}

.details-card { background: #fff; padding: 20px; border-radius: 12px; min-height: 300px; }
.detail-html-content { :deep(img) { max-width: 100%; height: auto; display: block; } }

/* ★★★ 优化后的评论模块样式 ★★★ */
.comments-container {
  padding: 10px 20px;

  /* 顶部统计区 */
  .comments-header {
    display: flex; justify-content: space-between; align-items: center;
    padding-bottom: 20px; border-bottom: 1px solid #f0f0f0; margin-bottom: 20px;
    
    .header-left {
      display: flex; align-items: center; gap: 16px;
      .big-score { font-size: 48px; color: #333; font-weight: 600; line-height: 1; }
      .score-meta {
        display: flex; flex-direction: column; gap: 4px;
        .meta-label { font-size: 12px; color: #999; }
      }
    }
    .header-right {
      .total-count { font-size: 14px; color: #666; background: #f5f7fa; padding: 6px 16px; border-radius: 20px; }
    }
  }

  /* 评价列表项 */
  .comment-item {
    padding: 24px 0; 
    border-bottom: 1px solid #f7f8fa;
    &:last-child { border-bottom: none; }

    /* 用户信息行 */
    .user-row {
      display: flex; align-items: center; margin-bottom: 12px;
      .user-avatar { border: 1px solid #f0f0f0; margin-right: 12px; }
      .user-info-col {
        display: flex; flex-direction: column;
        .nickname { font-size: 14px; font-weight: 600; color: #333; line-height: 1.4; }
        .meta-row {
          display: flex; align-items: center; font-size: 12px; color: #999;
          .mini-rate { transform: scale(0.9); transform-origin: left center; margin-right: 4px; }
          .separator { margin: 0 8px; color: #eee; }
        }
      }
    }

    /* 评价文本 */
    .content-text { font-size: 15px; color: #333; line-height: 1.6; margin-bottom: 10px; white-space: pre-wrap; }

    /* 图片列表 */
    .review-images {
      display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 15px;
      .review-img { 
        width: 100px; height: 100px; border-radius: 8px; 
        border: 1px solid #f0f0f0; cursor: zoom-in; 
        transition: transform 0.2s;
        &:hover { transform: scale(1.02); border-color: #ddd; }
      }
    }

    /* ★★★ 商品信息 Footer (仿 UserCenter) ★★★ */
    .card-footer-mini {
      margin-top: 10px;
      padding-top: 10px;
      border-top: 1px dashed #f5f7fa; /* 虚线分割 */
      
      .footer-product {
        display: flex; 
        align-items: center; 
        font-size: 12px; 
        color: #909399;
        
        .product-link {
          margin-left: 5px;
          margin-right: 5px;
          color: #606266;
          font-weight: 500;
        }

        /* SKU 标签样式 */
        .sku-tag {
          background-color: #f6f6f6;
          border: none;
          color: #666;
          border-radius: 4px;
          padding: 0 6px;
          height: 20px;
          line-height: 20px;
          font-size: 11px;
          margin-left: 8px;
          transition: all 0.2s;
        }
        
        /* 商品小图 */
        .footer-item-img {
          width: 40px;
          height: 40px;
          border-radius: 4px;
          margin-left: 10px;
          border: 1px solid #eee;
          flex-shrink: 0;
        }
      }
    }

    /* 商家回复 (气泡风格) */
    .merchant-reply {
      background: #f7f8fa; padding: 12px 16px; border-radius: 8px; position: relative;
      margin-top: 12px;
      .reply-header {
        display: flex; align-items: center; margin-bottom: 6px;
        .shop-badge { 
          background: #333; color: #fff; font-size: 10px; padding: 1px 4px; 
          border-radius: 2px; margin-right: 6px; 
        }
        .reply-title { font-size: 13px; color: #666; font-weight: bold; }
      }
      .reply-content { font-size: 13px; color: #555; line-height: 1.5; }
    }
  }
}

@media (max-width: 768px) { .info-container { padding-left: 0; margin-top: 20px; } }
</style>