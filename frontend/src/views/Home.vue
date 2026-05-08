<template>
  <div class="home-page">
    <header class="home-header">
      <div class="home-header__shell">
        <div class="home-topbar">
          <div class="home-topbar__group">
            <button type="button" class="home-topbar__link" @click="goHome">首页</button>
            <button type="button" class="home-topbar__link" @click="goFavorites">收藏夹</button>
            <button type="button" class="home-topbar__link" @click="goOrders">我的订单</button>
            <button type="button" class="home-topbar__link" @click="goBrandHall">品牌馆</button>
          </div>

          <div class="home-topbar__group home-topbar__group--right">
            <span class="home-topbar__greeting">{{ topGreeting }}</span>
            <button v-if="!isLoggedIn" type="button" class="home-topbar__link" @click="goLogin">登录</button>
            <button v-if="!isLoggedIn" type="button" class="home-topbar__link" @click="goRegister">注册</button>
            <button type="button" class="home-topbar__link" @click="goDashboard">个人中心</button>
          </div>
        </div>

        <div class="home-searchbar">
          <button type="button" class="home-brand" @click="goHome">
            <span class="home-brand__badge">YU</span>
            <span class="home-brand__copy">
              <strong>YU-Mall</strong>
              <small>先搜商品，再逛会场，再下单</small>
            </span>
          </button>

          <div class="home-search-panel">
            <div class="home-search-panel__body">
              <div class="home-search-tabs">
                <button
                  v-for="tab in searchTabs"
                  :key="tab"
                  type="button"
                  class="home-search-tab"
                  :class="{ active: tab === activeSearchTab }"
                  @click="activeSearchTab = tab"
                >
                  {{ tab }}
                </button>
              </div>

              <div class="home-search-box">
                <span class="home-search-box__icon">
                  <el-icon><Search /></el-icon>
                </span>
                <input
                  v-model="searchKeyword"
                  class="home-search-box__input"
                  type="text"
                  :placeholder="searchPlaceholder"
                  @keyup.enter="submitHomeSearch"
                >
                <button type="button" class="home-search-box__submit" @click="submitHomeSearch">搜索</button>
              </div>
            </div>

            <div class="home-hotwords">
              <button
                v-for="keyword in hotKeywords"
                :key="keyword"
                type="button"
                class="home-hotwords__item"
                @click="quickSearch(undefined, keyword)"
              >
                {{ keyword }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <div class="home-shell">

      <!-- 独立的导航栏行，与下方 Grid 轨道对齐，解决原本破坏卡片对齐的问题 -->
      <div class="channel-nav-row">
        <div class="stage-nav">
          <button
            v-for="channel in homeChannels"
            :key="channel.label"
            type="button"
            class="stage-nav__item"
            @click="quickSearch(channel.category, channel.keyword)"
          >
            {{ channel.label }}
          </button>
        </div>
      </div>

      <section class="hero-grid">
        <!-- 左侧：分类栏 -->
        <aside class="hero-panel category-panel">
          <div class="panel-heading">
            <h2>主题市场</h2>
            <p>10 个一级类目，按场景快速直达</p>
          </div>

          <div class="category-list">
            <button
              v-for="item in categories"
              :key="item.name"
              type="button"
              class="category-item"
              @click="handleCategoryClick(item.searchCategory)"
            >
              <span class="category-main">
                <span class="category-icon" :class="`tone-${item.tone}`">
                  <component :is="item.icon" />
                </span>
                <span class="category-label">{{ item.name }}</span>
              </span>
              <el-icon class="category-arrow"><ArrowRight /></el-icon>
            </button>
          </div>
        </aside>

        <!-- 中间：轮播图与活动（总高基准设为 512px） -->
        <section class="stage-panel">
          <div class="hero-banner-card">
            <el-carousel
              ref="bannerCarouselRef"
              class="hero-banner-carousel"
              height="364px"
              arrow="never"
              :interval="4600"
              @change="handleBannerChange"
            >
              <el-carousel-item v-for="slide in heroSlides" :key="slide.id">
                <div
                  class="hero-banner-slide"
                  :style="{ background: slide.background }"
                  role="button"
                  tabindex="0"
                  @click="quickSearch(slide.category, slide.keyword)"
                  @keyup.enter="quickSearch(slide.category, slide.keyword)"
                  @keyup.space.prevent="quickSearch(slide.category, slide.keyword)"
                >
                  <div class="hero-banner-copy">
                    <span class="hero-banner-tag">{{ slide.tag }}</span>
                    <h3>{{ slide.titleLine1 }}</h3>
                    <h3>{{ slide.titleLine2 }}</h3>
                    <p>{{ slide.subtitle }}</p>
                  </div>
                  <div class="hero-banner-visual">
                    <img class="hero-banner-image" :src="slide.image" :alt="slide.alt">
                  </div>
                  <div class="hero-banner-dots">
                    <button
                      v-for="(_, index) in heroSlides"
                      :key="index"
                      type="button"
                      class="hero-banner-dot"
                      :class="{ active: index === activeBannerIndex }"
                      @click.stop="showBanner(index)"
                    />
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>

          <div class="promo-row">
            <button
              v-for="banner in promoBanners"
              :key="banner.title"
              type="button"
              class="promo-banner"
              :class="`promo-banner--${banner.tone}`"
              @click="quickSearch(banner.category, banner.keyword)"
            >
              <div class="promo-banner__copy">
                <h3>{{ banner.title }}</h3>
                <p>{{ banner.subtitle }}</p>
              </div>
              <div class="promo-banner__visual">
                <img :src="banner.image" :alt="banner.alt">
              </div>
            </button>
          </div>
        </section>

        <!-- 右侧：个人中心卡片 -->
        <aside class="user-rail">
          <section class="hero-panel profile-card">
            <div class="profile-card__top">
              <div class="profile-avatar">
                <img
                  v-if="profileAvatarUrl"
                  class="profile-avatar__image"
                  :src="profileAvatarUrl"
                  :alt="`${displayUserName}头像`"
                >
                <span v-else>{{ profileBadgeText }}</span>
              </div>

              <div class="profile-copy">
                <h3>{{ heroGreeting }}</h3>
                <p>{{ heroGreetingSub }}</p>
              </div>
            </div>

            <div class="profile-actions">
              <template v-if="!isLoggedIn">
                <button type="button" class="primary-btn" @click="goLogin">登录</button>
                <button type="button" class="secondary-btn" @click="goRegister">注册</button>
              </template>
              <template v-else>
                <button type="button" class="primary-btn" @click="goUserDashboard">个人中心</button>
                <button type="button" class="secondary-btn" @click="goUserOrders">我的订单</button>
              </template>
            </div>

            <div class="profile-benefits">
              <span class="benefit-chip benefit-chip--orange">新人立减</span>
              <span class="benefit-chip benefit-chip--blue">补贴价</span>
              <span class="benefit-chip benefit-chip--purple">品牌日</span>
            </div>

            <div class="profile-shortcuts">
              <div class="shortcut-card__heading">
                <h3>常用入口</h3>
                <span>{{ isLoggedIn ? '账户状态实时同步' : '登录后同步订单与收藏' }}</span>
              </div>

              <div class="shortcut-grid">
                <button
                  v-for="shortcut in heroShortcuts"
                  :key="shortcut.label"
                  type="button"
                  class="shortcut-item"
                  @click="handleShortcutClick(shortcut.key)"
                >
                  <span class="shortcut-icon" :class="`tone-${shortcut.tone}`">
                    <el-icon><component :is="shortcut.icon" /></el-icon>
                  </span>
                  <span>{{ shortcut.label }}</span>
                </button>
              </div>
            </div>
          </section>

          <section class="hero-panel notice-card">
            <div class="notice-card__heading">
              <h3>公告论坛</h3>
              <span>活动与规则同步更新</span>
            </div>

            <ul class="notice-list">
              <li v-for="notice in homeAnnouncements" :key="notice.title">
                <button
                  type="button"
                  class="notice-item"
                  @click="quickSearch(notice.category, notice.keyword)"
                >
                  <span class="notice-tag" :class="`notice-tag--${notice.tone}`">{{ notice.tag }}</span>
                  <span class="notice-text">{{ notice.title }}</span>
                </button>
              </li>
            </ul>
          </section>
        </aside>
      </section>

      <div class="product-list-section" v-loading="loading" element-loading-text="首页商品加载中...">
        <div v-if="productList.length > 0">
          <el-row :gutter="16">
            <el-col
              v-for="item in productList"
              :key="item.id"
              :xs="12"
              :sm="8"
              :md="6"
              :lg="4"
              :xl="4"
            >
              <el-card class="product-card" shadow="hover" :body-style="{ padding: '0px' }">
                <div class="image-container">
                  <el-image
                    :src="resolveProductImage(item.image)"
                    fit="cover"
                    loading="lazy"
                    class="prod-img"
                    @click="enterItemDetail(Number(item.id))"
                  >
                    <template #error>
                      <div class="prod-img prod-img--fallback">
                        <img class="prod-img__fallback-image" src="/placeholder-image.svg" alt="商品占位图">
                      </div>
                    </template>
                  </el-image>
                  <div class="action-overlay">
                    <div class="action-btn cart-btn" @click.stop="addToCart(item)">
                      <el-icon><ShoppingCart /></el-icon>
                      <span class="btn-text">加入购物车</span>
                    </div>
                    <div class="action-btn favorite-btn" @click.stop="toggleFavorite(item)">
                      <el-icon class="star-icon" :class="{ active: item.isFavorite }"><Star /></el-icon>
                      <span class="btn-text">{{ item.isFavorite ? '已收藏' : '收藏' }}</span>
                    </div>
                  </div>
                </div>

                <div class="card-details" @click="enterItemDetail(Number(item.id))">
                  <h3 class="title">{{ item.name }}</h3>
                  <div class="meta-row">
                    <div class="price-group">
                      <span class="symbol">￥</span>
                      <span class="amount">{{ formatPrice(item.price) }}</span>
                    </div>
                    <span class="sales">{{ item.sold || 0 }} 人付款</span>
                  </div>
                  <div class="tag-row">
                    <span v-if="item.shippingDesc" class="tag-text blue">{{ item.shippingDesc }}</span>
                    <span v-if="item.category" class="tag-text gray">{{ item.category }}</span>
                    <span v-if="item.brand" class="tag-text red">{{ item.brand }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        <el-empty v-else description="暂无相关商品，换个条件试试" />
      </div>

      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[12, 20, 40, 60]"
          :background="true"
          layout="prev, pager, next, sizes, total, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import {
  ArrowRight,
  Collection,
  CollectionTag,
  Compass,
  Coffee,
  Goods,
  Iphone,
  Odometer,
  Reading,
  Search,
  ShoppingBag,
  ShoppingCart,
  Star,
  Sugar
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { list, type ItemListModel } from '@/api/item'
import { addItem2Cart } from '@/api/cart'
import { addCollect, deleteById, getCollectList } from '@/api/collect'
import { useUserStore } from '@/stores/useUserStore'
import { resolveProductImage } from '@/utils/image'
import { isHandledRequestError } from '@/utils/request'
import heroTechVisual from '@/assets/home/hero-tech-visual.svg'
import heroHomeVisual from '@/assets/home/hero-home-visual.svg'
import channelHeadphones from '@/assets/home/channel-headphones.svg'
import channelCamera from '@/assets/home/channel-camera.svg'

interface HomeItem extends ItemListModel {
  isFavorite?: boolean
  collectId?: number | string
}

type ShortcutKey = 'favorites' | 'cart' | 'orders' | 'brands'
type SearchTab = '宝贝' | '品牌' | '店铺'

const router = useRouter()
const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const loading = ref(false)
const productList = ref<HomeItem[]>([])
const total = ref(0)
const activeBannerIndex = ref(0)
const searchKeyword = ref('')
const activeSearchTab = ref<SearchTab>('宝贝')
const bannerCarouselRef = ref<{ next: () => void; setActiveItem: (index: number) => void } | null>(null)

const queryParams = reactive({
  pageNo: 1,
  pageSize: 12
})

const isLoggedIn = computed(() => userInfo.value.isLoggedIn)
const displayUserName = computed(() => userInfo.value.nickName || userInfo.value.username || 'YU 会员')
const profileAvatarUrl = computed(() => (isLoggedIn.value ? userInfo.value.avatar || '' : ''))
const profileBadgeText = computed(() => (isLoggedIn.value ? displayUserName.value.slice(0, 1).toUpperCase() : 'YU'))
const topGreeting = computed(() => (
  isLoggedIn.value ? `欢迎你，${displayUserName.value}` : '欢迎来到 YU-Mall'
))
const heroGreeting = computed(() => (isLoggedIn.value ? `Hi，${displayUserName.value}` : 'Hi，欢迎来到 YU-Mall'))
const heroGreetingSub = computed(() => (
  isLoggedIn.value
    ? '最近订单、收藏夹和专属权益都在这里。'
    : '登录后可同步订单、收藏和首页个性化推荐。'
))
const searchPlaceholder = computed(() => {
  if (activeSearchTab.value === '品牌') {
    return '搜索品牌 / 官方旗舰店'
  }
  if (activeSearchTab.value === '店铺') {
    return '搜索店铺 / 商家'
  }
  return '搜索商品 / 品牌 / 店铺'
})

const searchTabs: SearchTab[] = ['宝贝', '品牌', '店铺']

const hotKeywords = ['夏季新品', '蓝牙耳机', '居家焕新', '轻食饮品', '品牌馆', '运动户外'] as const

const homeChannels = [
  { label: '天猫', category: '电子产品', keyword: '品牌馆' },
  { label: '聚划算', category: '家居用品', keyword: '补贴' },
  { label: '天猫超市', category: '食品与饮料', keyword: '生鲜' },
  { label: '司法拍卖', category: '电子产品', keyword: '拍卖' },
  { label: '飞猪旅行', category: '户外运动', keyword: '旅行' },
  { label: '天天特卖', category: '服饰与配件', keyword: '特卖' }
] as const

const homeAnnouncements = [
  { tag: '优惠', title: '双重补贴活动上线，家电家居限时直降', category: '家居用品', keyword: '补贴', tone: 'orange' },
  { tag: '公告', title: '品牌馆频道升级，支持按品牌一键筛选', category: '电子产品', keyword: '品牌馆', tone: 'blue' },
  { tag: '活动', title: '夏季新品服饰专场开启，热门单品持续更新', category: '服饰与配件', keyword: '新品', tone: 'purple' },
  { tag: '通知', title: '订单服务体验优化，售后入口已同步调整', category: '电子产品', keyword: '订单', tone: 'green' }
] as const

const categories = [
  { name: '服饰与配件', searchCategory: '服饰与配件', icon: ShoppingBag, tone: 'blue' },
  { name: '电子产品', searchCategory: '电子产品', icon: Iphone, tone: 'purple' },
  { name: '家居用品', searchCategory: '家居用品', icon: Coffee, tone: 'green' },
  { name: '美妆个护', searchCategory: '美妆个护', icon: Star, tone: 'orange' },
  { name: '食品与饮料', searchCategory: '食品与饮料', icon: Sugar, tone: 'amber' },
  { name: '户外运动', searchCategory: '户外运动', icon: Compass, tone: 'teal' },
  { name: '母婴用品', searchCategory: '母婴用品', icon: Goods, tone: 'pink' },
  { name: '汽车用品', searchCategory: '汽车用品', icon: Odometer, tone: 'indigo' },
  { name: '图书音像', searchCategory: '图书音像', icon: Reading, tone: 'cyan' },
  { name: '珠宝首饰', searchCategory: '珠宝首饰', icon: Collection, tone: 'gold' }
] as const

const heroSlides = [
  {
    id: 'tech',
    tag: '超级 88',
    titleLine1: '精选频道限时抢',
    titleLine2: '补贴直降更划算',
    subtitle: '官方立减商品一屏直达，耳机、相机和小家电都有。',
    image: heroTechVisual,
    alt: '数码促销会场视觉',
    category: '电子产品',
    keyword: '蓝牙耳机',
    background: 'linear-gradient(135deg, #2d64f8 0%, #3e80ff 46%, #51befa 100%)'
  },
  {
    id: 'home',
    tag: '焕新季',
    titleLine1: '居家焕新补贴会场',
    titleLine2: '家电家居一起省',
    subtitle: '卧室、客厅和厨房好物集中陈列，跟着首页节奏快速选品。',
    image: heroHomeVisual,
    alt: '居家补贴会场视觉',
    category: '家居用品',
    keyword: '家电',
    background: 'linear-gradient(135deg, #3767d8 0%, #5b82f5 48%, #8faeff 100%)'
  }
] as const

const promoBanners = [
  {
    title: '潮牌运动鞋特卖',
    subtitle: '满300减40',
    image: channelHeadphones,
    alt: '潮牌运动鞋特卖横幅',
    category: '服饰与配件',
    keyword: '运动鞋',
    tone: 'red'
  },
  {
    title: '数码新品首发',
    subtitle: '限时24期免息',
    image: channelCamera,
    alt: '数码新品首发横幅',
    category: '电子产品',
    keyword: '数码新品',
    tone: 'slate'
  }
] as const

const heroShortcuts = [
  { key: 'favorites', label: '收藏', icon: Star, tone: 'blue' },
  { key: 'cart', label: '购物车', icon: ShoppingCart, tone: 'orange' },
  { key: 'orders', label: '订单', icon: Collection, tone: 'green' },
  { key: 'brands', label: '品牌馆', icon: CollectionTag, tone: 'purple' }
] as const

const formatPrice = (price?: string | number | null): string => {
  if (price === undefined || price === null || price === '') {
    return '0.00'
  }
  return (Number(price) / 100).toFixed(2)
}

const quickSearch = (category?: string, keyword?: string) => {
  router.push({
    name: 'Search',
    query: {
      ...(category ? { category } : {}),
      ...(keyword ? { q: keyword } : {})
    }
  })
}

const submitHomeSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (!keyword) {
    return
  }
  quickSearch(undefined, keyword)
}

const handleCategoryClick = (category: string) => {
  quickSearch(category)
}

const handleBannerChange = (current: number) => {
  activeBannerIndex.value = current
}

const showBanner = (index: number) => {
  activeBannerIndex.value = index
  bannerCarouselRef.value?.setActiveItem(index)
}

const goHome = () => {
  router.push('/')
}

const goLogin = () => {
  router.push('/login')
}

const goRegister = () => {
  router.push('/register')
}

const goUserDashboard = () => {
  router.push({ path: '/user', query: { tab: 'dashboard' } })
}

const goUserOrders = () => {
  router.push({ path: '/user', query: { tab: 'orders' } })
}

const goFavorites = () => {
  if (!isLoggedIn.value) {
    goLogin()
    return
  }
  router.push({ path: '/user', query: { tab: 'favorites' } })
}

const goOrders = () => {
  if (!isLoggedIn.value) {
    goLogin()
    return
  }
  goUserOrders()
}

const goDashboard = () => {
  if (!isLoggedIn.value) {
    goLogin()
    return
  }
  goUserDashboard()
}

const goBrandHall = () => {
  quickSearch(undefined, '品牌馆')
}

const handleShortcutClick = (shortcut: ShortcutKey) => {
  if (shortcut === 'cart') {
    router.push('/cart')
    return
  }

  if (!isLoggedIn.value) {
    router.push('/login')
    return
  }

  if (shortcut === 'favorites') {
    router.push({ path: '/user', query: { tab: 'favorites' } })
    return
  }

  if (shortcut === 'orders') {
    goUserOrders()
    return
  }

  goBrandHall()
}

const resetFavoriteState = () => {
  productList.value = productList.value.map((item) => ({ ...item, isFavorite: false, collectId: undefined }))
}

const updateFavoriteStatus = async () => {
  const token = localStorage.getItem('Authorization')
  if (!token || !isLoggedIn.value || !productList.value.length) {
    resetFavoriteState()
    return
  }

  try {
    const res: any = await getCollectList(undefined, {
      errorMeta: {
        silent: true
      }
    })
    const collectList = Array.isArray(res?.data) ? res.data : []
    const collectMap = new Map<number, any>()
    collectList.forEach((collect: any) => {
      const itemId = Number(collect?.itemId)
      if (itemId) {
        collectMap.set(itemId, collect)
      }
    })

    productList.value = productList.value.map((item) => {
      const collect = collectMap.get(Number(item.id))
      return {
        ...item,
        isFavorite: Boolean(collect),
        collectId: collect?.id || collect?.collectId
      }
    })
  } catch (error) {
    console.warn('获取收藏状态失败', error)
  }
}

const syncFavoriteStatus = async () => {
  const token = localStorage.getItem('Authorization')
  if (!token || !isLoggedIn.value || !productList.value.length) {
    resetFavoriteState()
    return
  }

  try {
    const res: any = await getCollectList(undefined, {
      errorMeta: {
        silent: true
      }
    })
    const collectList = Array.isArray(res?.data) ? res.data : []
    const collectMap = new Map<number, any>()
    collectList.forEach((collect: any) => {
      const itemId = Number(collect?.itemId)
      if (itemId) {
        collectMap.set(itemId, collect)
      }
    })

    productList.value = productList.value.map((item) => {
      const collect = collectMap.get(Number(item.id))
      return {
        ...item,
        isFavorite: Boolean(collect),
        collectId: collect?.id || collect?.collectId
      }
    })
  } catch (error) {
    resetFavoriteState()
    if (!isHandledRequestError(error)) {
      console.warn('获取收藏状态失败', error)
    }
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res: any = await list(
      {
        pageNo: queryParams.pageNo,
        pageSize: queryParams.pageSize
      },
      {
        errorMeta: {
          silent: true
        }
      }
    )

    productList.value = Array.isArray(res?.rows) ? res.rows : []
    total.value = Number(res?.total || 0)
    await syncFavoriteStatus()
  } catch (error) {
    console.error('加载商品失败', error)
    productList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (value: number) => {
  queryParams.pageSize = value
  queryParams.pageNo = 1
  loadData()
}

const handleCurrentChange = (value: number) => {
  queryParams.pageNo = value
  loadData()
}

const enterItemDetail = (id: number) => {
  router.push(`/item/${id}`)
}

const addToCart = async (item: HomeItem) => {
  const token = localStorage.getItem('Authorization')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await addItem2Cart({
      itemId: item.id,
      num: 1,
      price: item.price,
      name: item.name,
      image: item.image,
      skuId: 0,
      tags: item.category
    })
    ElMessage.success(`已将 ${item.name} 加入购物车`)
  } catch (error) {
    console.error('加入购物车失败', error)
    if (!(error as any)?.handled) {
      ElMessage.error('加入购物车失败，请稍后重试')
    }
  }
}

const toggleFavorite = async (item: HomeItem) => {
  const token = localStorage.getItem('Authorization')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    if (item.isFavorite && item.collectId) {
      await deleteById(Number(item.collectId))
      item.isFavorite = false
      item.collectId = undefined
      ElMessage.success('已取消收藏')
      return
    }

    await addCollect({
      itemId: Number(item.id),
      name: item.name,
      image: item.image || '',
      price: item.price,
      tags: item.category || ''
    })
    item.isFavorite = true
    await syncFavoriteStatus()
    ElMessage.success('收藏成功')
  } catch (error) {
    console.error('收藏操作失败', error)
    if (!(error as any)?.handled) {
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.home-page {
  min-height: 100%;
  background:
    linear-gradient(180deg, #edf3fb 0%, #f5f8fd 30%, #f6f8fc 100%),
    radial-gradient(circle at top center, rgba(102, 151, 255, 0.18), transparent 32%);
}

.home-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  padding: 0 16px;
}

.home-header__shell {
  width: min(1280px, 100%);
  border: 1px solid rgba(212, 222, 239, 0.92);
  border-top: none;
  border-radius: 0 0 18px 18px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 10px 22px rgba(24, 45, 86, 0.06);
  overflow: hidden;
}

.home-topbar {
  min-height: 36px;
  padding: 0 22px;
  border-bottom: 1px solid rgba(222, 230, 241, 0.96);
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.home-topbar__group {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.home-topbar__group--right {
  justify-content: flex-end;
}

.home-topbar__link {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  color: #6a7c97;
  cursor: pointer;
  transition: color 0.2s ease;
}

.home-topbar__link:hover,
.home-topbar__link:focus-visible {
  color: #2253d8;
}

.home-topbar__greeting {
  font-size: 13px;
  color: #8494ad;
}

.home-searchbar {
  padding: 18px 22px 14px;
  display: grid;
  grid-template-columns: 188px minmax(0, 1fr);
  align-items: center;
  gap: 18px;
  background: linear-gradient(180deg, #ffffff 0%, #fdfefe 100%);
}

.home-brand {
  border: none;
  background: transparent;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  text-align: left;
  cursor: pointer;
}

.home-brand__badge {
  width: 52px;
  height: 52px;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2f64ee 0%, #4d8bff 100%);
  color: #fff;
  font-size: 22px;
  font-weight: 800;
  box-shadow: 0 12px 22px rgba(47, 100, 238, 0.18);
}

.home-brand__copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.home-brand__copy strong {
  color: #20386f;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1.02;
}

.home-brand__copy small {
  color: #8393ae;
  font-size: 12px;
  line-height: 1.35;
}

.home-search-panel {
  min-width: 0;
}

.home-search-panel__body {
  position: relative;
  padding-top: 22px;
}

.home-search-tabs {
  display: flex;
  align-items: center;
  gap: 6px;
  position: absolute;
  left: 0;
  top: 0;
  z-index: 2;
}

.home-search-tab {
  min-width: 46px;
  height: 26px;
  border: none;
  border-radius: 8px 8px 0 0;
  background: rgba(62, 107, 228, 0.08);
  color: #4f6a9d;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.home-search-tab.active {
  background: linear-gradient(135deg, #2f64ee 0%, #4a86fb 100%);
  color: #fff;
  box-shadow: 0 8px 16px rgba(47, 100, 238, 0.14);
}

.home-search-box {
  height: 44px;
  border: 2px solid #4d82f2;
  border-radius: 0 24px 24px 24px;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 6px 0 14px;
  gap: 10px;
  box-shadow: none;
}

.home-search-box__icon {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #7d8eab;
  font-size: 16px;
}

.home-search-box__input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  color: #1f2f4d;
  font-size: 14px;
}

.home-search-box__input::placeholder {
  color: #9ba8bd;
}

.home-search-box__submit {
  min-width: 114px;
  height: 34px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #2f64ee 0%, #4d89ff 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 16px rgba(47, 100, 238, 0.18);
}

.home-hotwords {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding-left: 10px;
}

.home-hotwords__item {
  border: none;
  background: transparent;
  padding: 0;
  color: #6c7d98;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.home-hotwords__item:hover,
.home-hotwords__item:focus-visible {
  color: #f07c45;
}

.home-shell {
  width: min(1280px, calc(100% - 32px));
  margin: 0 auto;
  padding: 186px 0 64px;
}

/* 导航栏排版 */
.channel-nav-row {
  display: grid;
  grid-template-columns: 224px minmax(0, 1fr) 256px;
  gap: 16px;
  margin-bottom: 12px;
}

.stage-nav {
  grid-column: 2; /* 居中与舞台对齐 */
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 0 4px;
  overflow-x: auto;
}

.stage-nav__item {
  flex: 0 0 auto;
  border: none;
  background: transparent;
  padding: 0;
  color: #1f2937;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.2s ease;
  position: relative;
}

.stage-nav__item:first-child {
  color: #ff6a00;
}

.stage-nav__item:nth-child(3) {
  padding-right: 24px;
}

.stage-nav__item:nth-child(3)::after {
  content: '|';
  position: absolute;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  color: #d1d5db;
  font-size: 18px;
  font-weight: 600;
}

.stage-nav__item:hover,
.stage-nav__item:focus-visible {
  color: #ff6a00;
}

/* 强制三列等高拉伸，实现顶部底部双对齐 */
.hero-grid {
  display: grid;
  grid-template-columns: 224px minmax(0, 1fr) 256px;
  gap: 16px;
  align-items: stretch; /* 关键：强制等高 */
}

.hero-panel {
  border: 1px solid #d8e3f7;
  border-radius: 24px;
  background: #f8fbff;
  box-shadow: 0 10px 24px rgba(18, 38, 77, 0.06);
}

.panel-heading {
  margin-bottom: 16px;
}

.panel-heading h2,
.panel-heading h3 {
  margin: 0;
  color: #16325f;
  font-size: 18px;
  font-weight: 800;
}

.panel-heading p {
  margin: 6px 0 0;
  color: #6b7f9f;
  font-size: 12px;
}

/* 左侧：分类栏，自动填满拉伸后的空间 */
.category-panel {
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
}

.category-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 自动分配间距，实现等高 */
  gap: 0;
}

.category-item {
  width: 100%;
  border: none;
  border-radius: 18px;
  background: transparent;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #20314d;
  cursor: pointer;
  transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
}

.category-item:hover,
.category-item:focus-visible {
  transform: translateX(4px);
  background: rgba(228, 238, 255, 0.72);
  color: #2253d8;
}

.category-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-icon {
  width: 30px;
  height: 30px;
  border-radius: 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.category-label {
  font-size: 14px;
  font-weight: 600;
}

.category-arrow {
  color: #aebbd0;
  font-size: 14px;
}

/* 中间：舞台面板，设定总基准高度 512px */
.stage-panel {
  padding: 0;
  border: none;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 512px; /* 固定中间高度，迫使左右自动拉伸到此基准 */
  min-width: 0;
}

.hero-banner-card {
  position: relative;
  height: 364px; /* 精确高度计算 512-16-132 = 364px */
  overflow: hidden;
  border-radius: 24px;
}

:deep(.el-carousel__container) {
  height: 364px !important;
}

.hero-banner-slide {
  position: relative;
  width: 100%;
  height: 100%;
  padding: 40px 34px 62px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  text-align: left;
  cursor: pointer;
  outline: none;
  overflow: hidden;
}

.hero-banner-slide::after {
  content: '';
  position: absolute;
  right: -18%;
  top: -14%;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
}

.hero-banner-copy {
  position: relative;
  z-index: 1;
  max-width: 320px;
}

.hero-banner-copy h3 {
  margin: 0;
  color: #fff;
  font-size: 36px;
  line-height: 1.06;
  font-weight: 800;
  letter-spacing: -0.06em;
}

.hero-banner-copy p {
  margin: 16px 0 0;
  color: rgba(255, 255, 255, 0.94);
  font-size: 14px;
  line-height: 1.7;
}

.hero-banner-tag {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 14px;
  margin-bottom: 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.36);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
}

.hero-banner-visual {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  justify-content: flex-end;
  min-width: 0;
}

.hero-banner-image {
  width: min(100%, 360px);
  height: 260px;
  object-fit: contain;
  filter: drop-shadow(0 24px 30px rgba(17, 32, 76, 0.18));
}

.hero-banner-dots {
  position: absolute;
  left: 34px;
  bottom: 24px;
  display: flex;
  gap: 10px;
  z-index: 2;
}

.hero-banner-dot {
  width: 12px;
  height: 12px;
  padding: 0;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.56);
  cursor: pointer;
  transition: width 0.2s ease, background-color 0.2s ease;
}

.hero-banner-dot.active {
  width: 30px;
  background: #fff;
}

.promo-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  height: 132px; /* 固定高度 */
}

.promo-banner {
  min-width: 0;
  height: 100%;
  border: none;
  border-radius: 24px;
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  text-align: left;
}

.promo-banner::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0.38) 0%, rgba(0, 0, 0, 0.16) 42%, rgba(0, 0, 0, 0.04) 100%);
  pointer-events: none;
  z-index: 1;
}

.promo-banner--red {
  background: linear-gradient(90deg, #960606 0%, #c30f12 38%, #ef1d25 100%);
}

.promo-banner--slate {
  background: linear-gradient(90deg, #838d9d 0%, #a0a8b4 48%, #bcc3ce 100%);
}

.promo-banner__copy {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 55%;
}

.promo-banner__copy h3 {
  margin: 0;
  color: #fff;
  font-size: 18px;
  line-height: 1.2;
  font-weight: 800;
}

.promo-banner__copy p {
  margin: 0;
  color: rgba(255, 255, 255, 0.92);
  font-size: 14px;
  font-weight: 600;
}

.promo-banner__visual {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 55%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  z-index: 1;
}

.promo-banner__visual img {
  width: 100%;
  height: 145%;
  object-fit: contain;
  object-position: right bottom;
  transform: translate(8px, 12px);
  opacity: 0.96;
  filter: drop-shadow(-12px 12px 24px rgba(15, 23, 42, 0.22));
}

/* 右侧栏：个人中心压缩调整，配合外层高度自动拉伸到底 */
.user-rail {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.profile-card {
  padding: 14px 16px; /* 适当压缩给底部留空间 */
  background: linear-gradient(160deg, #f2f6fd 0%, #f7f9fe 100%);
  flex-shrink: 0;
}

.profile-card__top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.profile-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3e80ff 0%, #51befa 100%);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  overflow: hidden;
  flex-shrink: 0;
}

.profile-avatar__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.profile-copy h3 {
  margin: 0 0 4px;
  color: #20314d;
  font-size: 15px;
  font-weight: 800;
}

.profile-copy p {
  margin: 0;
  color: #6b7f9f;
  font-size: 12px;
  line-height: 1.6;
}

.profile-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.primary-btn,
.secondary-btn {
  flex: 1;
  height: 32px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.primary-btn {
  border: none;
  background: #2253d8;
  color: #fff;
}

.secondary-btn {
  border: 1px solid #d8e3f7;
  background: rgba(255, 255, 255, 0.84);
  color: #2253d8;
}

.profile-benefits {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.benefit-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.benefit-chip--orange {
  background: #fef2ef;
  color: #f07c45;
}

.benefit-chip--blue {
  background: #eef5ff;
  color: #2f74ff;
}

.benefit-chip--purple {
  background: #f3f1ff;
  color: #8e52ea;
}

.profile-shortcuts {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid rgba(216, 227, 247, 0.95);
}

.shortcut-card__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.shortcut-card__heading h3 {
  margin: 0;
  color: #20314d;
  font-size: 14px;
  font-weight: 800;
}

.shortcut-card__heading span {
  color: #6b7f9f;
  font-size: 11px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.shortcut-item {
  border: none;
  background: transparent;
  padding: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #20314d;
  font-size: 11px;
  cursor: pointer;
}

.shortcut-icon {
  width: 32px;
  height: 32px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.notice-card {
  padding: 14px 16px;
  background: linear-gradient(180deg, #f7fbff 0%, #f3f6fd 100%);
  flex: 1; /* 自动撑满剩余高度，实现与另外两列等高 */
  display: flex;
  flex-direction: column;
}

.notice-card__heading {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}

.notice-card__heading h3 {
  margin: 0;
  color: #20314d;
  font-size: 16px;
  font-weight: 800;
}

.notice-card__heading span {
  color: #6b7f9f;
  font-size: 12px;
}

.notice-list {
  list-style: none;
  margin: 0;
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 自动拉开间距贴紧下边缘 */
}

.notice-item {
  width: 100%;
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.notice-item:hover,
.notice-item:focus-visible {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px rgba(18, 38, 77, 0.08);
}

.notice-tag {
  flex: 0 0 auto;
  min-width: 44px;
  height: 24px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

.notice-tag--orange {
  background: #fef2ef;
  color: #f07c45;
}

.notice-tag--blue {
  background: #eef5ff;
  color: #2f74ff;
}

.notice-tag--purple {
  background: #f3f1ff;
  color: #8e52ea;
}

.notice-tag--green {
  background: #eef9f3;
  color: #2d9c68;
}

.notice-text {
  min-width: 0;
  color: #3f526d;
  font-size: 13px;
  line-height: 1.5;
}

.tone-blue {
  background: #eef5ff;
  color: #2f74ff;
}

.tone-purple {
  background: #f2f2fe;
  color: #8e52ea;
}

.tone-green {
  background: #f0f9f5;
  color: #298d61;
}

.tone-orange {
  background: #fef2ef;
  color: #f04e3c;
}

.tone-amber {
  background: #fff6e7;
  color: #d0741e;
}

.tone-teal {
  background: #edf8f2;
  color: #0b7859;
}

.tone-pink {
  background: #fff2f6;
  color: #c73984;
}

.tone-indigo {
  background: #f4f4ff;
  color: #4c4eb8;
}

.tone-cyan {
  background: #f1faff;
  color: #2b91b8;
}

.tone-gold {
  background: #fff4ed;
  color: #da7c17;
}

.product-list-section {
  margin-top: 24px;
}

.product-card {
  border: none;
  border-radius: 12px;
  margin-bottom: 20px;
  transition: all 0.3s;
  overflow: hidden;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.image-container {
  position: relative;
  height: 200px;
  background: #f8f8f8;
  overflow: hidden;
}

.prod-img {
  width: 100%;
  height: 100%;
}

.prod-img--fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f8f8;
}

.prod-img__fallback-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.action-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 15px 10px;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  display: flex;
  justify-content: space-around;
  gap: 8px;
}

.product-card:hover .action-overlay {
  transform: translateY(0);
}

.action-btn {
  flex: 1;
  height: 36px;
  border-radius: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
}

.btn-text {
  margin-left: 4px;
}

.cart-btn {
  background: rgba(255, 255, 255, 0.95);
  color: #409eff;
}

.favorite-btn {
  background: rgba(255, 255, 255, 0.95);
  color: #666;
}

.star-icon.active {
  color: #ff4d4f;
}

.card-details {
  padding: 14px;
}

.title {
  font-size: 13px;
  color: #333;
  margin: 0 0 8px;
  line-height: 20px;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
}

.price-group {
  color: #ff4d4f;
  font-weight: 700;
}

.symbol {
  font-size: 12px;
  margin-right: 1px;
}

.amount {
  font-size: 18px;
}

.sales {
  font-size: 12px;
  color: #999;
}

.tag-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-text {
  font-size: 10px;
  padding: 0 5px;
  border-radius: 4px;
  border: 1px solid;
}

.tag-text.blue {
  color: #409eff;
  border-color: rgba(64, 158, 255, 0.2);
  background: rgba(64, 158, 255, 0.05);
}

.tag-text.red {
  color: #f56c6c;
  border-color: rgba(245, 108, 108, 0.2);
  background: rgba(245, 108, 108, 0.05);
}

.tag-text.gray {
  color: #909399;
  border-color: #e9e9eb;
  background: #f4f4f5;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

:deep(.el-carousel__indicators),
:deep(.el-carousel__arrow) {
  display: none;
}

@media (max-width: 1200px) {
  .home-searchbar {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .home-brand {
    padding-top: 0;
    justify-content: flex-start;
  }

  .home-search-panel__body {
    max-width: none;
  }

  .channel-nav-row {
    grid-template-columns: 224px minmax(0, 1fr);
  }

  .hero-grid {
    grid-template-columns: 224px minmax(0, 1fr);
    grid-template-areas:
      'categories stage'
      'rail rail';
  }

  .category-panel {
    grid-area: categories;
  }

  .stage-panel {
    grid-area: stage;
  }

  .user-rail {
    grid-area: rail;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .home-shell {
    padding-top: 224px;
  }

  .channel-nav-row {
    grid-template-columns: 1fr;
    margin-bottom: 8px;
  }

  .stage-nav {
    grid-column: 1;
    padding: 0 8px;
  }

  .hero-grid {
    grid-template-columns: 1fr;
    grid-template-areas: none;
  }

  .promo-row {
    grid-template-columns: 1fr;
  }

  .user-rail {
    grid-template-columns: 1fr;
  }

}

@media (max-width: 768px) {
  .home-header {
    padding: 0 10px;
  }

  .home-header__shell {
    width: 100%;
    border-radius: 0 0 18px 18px;
  }

  .home-topbar {
    padding: 8px 16px;
  }

  .home-searchbar {
    padding: 16px 16px 14px;
    gap: 12px;
  }

  .home-brand {
    gap: 10px;
  }

  .home-brand__badge {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    font-size: 20px;
  }

  .home-brand__copy strong {
    font-size: 22px;
  }

  .home-brand__copy small {
    font-size: 11px;
  }

  .home-search-panel__body {
    padding-top: 20px;
  }

  .home-search-tab {
    min-width: 44px;
    height: 24px;
  }

  .home-search-box {
    height: 42px;
    border-radius: 0 22px 22px 22px;
    padding-left: 12px;
    gap: 8px;
  }

  .home-search-box__submit {
    min-width: 88px;
    height: 32px;
    font-size: 13px;
  }

  .home-hotwords {
    margin-top: 6px;
    gap: 8px;
    padding-left: 4px;
  }

  .home-shell {
    width: min(100%, calc(100% - 20px));
    padding-top: 246px;
  }

  .category-panel {
    min-height: auto;
  }

  .stage-nav {
    gap: 16px;
    padding: 0;
  }

  .stage-panel {
    height: auto; /* 移动端取消固定高度 */
  }

  .hero-banner-card,
  :deep(.el-carousel__container),
  .hero-banner-slide {
    height: 280px !important;
  }

  .hero-banner-slide {
    padding: 28px 24px 58px;
  }

  .hero-banner-copy h3 {
    font-size: 28px;
  }

  .hero-banner-image {
    width: min(100%, 260px);
    height: 200px;
  }

  .promo-row {
    height: auto;
  }

  .promo-banner {
    min-height: 120px;
    padding: 18px;
  }

  .promo-banner__copy h3 {
    font-size: 16px;
  }

  .promo-banner__copy p {
    font-size: 13px;
  }

  .shortcut-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .home-topbar {
    justify-content: center;
    padding: 8px 12px;
  }

  .home-topbar__group {
    justify-content: center;
    gap: 10px;
  }

  .home-searchbar {
    padding: 14px 12px 12px;
    gap: 12px;
  }

  .home-brand {
    gap: 8px;
  }

  .home-brand__badge {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    font-size: 18px;
  }

  .home-brand__copy strong {
    font-size: 20px;
  }

  .home-brand__copy small {
    font-size: 10px;
  }

  .home-search-tabs {
    gap: 4px;
  }

  .home-search-tab {
    min-width: 42px;
    height: 23px;
    font-size: 11px;
  }

  .home-search-box {
    height: 40px;
    padding-left: 12px;
    gap: 8px;
  }

  .home-search-box__submit {
    min-width: 76px;
    height: 30px;
    font-size: 12px;
  }

  .home-hotwords {
    gap: 6px 8px;
    padding-left: 2px;
  }

  .home-shell {
    padding-top: 258px;
  }

  .hero-banner-card,
  :deep(.el-carousel__container),
  .hero-banner-slide {
    height: 240px !important;
  }

  .hero-banner-slide {
    padding: 22px 18px 52px;
  }

  .hero-banner-copy {
    max-width: 180px;
  }

  .hero-banner-copy h3 {
    font-size: 22px;
  }

  .hero-banner-copy p {
    font-size: 12px;
    margin-top: 10px;
  }

  .hero-banner-image {
    width: min(100%, 176px);
    height: 150px;
  }

  .hero-banner-dots {
    left: 18px;
    bottom: 18px;
  }
}
</style>
