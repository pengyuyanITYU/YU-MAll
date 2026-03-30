<template>
  <header class="header-bar">
    <div class="container header-content">
      <!-- Logo -->
      <div class="logo" @click="router.push('/')">
        <span class="logo-icon">🛍️</span>
        <span class="logo-text">YU-Mall</span>
      </div>

      <!-- 搜索栏 -->
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

      <!-- 用户操作区 -->
      <div class="user-actions">
        <!-- 未登录状态 -->
        <template v-if="!userInfo.isLoggedIn">
          <el-button text @click="login">登录</el-button>
          <el-button type="primary" round @click="register">注册</el-button>
        </template>

        <!-- 已登录状态 -->
        <template v-else>
          <!-- 1. 我的购物车 -->
          <div class="action-item header-link-wrapper" @click="router.push('/cart')">
            <el-badge :value="cartCount" :max="99" :hidden="cartCount === 0" class="custom-badge">
              <span class="header-link">
                <el-icon class="link-icon"><ShoppingCart /></el-icon>
                我的购物车
              </span>
            </el-badge>
          </div>

          <!-- 2. ★★★ 新增：我的收藏 ★★★ -->
          <div class="action-item header-link-wrapper" @click="router.push({ path: '/user', query: { tab: 'favorites' } })">
            <span class="header-link">
              <el-icon class="link-icon"><Star /></el-icon>
              我的收藏
            </span>
          </div>

          <!-- 3. 我的订单 -->
          <el-dropdown class="nav-dropdown header-link-wrapper" @command="handleCommand">
            <span class="header-link">
              我的订单
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="orders">全部订单</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 4. 头像/个人中心 -->
          <el-dropdown class="nav-dropdown header-link-wrapper" @command="handleCommand">
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
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Search, ShoppingCart, ArrowDown, User, SwitchButton, Star // ★★★ 引入 Star 图标
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
const cartCount = ref(0)
const { userInfo } = storeToRefs(userStore)

// --- 购物车数量获取 ---
const fetchCartCount = async () => {
  if (!userInfo.value.isLoggedIn) {
    cartCount.value = 0;
    return;
  }
  try {
    const res: any = await queryMyCarts();
    if (res && res.data) {
      cartCount.value = res.data.length;
    } else {
      cartCount.value = 0;
    }
  } catch (error) {
    console.error("获取购物车数量失败", error);
  }
}

// --- 命令处理 ---
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

// --- 监听与生命周期 ---
watch(() => route.fullPath, () => {
  if (route.path === '/search') {
    keyword.value = (route.query.q as string) || ''
  } else {
    keyword.value = ''
  }
  if (userInfo.value.isLoggedIn) {
    fetchCartCount()
  }
}, { immediate: true })

onMounted(() => {
  fetchCartCount()
})

watch(() => userInfo.value.isLoggedIn, (newValue) => {
  if (newValue) {
    fetchCartCount();
  } else {
    cartCount.value = 0;
  }
});

</script>

<style scoped lang="scss">
$primary: #409eff;
$text-main: #303133;

.header-bar {
  position: fixed;
  top: 0; left: 0; right: 0; z-index: 1000;
  height: 64px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  box-shadow: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  display: flex; align-items: center;
  transition: all 0.3s ease;
}

.container {
  max-width: 1280px; margin: 0 auto; padding: 0 16px; width: 100%;
}

.header-content {
  display: flex; align-items: center; justify-content: space-between;
}

.logo {
  display: flex; align-items: center; cursor: pointer;
  .logo-icon { margin-right: 8px; font-size: 26px; }
  .logo-text {
    font-size: 22px; font-weight: 900;
    background: linear-gradient(45deg, #409eff, #36cfc9);
    background-clip: text; -webkit-background-clip: text; color: transparent;
    letter-spacing: -0.5px;
  }
}

.search-area {
  width: 450px;
  :deep(.el-input-group__append) {
    background-color: $primary; border-color: $primary; color: #fff;
    border-radius: 0 20px 20px 0; padding: 0 20px; transition: opacity 0.3s;
    &:hover { opacity: 0.9; }
  }
  :deep(.el-input__wrapper) {
    border-radius: 20px 0 0 20px; box-shadow: none;
    background-color: rgba(255, 255, 255, 0.8);
    border: 1px solid transparent; transition: all 0.3s;
    &:hover, &.is-focus { background-color: #fff; box-shadow: 0 0 0 1px $primary inset; }
  }
}

.user-actions {
  display: flex; align-items: center; gap: 24px;

  /* 通用头部链接样式 */
  .header-link-wrapper {
    display: flex; align-items: center; cursor: pointer;
  }
  .header-link {
    display: flex; align-items: center; /* 确保图标和文字对齐 */
    font-size: 14px; color: #333; font-weight: 500;
    transition: color 0.3s;
    &:hover { color: $primary; }
    .link-icon { margin-right: 4px; font-size: 16px; }
  }

  /* 购物车角标样式 */
  .custom-badge {
    :deep(.el-badge__content) {
      border: none;
      transform: translateY(-2px) translateX(100%);
      background-color: #f56c6c;
    }
  }

  .nav-dropdown {
    display: flex; align-items: center;
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