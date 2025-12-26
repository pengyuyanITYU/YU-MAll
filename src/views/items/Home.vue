<template>
  <div class="home-page">
    <!-- 背景装饰球 -->
    <div class="bg-decoration"></div>

    <div class="container main-content">
      
      <!-- 1. Hero 区域 -->
      <div class="hero-section-wrapper">
        <el-row :gutter="16" class="hero-row">
          <!-- 左侧：分类导航 -->
          <el-col :xs="0" :sm="5" :md="5" :lg="5" class="hidden-xs-only hero-col">
            <div class="category-box glass-panel">
              <div class="box-header">
                <div class="header-icon">
                  <el-icon><Menu /></el-icon>
                </div>
                <span class="title">全部分类</span>
              </div>
              <el-scrollbar class="category-scroll">
                <ul class="custom-menu">
                  <li 
                    v-for="(item, index) in categories" 
                    :key="item.name" 
                    class="menu-item"
                    @click="handleCategoryClick(item.name)"
                  >
                    <div class="label-group">
                      <span class="icon-wrap">
                        <component :is="item.icon" class="cat-icon" />
                      </span>
                      <span class="text">{{ item.name }}</span>
                    </div>
                    <el-icon class="arrow"><ArrowRight /></el-icon>
                  </li>
                </ul>
              </el-scrollbar>
            </div>
          </el-col>

          <!-- 中间：主轮播图 -->
          <el-col :xs="24" :sm="13" :md="13" :lg="13" class="hero-col">
            <div class="banner-box">
              <el-carousel 
                trigger="click" 
                height="420px" 
                class="main-carousel" 
                :interval="4000"
                indicator-position="outside"
                arrow="hover"
              >
                <el-carousel-item v-for="item in banners" :key="item.id">
                  <img :src="item.img" class="banner-img" alt="Banner" />
                  <div class="banner-overlay">
                    <div class="banner-content">
                      <h3 class="banner-title">限时特惠</h3>
                      <p class="banner-desc">精选好物,低至5折起</p>
                    </div>
                  </div>
                </el-carousel-item>
              </el-carousel>
            </div>
          </el-col>

          <!-- 右侧：创意营销面板 -->
          <el-col :xs="0" :sm="6" :md="6" :lg="6" class="hidden-xs-only hero-col">
            <div class="promo-dashboard">
              <div class="creative-card card-luxury" @click="quickSearch('奢品')">
                <div class="luxury-bg"></div>
                <div class="glass-content">
                  <div class="text-zone">
                    <div class="brand-badge gold"><span class="line"></span> LUXURY</div>
                    <h3 class="title-main">奢品 <span>大牌日</span></h3>
                    <p class="title-sub">尊享 12 期免息</p>
                    <el-button class="luxury-btn" round size="small">探索臻品 <el-icon><ArrowRight /></el-icon></el-button>
                  </div>
                  <div class="visual-zone">
                    <img src="https://images.unsplash.com/photo-1541643600914-78b084683601?q=80&w=300&auto=format&fit=crop" class="floating-img" />
                    <div class="price-tag-minimal">¥12999</div>
                  </div>
                </div>
              </div>
              <div class="creative-card card-daily" @click="quickSearch('居家')">
                <div class="daily-bg"></div>
                <div class="glass-content light-glass">
                  <div class="text-zone">
                    <div class="brand-badge cyan"><el-icon><Sugar /></el-icon> DAILY LIFE</div>
                    <h3 class="title-main text-dark">居家 <span>焕新</span></h3>
                    <p class="title-sub text-grey">满199减50</p>
                    <el-button class="daily-btn" round size="small">立即囤货</el-button>
                  </div>
                  <div class="visual-zone">
                    <img src="https://images.unsplash.com/photo-1620916566398-39f1143ab7be?q=80&w=300&auto=format&fit=crop" class="floating-img rotate-left" />
                  </div>
                </div>
              </div>
              <div class="creative-card card-tech" @click="quickSearch('数码')">
                <div class="cyber-bg"></div>
                <div class="glass-content">
                  <div class="text-zone">
                    <div class="brand-badge neon"><span class="dot"></span> FLASH SALE</div>
                    <h3 class="title-main">极客 <span>前沿</span></h3>
                    <p class="title-sub">黑科技首发</p>
                    <el-button class="tech-btn" round size="small">去抢购</el-button>
                  </div>
                  <div class="visual-zone">
                    <div class="glitch-circle"></div>
                    <img src="https://images.unsplash.com/photo-1600080972464-8e5f35f63d08?q=80&w=300&auto=format&fit=crop" class="floating-img" />
                    <div class="discount-tag">-20%</div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 2. 筛选栏 -->
      <div class="filter-bar-wrapper glass-panel">
        <div class="filter-left">
          <span class="main-label">筛选:</span>
          
          <el-popover placement="bottom-start" :width="320" trigger="click" :visible="pricePopoverVisible">
            <template #reference>
              <div class="filter-pill" @click="pricePopoverVisible = !pricePopoverVisible">
                <span>{{ priceLabel }}</span>
                <el-icon class="icon-down"><ArrowDown /></el-icon>
              </div>
            </template>
            <div class="price-popover">
              <div class="popover-header">自定义价格区间 (元)</div>
              <el-slider v-model="priceRange" range :max="10000" class="price-slider"/>
              <div class="price-inputs">
                <el-input-number v-model="priceRange[0]" :controls="false" placeholder="最低" />
                <span class="separator">-</span>
                <el-input-number v-model="priceRange[1]" :controls="false" placeholder="最高" />
              </div>
              <div class="popover-footer">
                <el-button text size="small" @click="resetPrice">重置</el-button>
                <el-button type="primary" size="small" @click="confirmPrice">确定</el-button>
              </div>
            </div>
          </el-popover>

          <div class="brand-list">
             <span 
               v-for="brand in brands" 
               :key="brand" 
               class="brand-item" 
               :class="{ active: queryParams.brand === brand }" 
               @click="handleBrandClick(brand)"
             >
               {{ brand === '' ? '全部' : brand }}
             </span>
          </div>
        </div>

        <div class="filter-right">
           <div 
             class="sort-tab" 
             :class="{ active: queryParams.sort === 'default' }"
             @click="handleSort('default')"
           >综合</div>
           <div 
             class="sort-tab" 
             :class="{ active: queryParams.sort === 'sales' }"
             @click="handleSort('sales')"
           >销量</div>
           <div 
             class="sort-tab" 
             :class="{ active: queryParams.sort.includes('price') }"
             @click="handleSortPrice"
           >
             价格 
             <el-icon v-if="queryParams.sort === 'price_asc'"><CaretTop /></el-icon>
             <el-icon v-else-if="queryParams.sort === 'price_desc'"><CaretBottom /></el-icon>
             <el-icon v-else><Sort /></el-icon>
           </div>
        </div>
      </div>

      <!-- 3. 商品列表 -->
      <div class="product-list-section" v-loading="loading" element-loading-text="精彩好物加载中...">
        <div v-if="productList.length > 0">
          <el-row :gutter="16">
            <el-col v-for="item in productList" :key="item.id" :xs="12" :sm="8" :md="6" :lg="4" :xl="4">
              <el-card class="product-card" shadow="hover" :body-style="{ padding: '0px' }">
                <div class="image-container">
                  <el-image 
                    :src="item.image" 
                    @click="enterItemDetail(item.id)" 
                    fit="cover" 
                    loading="lazy" 
                    class="prod-img" 
                  />
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
                
                <div class="card-details" @click="enterItemDetail(item.id)">
                    <h3 class="title" :title="item.title">
                      <span v-if="queryParams.name" v-html="item.title.replace(new RegExp(queryParams.name, 'gi'), `<span style='color:red'>${queryParams.name}</span>`)"></span>
                      <span v-else>{{ item.name }}</span>
                    </h3>
                    <div class="meta-row">
                        <div class="price-group">
                          <span class="symbol">¥</span>
                          <span class="amount">{{ formatPrice(item.price) }}</span>
                        </div>
                        <span class="sales">{{ item.sold > 999 ? '999+' : item.sold }}人付款</span>
                    </div>
                    <div class="tag-row">
                        <span v-if="item.freeShipping" class="tag-text blue">包邮</span>
                        <span v-if="item.coupon" class="tag-text red">券</span>
                    </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
        <el-empty v-else description="暂无相关商品，换个词试试？" />
      </div>

      <!-- 4. 分页器 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.pageNo"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[5, 10, 15,20,50]"
          :background="true"
          layout="prev, pager, next,sizes,total, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { 
  Menu, ArrowRight, ArrowDown, Sort, ShoppingCart, Sugar,
  Iphone, Monitor, Coffee, Goods, CaretTop, CaretBottom, ShoppingBag, Star
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { list } from '@/api/item';
import { addItem2Cart } from '@/api/cart'; 
import { getCollectList, addCollect, deleteById } from '@/api/collect';

const route = useRoute();
const router = useRouter();

// --- 核心状态 ---
const loading = ref(false);
const productList = ref<any[]>([]);
const total = ref(0);

// 查询参数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  name: '',
  category: '',
  brand: '',
  sort: 'default',
  minPrice: undefined as number | string | undefined,
  maxPrice: undefined as number |  string |undefined
});

// --- 数据获取方法 ---
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      ...queryParams,
      name: queryParams.name || (route.query.q as string) || '' 
    };
    const res: any = await list(params);
    if (res && res.code === 200) {
      productList.value = res.rows || [];
      total.value = res.total || 0;
      // 加载完商品后同步收藏状态
      await updateFavoriteStatus();
    } else {
      productList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('加载失败:', error);
    ElMessage.error('商品加载失败');
  } finally {
    loading.value = false;
  }
};

// --- 同步收藏状态 ---
const updateFavoriteStatus = async () => {
  const token = localStorage.getItem('Authorization');
  if (!token) return;

  try {
    const res: any = await getCollectList();
    const collectList = res.data || []; // 确保是数组
    
    // 使用 Set 存储已收藏的商品ID，提高查找效率
    const favoriteItemIds = new Set();
    
    collectList.forEach((c: any) => {
      // 假设后端返回的对象中 itemId 代表商品ID
      if (c.itemId) {
        favoriteItemIds.add(c.itemId);
      }
    });

    // 遍历商品列表，如果商品ID在 Set 中，则标记为已收藏
    productList.value.forEach(p => {
      if (favoriteItemIds.has(p.id)) {
        p.isFavorite = true;
      } else {
        p.isFavorite = false;
      }
    });
  } catch (error) {
    console.warn('获取收藏状态失败', error);
  }
};

// --- 事件处理 ---
const handleCategoryClick = (category: string) => {
  queryParams.category = category;
  queryParams.pageNo = 1;
  loadData();
  ElMessage.info(`已选择分类：${category}`);
};

const quickSearch = (keyword: string) => {
  queryParams.name = keyword;
  queryParams.pageNo = 1;
  loadData();
  ElMessage.info(`正在搜索：${keyword}`);
};

const handleSizeChange = (val: number) => {
  queryParams.pageSize = val;
  queryParams.pageNo = 1; 
  loadData();
};
const handleCurrentChange = (val: number) => {
  queryParams.pageNo = val;
  loadData();
};

const handleSort = (type: string) => {
  if (queryParams.sort === type) return;
  queryParams.sort = type;
  queryParams.pageNo = 1;
  loadData();
};

const handleSortPrice = () => {
  if (queryParams.sort === 'price_asc') {
    queryParams.sort = 'price_desc';
  } else {
    queryParams.sort = 'price_asc';
  }
  queryParams.pageNo = 1;
  loadData();
};

const formatPrice = (price?: string | number | null): string => {
  if (price === undefined || price === null || price === '') return '0.00';
  const str = price.toString();
  const paddedStr = str.padStart(3, '0');
  const integerPart = paddedStr.slice(0, -2);
  const decimalPart = paddedStr.slice(-2);
  return `${integerPart}.${decimalPart}`;
};

const enterItemDetail = (id: number) => { 
  router.push(`/item/${id}`); 
};

const addToCart = async (item: any) => {
  const token = localStorage.getItem('Authorization');
  if (!token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  try {
    const params = {
      itemId: item.id,
      num: 1,
      price: item.price,
      name: item.name,
      image: item.image,
      skuId: item.defaultSkuId || 0, 
      tags: item.tags 
    };
    await addItem2Cart(params);
    ElMessage.success(`已将 ${item.name} 加入购物车`);
  } catch (error) {
    console.error(error);
    ElMessage.error('加购失败，请重试');
  }
}

// === 修改后的 toggleFavorite ===
const toggleFavorite = async (item: any) => {
  const token = localStorage.getItem('Authorization');
  if (!token) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }

  try {
    if (item.isFavorite) {
      // --- 取消收藏 ---
      // 这里的 item.id 是商品ID，直接传给后端
      await deleteById(item.id);
      
      item.isFavorite = false;
      ElMessage.success('已取消收藏');
      // 可选：更新全局状态
      // await updateFavoriteStatus();  
    } else {
      // --- 添加收藏 ---
      const data:any = {
        itemId: item.id,
        name: item.name,
        spec: {},
        price: item.price,
        image: item.image
      };
      
      await addCollect(data);
      
      item.isFavorite = true;
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    console.error('操作收藏失败', error);
    ElMessage.error('操作失败，请稍后重试');
  }
};

const scrollToProductTop = () => {
  const target = document.querySelector('.product-list-section');
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
};

watch(() => route.query.q, (newVal) => {
  queryParams.name = (newVal as string) || '';
  queryParams.pageNo = 1;
  loadData();
});

onMounted(() => {
  if (route.query.q) {
    queryParams.name = route.query.q as string;
  }
  loadData();
});

// --- UI 数据 ---
const categories = [
  { name: '家电', icon: Coffee }, { name: '数码', icon: Monitor },       
  { name: '食品', icon: ShoppingBag }, { name: '服装', icon: Iphone }, { name: '母婴', icon: Goods },         
];
const banners = [
  { id: 1, img: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=1200&q=80' },
  { id: 2, img: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1200&q=80' },
];
const brands = ['全部', 'Sony', 'Apple', 'Huawei']; 
const priceRange = ref([0, 10000]);
const pricePopoverVisible = ref(false);
const priceLabel = computed(() => {
   return (queryParams.minPrice || queryParams.maxPrice) 
     ? `${queryParams.minPrice || 0} - ${queryParams.maxPrice || '不限'}` 
     : '价格范围';
});
const resetPrice = () => { 
  priceRange.value = [0, 10000]; 
  queryParams.minPrice = undefined; 
  queryParams.maxPrice = undefined; 
  loadData(); 
};
const confirmPrice = () => {
  queryParams.minPrice = priceRange.value[0];
  queryParams.maxPrice = priceRange.value[1];
  pricePopoverVisible.value = false;
  loadData();
};
const handleBrandClick = (b: string) => {
  queryParams.brand = b === '全部' ? '' : b;
  loadData();
};
</script>

<style scoped lang="scss">
/* === 变量 === */
$hero-height: 440px;
$primary: #409eff;
$gradient-primary: linear-gradient(135deg, $primary, #79bbff);

/* 1. 页面容器 */
.home-page {
  background-color: #f2f4f8;
  min-height: 100vh;
  position: relative;
}

/* 氛围装饰背景 */
.bg-decoration {
  position: absolute;
  top: 0; left: 0; right: 0; height: 600px;
  background: linear-gradient(180deg, #dcecfb 0%, rgba(242,244,248,0) 100%);
  z-index: 0;
  pointer-events: none;
  &::before {
    content: ''; position: absolute; top: -100px; right: 5%; width: 600px; height: 600px;
    background: radial-gradient(circle, rgba(64,158,255,0.08) 0%, transparent 70%); border-radius: 50%;
  }
  &::after {
    content: ''; position: absolute; top: 50px; left: -5%; width: 400px; height: 400px;
    background: radial-gradient(circle, rgba(54, 207, 201, 0.08) 0%, transparent 70%); border-radius: 50%;
  }
}

/* 3. 内容容器 */
.container {
  max-width: 1280px; 
  margin: 0 auto; 
  padding: 0 16px;
  position: relative; 
  z-index: 1;
  padding-top: 60px !important; 
}

/* Hero 区域 */
.hero-section-wrapper {
  margin-bottom: 24px;
  margin-top: 20px !important;
  position: relative;
  &::before {
    content: '';
    position: absolute;
    top: -20px;
    left: 0;
    right: 0;
    height: 20px;
    background: linear-gradient(to bottom, rgba(0,0,0,0.02), transparent);
  }
}
.hero-col { height: $hero-height; }

/* 左侧分类 */
.glass-panel {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 8px 20px rgba(0,0,0,0.03);
}

.category-box {
  height: 100%; display: flex; flex-direction: column; overflow: hidden; border-radius: 16px;
  .box-header {
    padding: 20px; display: flex; align-items: center; gap: 12px; border-bottom: 1px solid rgba(0,0,0,0.03);
    .header-icon {
      width: 36px; height: 36px; background: linear-gradient(135deg, #409eff, #a0cfff);
      border-radius: 10px; display: flex; justify-content: center; align-items: center;
      color: #fff; font-size: 18px; box-shadow: 0 4px 10px rgba(64,158,255,0.3);
    }
    .title { font-weight: 800; font-size: 16px; color: #2c3e50; letter-spacing: 1px; }
  }
  .category-scroll { flex: 1; :deep(.el-scrollbar__wrap) { overflow-x: hidden; } }
  .custom-menu {
    list-style: none; padding: 12px; margin: 0;
    .menu-item {
      padding: 0 16px; height: 48px; border-radius: 10px;
      display: flex; justify-content: space-between; align-items: center;
      cursor: pointer; transition: all 0.3s ease; color: #50596c; font-size: 14px; margin-bottom: 2px;
      position: relative;
      overflow: hidden;
      .label-group {
        display: flex; align-items: center; gap: 14px;
        .icon-wrap {
          width: 24px; height: 24px; background: #f0f2f5; border-radius: 6px;
          display: flex; justify-content: center; align-items: center; transition: all 0.3s;
          .cat-icon { font-size: 14px; color: #909399; }
        }
        .text { font-weight: 500; position: relative; z-index: 1; }
      }
      .arrow { opacity: 0; font-size: 12px; transition: all 0.3s; transform: translateX(-5px); }
      &::before {
        content: ''; position: absolute; left: -100%; top: 0; width: 100%; height: 100%;
        background: $gradient-primary; opacity: 0; transition: all 0.4s ease;
      }
      &:hover {
        background: #fff; box-shadow: 0 4px 12px rgba(0,0,0,0.1); color: $primary;
        .icon-wrap { background: $primary; color: #fff; transform: scale(1.1); }
        .arrow { opacity: 1; transform: translateX(0); color: $primary; }
        .text { transform: translateX(5px); transition: transform 0.3s ease; }
      }
    }
  }
}

/* 轮播 */
.banner-box {
  height: 100%; border-radius: 16px; overflow: hidden;
  box-shadow: 0 12px 32px rgba(0,0,0,0.08); background-color: #eef2f6;
  position: relative;
  .banner-img { width: 100%; height: 100%; object-fit: cover; display: block; transition: transform 8s cubic-bezier(0.2, 1, 0.3, 1); }
  &:hover .banner-img { transform: scale(1.05); }
  .banner-overlay {
    position: absolute; bottom: 0; left: 0; right: 0; height: 100px;
    background: linear-gradient(transparent, rgba(0,0,0,0.7));
    display: flex; align-items: flex-end; padding: 24px;
  }
  .banner-content {
    .banner-title { color: #fff; font-size: 24px; font-weight: 800; margin: 0 0 8px; text-shadow: 0 2px 4px rgba(0,0,0,0.5); animation: fadeInUp 0.6s ease; }
    .banner-desc { color: rgba(255,255,255,0.9); font-size: 14px; margin: 0; text-shadow: 0 2px 4px rgba(0,0,0,0.5); animation: fadeInUp 0.8s ease; }
  }
}
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

/* 创意区 */
.promo-dashboard {
  height: 100%; display: flex; flex-direction: column; gap: 12px;
  .creative-card {
    flex: 1; border-radius: 16px; position: relative; overflow: hidden;
    cursor: pointer; transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    &:hover {
      transform: translateY(-4px); box-shadow: 0 12px 32px rgba(0,0,0,0.15);
      .floating-img { transform: scale(1.1) rotate(-5deg) !important; }
    }
    .glass-content { position: absolute; inset: 0; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; z-index: 2; }
    .text-zone { display: flex; flex-direction: column; justify-content: center; z-index: 3;
      .brand-badge { font-size: 10px; font-weight: 800; letter-spacing: 1px; margin-bottom: 6px; display: flex; align-items: center; gap: 5px; text-transform: uppercase; }
      .title-main { font-size: 20px; margin: 0; font-weight: 900; line-height: 1.1; font-style: italic; }
      .title-sub { font-size: 12px; margin: 4px 0 12px; opacity: 0.8; font-weight: 400; }
    }
    .visual-zone { position: relative; width: 90px; height: 90px;
      .floating-img { width: 100px; height: 100px; object-fit: contain; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); filter: drop-shadow(0 8px 16px rgba(0,0,0,0.3)); transition: transform 0.4s ease; }
    }
  }
  .card-luxury { background: #1c1c1c; color: #f0f0f0; border: 1px solid #333;
    .luxury-bg { position: absolute; inset: 0; background: linear-gradient(135deg, #2b2b2b 0%, #141414 100%); &::before { content: ''; position: absolute; top: -50%; right: -50%; width: 100%; height: 100%; background: radial-gradient(circle, rgba(212, 175, 55, 0.15) 0%, transparent 70%); } }
    .text-zone { .brand-badge.gold { color: #d4af37; .line { width: 12px; height: 1px; background: #d4af37; } } .title-main span { color: #d4af37; font-family: 'Times New Roman', serif; } .luxury-btn { background: transparent; border: 1px solid rgba(212, 175, 55, 0.5); color: #d4af37; &:hover { background: #d4af37; color: #000; } } }
    .price-tag-minimal { position: absolute; bottom: 0; right: 0; font-family: 'Times New Roman', serif; color: #fff; font-size: 14px; letter-spacing: 1px; }
  }
  .card-daily { background: #f0f9ff; color: #333;
    .daily-bg { position: absolute; inset: 0; background: linear-gradient(120deg, #e0f7fa 0%, #ffffff 60%, #e3f2fd 100%); }
    .text-zone { .brand-badge.cyan { color: #00bfa5; background: rgba(0, 191, 165, 0.1); padding: 2px 6px; border-radius: 4px; width: fit-content; } .title-main span { color: #00bfa5; } .daily-btn { background: #00bfa5; border: none; color: #fff; &:hover { background: #009688; } } }
  }
  .card-tech { background: #0f0c29; color: #fff;
    .cyber-bg { position: absolute; inset: 0; background: linear-gradient(to right, #24243e, #302b63, #0f0c29); }
    .text-zone { .brand-badge.neon { color: #ff0080; .dot { width: 6px; height: 6px; background: #ff0080; border-radius: 50%; box-shadow: 0 0 8px #ff0080; } } .title-main span { background: linear-gradient(to right, #00dbde, #fc00ff); -webkit-background-clip: text; color: transparent; } .tech-btn { background: linear-gradient(90deg, #fc00ff, #00dbde); border: none; color: #fff; } }
    .visual-zone { .glitch-circle { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 70px; height: 70px; border: 2px solid rgba(255,255,255,0.1); border-radius: 50%; } .discount-tag { position: absolute; top: 0; right: -10px; background: #ff0080; color: #fff; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-weight: bold; transform: rotate(10deg); } }
  }
}

/* 筛选栏 */
.filter-bar-wrapper {
  border-radius: 12px; padding: 16px 24px; margin-bottom: 24px;
  display: flex; justify-content: space-between; align-items: center;
  transition: all 0.3s ease;
  &:hover { box-shadow: 0 10px 30px rgba(0,0,0,0.06); }
  .filter-left {
    display: flex; align-items: center; gap: 16px;
    .main-label { font-weight: 700; color: #333; font-size: 14px; }
    .filter-pill { 
      padding: 6px 14px; background: #fff; border-radius: 16px; font-size: 13px; color: #666; cursor: pointer; 
      display: flex; align-items: center; gap: 4px; transition: all 0.2s; border: 1px solid #eee; 
      &:hover { border-color: $primary; color: $primary; background: rgba(64, 158, 255, 0.05); } 
    }
    .brand-list { 
      display: flex; gap: 12px; 
      .brand-item { 
        font-size: 13px; color: #666; cursor: pointer; transition: all 0.2s; position: relative;
        &:hover, &.active { color: $primary; font-weight: 600; } 
        &.active::after { content: ''; position: absolute; bottom: -4px; left: 50%; transform: translateX(-50%); width: 12px; height: 2px; background: $primary; border-radius: 1px; }
      } 
    }
  }
  .filter-right { 
    display: flex; gap: 24px; 
    .sort-tab { 
      font-size: 14px; color: #666; cursor: pointer; display: flex; align-items: center; gap: 2px; transition: all 0.2s; padding: 4px 8px; border-radius: 4px;
      &:hover, &.active { color: $primary; font-weight: 600; background: rgba(64, 158, 255, 0.05); } 
    } 
  }
  @media (max-width: 1024px) {
    flex-wrap: wrap; gap: 12px;
    .filter-left { width: 100%; .brand-list { flex-wrap: wrap; } }
    .filter-right { width: 100%; justify-content: flex-start; }
  }
}
.price-popover { padding: 8px; .popover-header { font-size: 12px; color: #999; margin-bottom: 12px; } .price-slider { margin-bottom: 16px; } .price-inputs { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; .separator { color: #999; } } .popover-footer { display: flex; justify-content: flex-end; } }

/* 商品卡片 */
.product-card {
  border: none; border-radius: 12px; margin-bottom: 20px; transition: all 0.3s; overflow: hidden; cursor: pointer;
  &:hover { transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.08); .add-cart-overlay { transform: translateY(0); } }
  .image-container { 
    position: relative; height: 200px; background: #f8f8f8; overflow: hidden; 
    .prod-img { width: 100%; height: 100%; transition: transform 0.5s ease; } 
    .action-overlay { 
      position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(transparent, rgba(0,0,0,0.7)); 
      padding: 15px 10px; transform: translateY(100%); transition: transform 0.3s ease; 
      display: flex; justify-content: space-around; gap: 8px; 
    } 
    .action-btn { 
      flex: 1; height: 36px; border-radius: 18px; display: flex; justify-content: center; align-items: center; 
      font-size: 12px; font-weight: 500; transition: all 0.2s ease; cursor: pointer; 
      .btn-text { margin-left: 4px; } 
    } 
    .cart-btn { background: rgba(255,255,255,0.95); color: #409eff; border: 1px solid #409eff; &:hover { background: #409eff; color: white; } } 
    .favorite-btn { background: rgba(255,255,255,0.95); color: #666; border: 1px solid #ddd; &:hover { border-color: #ff4d4f; color: #ff4d4f; } .star-icon.active { color: #ff4d4f; } } 
  }
  &:hover { 
    transform: translateY(-4px); box-shadow: 0 12px 24px rgba(0,0,0,0.08); 
    .image-container { .prod-img { transform: scale(1.05); } .action-overlay { transform: translateY(0); } } 
  }
  .card-details { padding: 14px; .title { font-size: 13px; color: #333; margin: 0 0 8px; line-height: 20px; height: 40px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; } .meta-row { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 8px; .price-group { color: #ff4d4f; font-weight: 700; .symbol { font-size: 12px; margin-right: 1px; } .amount { font-size: 18px; } } .sales { font-size: 12px; color: #999; } } .tag-row { display: flex; gap: 6px; height: 18px; .tag-text { font-size: 10px; padding: 0 5px; border-radius: 4px; border: 1px solid; &.blue { color: $primary; border-color: rgba(64,158,255,0.2); background: rgba(64,158,255,0.05); } &.red { color: #f56c6c; border-color: rgba(245,108,108,0.2); background: rgba(245,108,108,0.05); } } } }
}

/* 分页器 */
.pagination-container { display: flex; justify-content: center; margin-top: 40px; padding-bottom: 60px; }

/* 移动端适配 */
@media (max-width: 768px) {
  .container { padding: 0 10px; padding-top: 40px !important; }
  .hero-section-wrapper { margin-top: 15px !important; }
  .hero-col { height: auto !important; margin-bottom: 10px; }
  .filter-bar-wrapper { flex-direction: column; align-items: flex-start; gap: 15px; }
  .promo-dashboard { display: none; }
}
</style>