<template>
  <header class="site-header">
    <div class="site-header__shell">
      <div class="site-header__top">
        <div class="top-links">
          <button type="button" class="top-link" @click="router.push('/')">首页</button>
          <button type="button" class="top-link" @click="goFavorites">收藏</button>
          <button type="button" class="top-link" @click="goOrders">订单</button>
          <button type="button" class="top-link" @click="goBrandHall">品牌馆</button>
        </div>

        <div class="top-links top-links--right">
          <span class="top-greeting">{{ topGreeting }}</span>
          <button v-if="!userInfo.isLoggedIn" type="button" class="top-link" @click="goLogin">
            登录
          </button>
          <button v-if="!userInfo.isLoggedIn" type="button" class="top-link" @click="goRegister">
            注册
          </button>
          <button type="button" class="top-link" @click="goDashboard">个人中心</button>
        </div>
      </div>

      <div class="site-header__main">
        <button type="button" class="brand-block" @click="router.push('/')">
          <span class="brand-badge">YU</span>
          <span class="brand-copy">
            <strong>YU-Mall</strong>
            <small>先搜索，再逛会场，再下单</small>
          </span>
        </button>

        <div class="search-stack">
          <div class="header-search">
            <span class="search-leading">
              <el-icon><Search /></el-icon>
            </span>
            <input
              v-model="keyword"
              class="search-input"
              type="text"
              placeholder="搜索商品 / 品牌 / 店铺"
              @keyup.enter="search"
            >
            <button type="button" class="search-submit" @click="search">搜索</button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const keyword = ref('')
const { userInfo } = storeToRefs(userStore)

const topGreeting = computed(() => {
  if (!userInfo.value.isLoggedIn) {
    return '欢迎来到 YU-Mall'
  }
  return `欢迎你，${userInfo.value.nickName || userInfo.value.username || 'YU 会员'}`
})

const search = () => {
  const searchKeyword = keyword.value.trim()
  if (!searchKeyword) {
    return
  }
  router.push({ name: 'Search', query: { q: searchKeyword } })
}

const goLogin = () => {
  router.push('/login')
}

const goRegister = () => {
  router.push('/register')
}

const goFavorites = () => {
  if (!userInfo.value.isLoggedIn) {
    goLogin()
    return
  }
  router.push({ path: '/user', query: { tab: 'favorites' } })
}

const goOrders = () => {
  if (!userInfo.value.isLoggedIn) {
    goLogin()
    return
  }
  router.push({ path: '/user', query: { tab: 'orders' } })
}

const goDashboard = () => {
  if (!userInfo.value.isLoggedIn) {
    goLogin()
    return
  }
  router.push({ path: '/user', query: { tab: 'dashboard' } })
}

const goBrandHall = () => {
  router.push({ name: 'Search', query: { q: '品牌馆' } })
}

watch(
  () => route.fullPath,
  () => {
    keyword.value = route.path === '/search' ? String(route.query.q || '') : ''
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.site-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  padding: 0 16px;
  pointer-events: none;
}

.site-header__shell {
  width: min(1280px, 100%);
  border: 1px solid #cddcf3;
  border-top: none;
  border-radius: 0 0 28px 28px;
  background: rgba(243, 246, 253, 0.98);
  box-shadow: 0 12px 32px rgba(18, 38, 77, 0.08);
  overflow: hidden;
  pointer-events: auto;
}

.site-header__top {
  min-height: 36px;
  padding: 0 24px;
  border-bottom: 1px solid rgba(205, 220, 243, 0.8);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.top-links {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.top-links--right {
  justify-content: flex-end;
}

.top-link {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  color: #5872a7;
  cursor: pointer;
  transition: color 0.2s ease;
}

.top-link:hover,
.top-link:focus-visible {
  color: #2f60d2;
}

.top-greeting {
  font-size: 13px;
  color: #7b90ba;
}

.site-header__main {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  align-items: center;
  gap: 28px;
  padding: 18px 24px 20px;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 14px;
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  text-align: left;
}

.brand-badge {
  width: 46px;
  height: 46px;
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4a7cf0 0%, #2f60d2 100%);
  color: #fff;
  font-weight: 700;
  font-size: 18px;
  box-shadow: 0 10px 20px rgba(47, 96, 210, 0.22);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand-copy strong {
  font-size: 18px;
  font-weight: 700;
  color: #243b63;
  line-height: 1;
}

.brand-copy small {
  font-size: 12px;
  color: #8196bf;
}

.search-stack {
  min-width: 0;
}

.header-search {
  height: 68px;
  border-radius: 999px;
  border: 2px solid rgba(113, 158, 250, 0.65);
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  padding: 0 10px 0 16px;
  gap: 12px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
}

.search-leading {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #eef4ff;
  color: #6f92d8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  min-width: 0;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: #324a74;
}

.search-input::placeholder {
  color: #97a8c9;
}

.search-submit {
  border: none;
  background: linear-gradient(135deg, #4d86f5 0%, #2f60d2 100%);
  color: #fff;
  border-radius: 999px;
  padding: 0 22px;
  height: 40px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: 0 12px 22px rgba(47, 96, 210, 0.24);
}

@media (max-width: 1080px) {
  .site-header__main {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .site-header {
    padding: 0 10px;
  }

  .site-header__top {
    padding: 10px 16px;
    align-items: flex-start;
    flex-direction: column;
  }

  .site-header__main {
    padding: 16px;
    gap: 16px;
  }

  .header-search {
    height: 56px;
  }
}
</style>
