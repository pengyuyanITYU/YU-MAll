<template>
  <!-- 模板部分保持不变，省略以节省篇幅，重点看 Script -->
  <div class="search-page">
    <div class="container">
      
      <!-- 1. 头部：面包屑与结果统计 -->
      <div class="search-header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>全部分类</el-breadcrumb-item>
          <el-breadcrumb-item>搜索结果</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="result-stat">
          搜索 <span class="keyword">"{{ route.query.q || '全部商品' }}"</span>，
          共找到 <span class="count">{{ total }}</span> 件相关商品
        </div>
      </div>

      <!-- 2. 筛选工具栏 -->
      <div class="filter-toolbar">
        <div class="sort-area">
          <span class="label">排序：</span>
          <div class="sort-group">
            <span 
              class="sort-item" 
              :class="{ active: sortType === 'default' }"
              @click="handleSort('default')"
            >综合</span>
            <span 
              class="sort-item" 
              :class="{ active: sortType === 'sales' }"
              @click="handleSort('sales')"
            >销量</span>
            <span 
              class="sort-item" 
              :class="{ active: sortType.includes('price') }"
              @click="handleSortPrice"
            >
              价格 
              <div class="sort-arrows">
                <el-icon :class="{ active: sortType === 'price_asc' }"><CaretTop /></el-icon>
                <el-icon :class="{ active: sortType === 'price_desc' }"><CaretBottom /></el-icon>
              </div>
            </span>
            <span 
              class="sort-item" 
              :class="{ active: sortType === 'new' }"
              @click="handleSort('new')"
            >新品</span>
          </div>
        </div>

        <div class="price-filter">
          <el-input 
            v-model="minPrice" 
            placeholder="¥ 最低价" 
            size="small" 
            class="price-input" 
          />
          <span class="sep">-</span>
          <el-input 
            v-model="maxPrice" 
            placeholder="¥ 最高价" 
            size="small" 
            class="price-input" 
          />
          <el-button 
            type="primary" 
            size="small" 
            class="filter-btn" 
            plain
            @click="search"
          >确定</el-button>
        </div>
        
        <div class="extra-options">
          <el-checkbox v-model="filterFlags.freeShipping" label="包邮" size="small" border />
          <el-checkbox v-model="filterFlags.hasStock" label="仅显示有货" size="small" border />
        </div>
      </div>

      <!-- 3. 商品列表区域 -->
      <div class="product-list-area" v-loading="loading" element-loading-text="正在搜索好物...">
        
        <div v-if="productList.length > 0">
          <el-row :gutter="16">
            <el-col
              v-for="item in productList"
              :key="item.id"
              :xs="12" :sm="8" :md="6" :lg="4" :xl="4"
            >
              <el-card 
                class="product-card" 
                :body-style="{ padding: '0px' }" 
                shadow="hover"
                @click="goToDetail(item.id)"
              >
                <div class="img-box">
                  <el-image :src="item.image" fit="cover" loading="lazy" class="prod-img" />
                </div>
                <div class="info-box">
                  <div class="price-row">
                    <span class="currency">¥</span>
                    <span class="amount">{{ formatPrice(item.price) }}</span>
                    <span class="sales">{{ item.sales }}人付款</span>
                  </div>
                  <div class="title" :title="item.title">
                    <span v-html="highlightKeyword(item.title)"></span>
                  </div>
                  <div class="shop-row">
                    <span class="shop-name">{{ item.shopName }}</span>
                    <el-icon><ArrowRight /></el-icon>
                  </div>
                  <div class="tags-row">
                    <el-tag v-if="item.isSelf" type="danger" size="small" effect="plain" class="mini-tag">自营</el-tag>
                    <el-tag v-if="item.coupon" type="warning" size="small" effect="plain" class="mini-tag">券</el-tag>
                  </div>
                </div>
                <!-- 悬浮操作按钮 -->
                <div class="hover-actions">
                  <div class="btn-wrap" title="加入购物车" @click.stop="addToCart(item)">
                    <el-icon><ShoppingCart /></el-icon> 
                  </div>
                  <!-- 注意这里绑定了 active class 和点击事件 -->
                  <div class="btn-wrap" :title="item.isFavorite ? '取消收藏' : '收藏'" @click.stop="toggleFavorite(item)">
                    <el-icon :class="{ active: item.isFavorite }"><Star /></el-icon> 
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="pageQuery.pageNo"
              v-model:page-size="pageQuery.pageSize"
              :page-sizes="[5,10, 40, 80]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              background
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <el-empty 
          v-else 
          description="未能找到相关商品，换个词试试吧~" 
          :image-size="200"
        >
          <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
        </el-empty>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CaretTop, CaretBottom, ArrowRight, ShoppingCart, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 引入 API
import { list } from '@/api/item'
import { addItem2Cart } from '@/api/cart'
import { addCollect, deleteById, type CollectItem } from '@/api/collect' // 假设路径


// --- 金额格式化工具 ---
const formatPrice = (price?: number | string | null) => {
  if (price === undefined || price === null || price === '') return '0.00'
  const str = price.toString()
  const padded = str.padStart(3, '0') 
  return `${padded.slice(0, -2)}.${padded.slice(-2)}`
}

interface FilterFlags {
  freeShipping: boolean
  hasStock: boolean
}

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const productList = ref<any[]>([])
const total = ref(0)

const pageQuery = reactive({
  pageNo: 1,
  pageSize: 20
})

const sortType = ref('default')
const minPrice = ref('') 
const maxPrice = ref('')
const filterFlags = reactive<FilterFlags>({
  freeShipping: false,
  hasStock: false
})

// --- 搜索逻辑 ---
const search = async () => {
  loading.value = true
  let minPriceFen = undefined
  let maxPriceFen = undefined

  if (minPrice.value) {
    minPriceFen = Math.round(Number(minPrice.value) * 100)
  }
  if (maxPrice.value) {
    maxPriceFen = Math.round(Number(maxPrice.value) * 100)
  }

  const queryParams = {
    pageNo: pageQuery.pageNo,
    pageSize: pageQuery.pageSize,
    name: (route.query.q as string) || '',
    sort: sortType.value,
    minPrice: minPriceFen, 
    maxPrice: maxPriceFen, 
    hasStock: filterFlags.hasStock,
    freeShipping: filterFlags.freeShipping
  }

  try {
    const res: any = await list(queryParams);
    if (res && res.code === 200) {
      // 假设后端返回的数据包含 isFavorite (bool) 和 collectId (number)
      // 如果后端没有返回这些字段，前端默认显示未收藏
      productList.value = res.rows || [];
      total.value = res.total || 0;
    } else {
      productList.value = [];
      total.value = 0;
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('网络请求失败');
  } finally {
    loading.value = false;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}

// --- 核心功能：收藏与取消收藏 ---
const toggleFavorite = async (item: any) => {
  // 1. 校验登录
  const token = localStorage.getItem('Authorization');
  if (!token) {
    ElMessage.warning('请先登录后操作');
    router.push('/login');
    return;
  }

  try {
    if (item.isFavorite) {
      // --- 取消收藏逻辑 ---
      
      // 注意：deleteById 通常需要“收藏记录的ID”(collectId)，而不是“商品ID”(itemId)
      // 如果后端 list 接口返回了 collectId，请使用 item.collectId
      // 如果后端 deleteById 接口设计特殊，支持传 itemId，则传 item.id
      
      const idToDelete = item.collectId || item.id; 
      
      if (!idToDelete) {
        ElMessage.error('无法获取收藏ID');
        return;
      }

      await deleteById(idToDelete);
      
      // 更新 UI 状态
      item.isFavorite = false;
      item.collectId = undefined; // 清理掉 ID
      ElMessage.success('已取消收藏');

    } else {
      // --- 添加收藏逻辑 ---
      
      // 构造符合接口定义的 CollectItem 数据
      const collectData: CollectItem = {
        itemId: item.id,
        name: item.name,
        image: item.image,
        price: item.price, // 透传分，API接口定义允许 number | string
        tags: item.tags // 简单标记来源，可视情况修改
      };

      const res: any = await addCollect(collectData);
      
      // 更新 UI 状态
      item.isFavorite = true;
      
      // 如果后端添加成功后返回了新生成的 ID (例如 res.data)，建议赋值给 item.collectId
      // 这样用户立即点击取消时，能拿到正确的 ID
      if (res && res.data) {
         // 假设 res.data 是新的收藏 ID (Long类型)
         item.collectId = res.data;
      }
      
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    console.error('收藏操作失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  }
}

// --- 其他事件处理 ---
const handleSort = (type: string) => {
  if (sortType.value === type) return
  sortType.value = type
  pageQuery.pageNo = 1
  search()
}

const handleSortPrice = () => {
  sortType.value = sortType.value === 'price_asc' ? 'price_desc' : 'price_asc'
  pageQuery.pageNo = 1
  search()
}

const handleSizeChange = (val: number) => {
  pageQuery.pageSize = val
  pageQuery.pageNo = 1
  search()
}

const handlePageChange = (val: number) => {
  pageQuery.pageNo = val
  search()
}

const highlightKeyword = (title: string | undefined) => {
  if (!title) return '';
  const keyword = route.query.q as string | undefined;
  if (!keyword) return title;
  const reg = new RegExp(`(${keyword})`, 'gi');
  return title.replace(reg, `<span style="color: #f56c6c; font-weight:bold">$1</span>`);
}

const goToDetail = (id: number) => {
  router.push(`/item/${id}`)
}

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
      name: item.title,
      image: item.image,
      skuId: item.defaultSkuId || 0, 
      spec: "{}" 
    };
    await addItem2Cart(params);
    ElMessage.success(`已添加 ${item.title} 到购物车`);
  } catch (error) {
    console.error(error);
    ElMessage.error('加入购物车失败');
  }
}

watch(() => route.query.q, () => {
  pageQuery.pageNo = 1
  minPrice.value = ''
  maxPrice.value = ''
  sortType.value = 'default'
  search()
})

onMounted(() => {
  search()
})
</script>

<style scoped lang="scss">
$primary: #409eff;
$text-main: #303133;
$text-sub: #909399;
$border: #ebeef5;
$bg-gray: #f5f7fa;

.search-page {
  background: $bg-gray;
  min-height: 100vh;
  padding-bottom: 40px;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 样式保持不变，确保 .active 样式存在即可 */
.search-header {
  padding: 20px 0;
  .result-stat {
    margin-top: 10px;
    font-size: 14px;
    color: $text-sub;
    .keyword { color: $primary; font-weight: bold; margin: 0 4px; }
    .count { color: $text-main; font-weight: bold; margin: 0 4px; }
  }
}

/* 2. 筛选工具栏 */
.filter-toolbar {
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);

  .sort-area {
    display: flex;
    align-items: center;
    .label { font-size: 14px; color: $text-sub; margin-right: 10px; }
    .sort-group {
      display: flex;
      .sort-item {
        padding: 0 16px;
        height: 28px;
        line-height: 28px;
        font-size: 14px;
        color: $text-main;
        cursor: pointer;
        border: 1px solid transparent;
        border-radius: 4px;
        display: flex;
        align-items: center;
        transition: all 0.2s;
        
        &:hover { color: $primary; }
        &.active { 
          color: #fff; 
          background: $primary; 
          border-color: $primary;
        }

        .sort-arrows {
          display: flex;
          flex-direction: column;
          margin-left: 4px;
          height: 12px;
          justify-content: center;
          
          .el-icon {
            font-size: 12px;
            height: 6px;
            color: #dcdfe6; 
            &.active { color: #fff; } 
          }
        }
      }
    }
  }

  .price-filter {
    display: flex;
    align-items: center;
    .price-input { width: 70px; }
    .sep { margin: 0 6px; color: $text-sub; }
    .filter-btn { margin-left: 10px; }
  }
}

/* 3. 商品网格 */
.product-list-area {
  min-height: 400px;
}

.product-card {
  border: none;
  margin-bottom: 16px;
  border-radius: 8px;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0,0,0,0.08);
    .hover-actions { opacity: 1; pointer-events: auto; }
  }

  .img-box {
    height: 200px;
    overflow: hidden;
    position: relative;
    .prod-img { width: 100%; height: 100%; transition: transform 0.5s; }
  }
  
  &:hover .prod-img { transform: scale(1.05); }

  .info-box {
    padding: 10px;
    
    .price-row {
      color: #f56c6c;
      font-weight: 700;
      line-height: 1.2;
      margin-bottom: 6px;
      display: flex;
      align-items: baseline;
      .currency { font-size: 12px; }
      .amount { font-size: 20px; margin-right: 8px; }
      .sales { font-size: 12px; color: $text-sub; font-weight: normal; }
    }

    .title {
      font-size: 13px;
      color: $text-main;
      line-height: 20px;
      height: 40px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      margin-bottom: 6px;
    }

    .shop-row {
      display: flex; align-items: center; color: $text-sub; font-size: 12px; margin-bottom: 6px;
      .shop-name { margin-right: 4px; max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
      .el-icon { font-size: 12px; }
      &:hover { color: $primary; }
    }

    .tags-row {
      height: 20px;
      display: flex; gap: 4px;
      .mini-tag { height: 18px; padding: 0 4px; font-size: 12px; line-height: 16px; border-radius: 2px; }
    }
  }

  .hover-actions {
    position: absolute;
    bottom: 90px;
    right: 10px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    opacity: 0;
    pointer-events: none;
    transition: opacity 0.2s;

    .btn-wrap {
      width: 32px; height: 32px;
      border-radius: 50%;
      background: rgba(255,255,255,0.9);
      display: flex; justify-content: center; align-items: center;
      color: $text-main;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      transition: all 0.2s;
      cursor: pointer;
      
      &:hover { background: $primary; color: #fff; }
      
      /* 添加星星激活状态的样式 */
      .el-icon.active {
        color: #e6a23c; /* 亮黄色 */
        fill: currentColor;
      }
    }
  }
}

.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  padding: 20px 0;
  background: #fff;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .filter-toolbar { gap: 10px; }
  .price-filter, .extra-options { display: none; }
  .product-card .img-box { height: 160px; }
}
</style>