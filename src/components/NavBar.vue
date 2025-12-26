<template>
  <header class="header-bar">
    <div class="container header-content">
      <div class="logo" @click="router.push('/')">
        <span class="logo-icon">🛍️</span>
        <span class="logo-text">YU-Mall</span>
      </div>

      <div class="search-area">
        <el-input 
          v-model="keyword" 
          placeholder="搜索好物..." 
          class="search-input"
          @keyup.enter="search"
        >
          <template #append>
            <el-button :icon="Search" @click="search" />
          </template>
        </el-input>
      </div>

      <div class="user-actions">
        <template v-if="!userInfo.isLoggedIn">
          <el-button text @click="login">登录</el-button>
          <el-button type="primary" round @click="register">注册</el-button>
        </template>

        <template v-else>
  <el-dropdown class="nav-dropdown" @command="handleCommand">
    <span class="el-dropdown-link">
      我的订单
      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="orders">全部订单</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
  
  <el-dropdown class="nav-dropdown" @command="handleCommand">
    <div class="avatar-wrapper">
      <el-avatar :size="32" :src="userInfo.avatar" />
      <span class="username">{{ userInfo.nickName }}</span>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="userCenter">
          <el-icon><User /></el-icon> 个人中心
        </el-dropdown-item>
        <el-dropdown-item divided command="logout">
          <el-icon><SwitchButton /></el-icon> 退出登录
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, watch,reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { 
  Search, ShoppingCart, ArrowDown, User, Setting, SwitchButton, Star 
} from '@element-plus/icons-vue' 
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useUserStore }  from '@/stores/useUserStore'
import { queryMyCarts } from '@/api/cart'
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
// --- 状态定义 ---
const keyword = ref('')
// [任务2] 购物车数量不再写死
const cartCount = ref(0)
const { userInfo } = storeToRefs(userStore)

const handleCommand = (command: string) => {
  switch (command) {
    case 'orders':
      router.push({ path: '/user', query: { tab: 'orders' } })
      break
    case 'userCenter':
      router.push({ path: '/user', query: { tab: 'dashboard' } })
      break
    case 'logout':
      handleLogout()
      break
  }
}

// --- 事件处理 ---
const login = (): void => {
  router.push({ name: 'Login' })
}
const fetchCartCount = async () => {
  if (!userInfo.value.isLoggedIn) {
    cartCount.value = 0;
    return;
  }
  try {
    const res: any = await queryMyCarts();
    if (res && res.data) {
      // 假设后端返回的是列表，长度即为商品种数
      cartCount.value = res.data.length;
    }
  } catch (error) {
    console.error("获取购物车数量失败", error);
  }
}
const register = (): void => {
  router.push({ name: 'Register' })
}

const handleLogout = () => {
  userStore.logout()
  cartCount.value = 0
  ElMessage.success('已退出登录')
  router.push('/')
}

const search = () => {
  if (!keyword.value || !keyword.value.trim()) return
  router.push({ name: 'Search', query: { q: keyword.value } })
}

const handleCartDropdownShow = (visible: boolean) => {
  if (visible) fetchCartCount()
}

// 监听路由变化
watch(() => route.fullPath, () => {
  // 1. 同步搜索框
  if (route.path === '/search') {
    keyword.value = (route.query.q as string) || ''
  } else {
    keyword.value = ''
  }
  // 2. 路由变化时尝试更新购物车数量 (比如从详情页加购后跳转)
  fetchCartCount()
}, { immediate: true })


onMounted(() => {
  fetchCartCount()
})
</script>

<style scoped lang="scss">
$primary: #409eff;
$text-main: #303133;

.header-bar {
  /* 🌟 核心修改：固定定位 + 玻璃拟态 */
  position: fixed; 
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 64px;
  
  /* 背景改为半透明白色，配合模糊滤镜 */
  background: rgba(255, 255, 255, 0.7); 
  backdrop-filter: blur(20px); /* 关键：毛玻璃效果 */
  -webkit-backdrop-filter: blur(20px);
  
  /* 去掉底部的阴影和边框，消除割裂感 */
  box-shadow: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  
  /* 可选：页面向下滚动时加深背景（需配合JS，暂且保持常驻玻璃态）*/
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
  width: 100%;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  cursor: pointer;
  .logo-icon { margin-right: 8px; font-size: 26px; }
  .logo-text {
    font-size: 22px;
    font-weight: 900;
    background: linear-gradient(45deg, #409eff, #36cfc9);
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
    letter-spacing: -0.5px;
  }
}

.search-area {
  width: 450px;
  
  :deep(.el-input-group__append) {
    background-color: $primary;
    border-color: $primary;
    color: #fff;
    border-radius: 0 20px 20px 0;
    padding: 0 20px;
    transition: opacity 0.3s;
    &:hover { opacity: 0.9; }
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 20px 0 0 20px;
    box-shadow: none;
    /* 搜索框背景稍微白一点，突出显示 */
    background-color: rgba(255, 255, 255, 0.8); 
    border: 1px solid transparent;
    transition: all 0.3s;
    
    &:hover, &.is-focus {
      background-color: #fff;
      box-shadow: 0 0 0 1px $primary inset;
    }
  }
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 24px;

  .el-dropdown-link {
    cursor: pointer;
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #333; /* 加深字体颜色，防止背景太亮看不清 */
    font-weight: 500;
    transition: color 0.3s;
    &:hover { color: $primary; }
  }

  .icon-btn {
    border: none;
    background: rgba(255,255,255,0.5); /* 半透明按钮背景 */
    color: #333;
    box-shadow: 0 2px 6px rgba(0,0,0,0.05);
    &:hover { background: #fff; color: $primary; }
  }

  .nav-btn {
    &:hover { color: $primary; background: transparent; }
  }

  .avatar-wrapper {
    display: flex; align-items: center; gap: 8px; cursor: pointer;
    .user-avatar { border: 2px solid #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.1); transition: transform 0.3s; }
    &:hover .user-avatar { transform: scale(1.05); }
    .username { font-size: 14px; font-weight: 500; color: $text-main; }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .search-area { display: none; }
  .header-bar { height: 60px; }
  .logo-text { font-size: 18px; }
}
</style>