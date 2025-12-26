<template>
  <div class="collect-container" v-loading="loading">
    
    <!-- 顶部操作栏 -->
    <div class="action-bar" v-if="collectList.length > 0">
      <el-checkbox 
        v-model="isAllSelected" 
        :indeterminate="isIndeterminate" 
        @change="handleSelectAllChange"
      >全选</el-checkbox>
      <el-button 
        type="danger" 
        link 
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        删除选中 ({{ selectedIds.length }})
      </el-button>
    </div>

    <!-- 收藏列表 -->
    <div v-if="collectList.length > 0" class="collect-grid">
      <div 
        v-for="item in collectList" 
        :key="item.id" 
        class="collect-item"
        :class="{ selected: selectedIds.includes(item.id) }"
      >
        <!-- 选择框 -->
        <div class="select-layer">
           <el-checkbox :label="item.id" v-model="selectedIds" @change="handleItemSelectChange"><span></span></el-checkbox>
        </div>

        <!-- 商品图片 -->
        <el-image :src="item.image" class="item-img" fit="cover" @click="goDetail(item.itemId)" />

        <!-- 信息区域 -->
        <div class="item-info">
          <div class="item-name" @click="goDetail(item.itemId)">{{ item.name }}</div>
          
          <div class="item-spec" v-if="item.spec">
             <span v-for="(val, key) in item.spec" :key="key">{{ val }} </span>
          </div>

          <div class="price-row">
            <!-- 使用你之前定义的 formatPrice 函数 -->
            <div class="price">¥{{ formatPrice(item.price) }}</div>
            <div class="date">收藏于 {{ formatDate(item.createTime) }}</div>
          </div>

          <!-- 按钮组 -->
          <div class="btn-group">
            <el-button size="small" type="primary" plain round @click="addToCart(item)">加入购物车</el-button>
            <el-button size="small" type="danger" icon="Delete" circle plain @click="handleSingleDelete(item.id)"></el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无收藏商品" :image-size="120">
      <el-button type="primary" round @click="$router.push('/')">去逛逛</el-button>
    </el-empty>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getCollectList, deleteCollect } from '@/api/collect'
// 假设你有加入购物车的API
// import { addCart } from '@/api/cart' 

const router = useRouter()
const loading = ref(false)
const collectList = ref<any[]>([])
const selectedIds = ref<number[]>([])

// 全选状态计算
const isAllSelected = ref(false)
const isIndeterminate = computed(() => {
  return selectedIds.value.length > 0 && selectedIds.value.length < collectList.value.length
})

// 初始化
onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await getCollectList()
    if (res.code === 200) { // 假设后端返回 code 200 为成功
      collectList.value = res.data || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 金额格式化 (复用你之前的逻辑)
const formatPrice = (price: number) => {
  if (!price) return '0.00'
  const str = price.toString()
  const padded = str.padStart(3, '0')
  return `${padded.slice(0, -2)}.${padded.slice(-2)}`
}

// 日期格式化
const formatDate = (time: string) => {
  if (!time) return ''
  return time.split('T')[0] // 简单处理，或者用 dayjs
}

// 全选逻辑
const handleSelectAllChange = (val: boolean) => {
  selectedIds.value = val ? collectList.value.map(item => item.id) : []
}
const handleItemSelectChange = () => {
  const count = selectedIds.value.length
  isAllSelected.value = count === collectList.value.length
}

// 跳转详情
const goDetail = (itemId: number) => {
  router.push(`/product/${itemId}`)
}

// 删除逻辑
const handleSingleDelete = (id: number) => {
  confirmDelete([id])
}

const handleBatchDelete = () => {
  confirmDelete(selectedIds.value)
}

const confirmDelete = (ids: number[]) => {
  ElMessageBox.confirm(`确定要删除选中的 ${ids.length} 个商品吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteCollect(ids)
      ElMessage.success('删除成功')
      selectedIds.value = [] // 清空选中
      fetchData() // 刷新列表
    } catch (e) {
      ElMessage.error('删除失败')
    }
  })
}

// 加入购物车
const addToCart = async (item: any) => {
  try {
    // 调用加入购物车 API
    // await addCart({ itemId: item.itemId, num: 1, spec: item.spec })
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
}
</script>

<style scoped lang="scss">
.collect-container {
  padding: 10px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px 15px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.collect-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.collect-item {
  position: relative;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    transform: translateY(-2px);
  }

  &.selected {
    border-color: #409eff;
    background-color: #f0f9eb;
  }

  .select-layer {
    position: absolute;
    top: 10px;
    right: 10px;
    z-index: 2;
  }

  .item-img {
    width: 100%;
    height: 200px;
    cursor: pointer;
    display: block;
  }

  .item-info {
    padding: 12px;

    .item-name {
      font-size: 14px;
      color: #303133;
      margin-bottom: 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      cursor: pointer;
      &:hover { color: #409eff; }
    }

    .item-spec {
      font-size: 12px;
      color: #909399;
      background: #f4f4f5;
      padding: 2px 6px;
      border-radius: 4px;
      display: inline-block;
      margin-bottom: 8px;
    }

    .price-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 12px;
      
      .price {
        color: #f56c6c;
        font-weight: bold;
        font-size: 16px;
      }
      .date {
        font-size: 12px;
        color: #c0c4cc;
      }
    }

    .btn-group {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>