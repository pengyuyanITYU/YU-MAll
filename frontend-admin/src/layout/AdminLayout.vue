<template>
  <div class="admin-shell">
    <el-container class="admin-container">
      <el-aside class="admin-aside" width="220px">
        <div class="brand" @click="router.push('/dashboard')">
          <span class="brand-mark">
            <el-icon><ShoppingCart /></el-icon>
          </span>
          <div class="brand-copy">
            <div class="brand-title">YU-Mall</div>
          </div>
        </div>

        <div class="menu-groups">
          <el-menu
            :default-active="route.path"
            :default-openeds="['goods', 'orders', 'users', 'security']"
            class="menu"
            router
            unique-opened
            background-color="transparent"
            text-color="#9ca8ba"
            active-text-color="#ffffff"
          >
            <el-menu-item index="/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>控制台</span>
            </el-menu-item>

            <el-sub-menu index="goods">
              <template #title>
                <el-icon><Goods /></el-icon>
                <span>商品管理</span>
              </template>
              <el-menu-item index="/items">商品列表</el-menu-item>
              <el-menu-item index="/shops">店铺管理</el-menu-item>
              <el-menu-item index="/categories">分类管理</el-menu-item>
              <el-menu-item index="/brands">品牌管理</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="orders">
              <template #title>
                <el-icon><Tickets /></el-icon>
                <span>订单管理</span>
              </template>
              <el-menu-item index="/orders">订单列表</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="users">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>用户管理</span>
              </template>
              <el-menu-item index="/users">用户列表</el-menu-item>
            </el-sub-menu>

            <el-sub-menu index="security">
              <template #title>
                <el-icon><Lock /></el-icon>
                <span>权限管理</span>
              </template>
              <el-menu-item index="/comments">评论审核</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
      </el-aside>

      <el-container class="content-shell">
        <el-header class="admin-header">
          <div class="shell-breadcrumb">
            <button class="shell-toggle" type="button" aria-label="menu">
              <el-icon><Operation /></el-icon>
            </button>
            <div class="breadcrumb-text">
              <span class="breadcrumb-root">首页</span>
              <span class="breadcrumb-divider">/</span>
              <span class="breadcrumb-current">{{ pageTitle }}</span>
            </div>
          </div>

          <div class="shell-profile">
            <el-avatar :size="42" :src="avatarUrl">{{ profileInitial }}</el-avatar>
            <div class="profile-copy">
              <div class="profile-name">{{ authStore.nickName || '管理员' }}</div>
              <div class="profile-role">行政人员</div>
            </div>
            <button class="logout-trigger" type="button" aria-label="logout" @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </button>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Goods,
  Lock,
  Odometer,
  Operation,
  ShoppingCart,
  SwitchButton,
  Tickets,
  UserFilled
} from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const pageTitle = computed(() => (route.meta.title as string) || '控制台');

const avatarUrl = computed(() => authStore.avatar || undefined);

const profileInitial = computed(() => {
  const text = (authStore.nickName || '鱼').trim();
  return text.slice(0, 1).toUpperCase();
});

function handleLogout() {
  authStore.logout();
  router.replace('/login');
}
</script>

<style scoped lang="scss">
.admin-shell {
  min-height: 100vh;
}

.admin-container {
  min-height: 100vh;
  background: transparent;
}

.admin-aside {
  background: linear-gradient(180deg, #2d3745 0%, #303948 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.02);
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 22px 14px;
  cursor: pointer;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  background: linear-gradient(135deg, #7657ff, #6c63ff);
  box-shadow: 0 8px 18px rgba(108, 99, 255, 0.35);
  font-size: 18px;
}

.brand-copy {
  min-width: 0;
}

.brand-title {
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.menu-groups {
  flex: 1;
  padding: 10px 0 18px;
}

.menu {
  border-right: none;
}

.menu :deep(.el-menu-item),
.menu :deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 6px 0;
  padding-left: 24px !important;
  color: #97a3b8;
  border-radius: 0;
  transition: all 0.2s ease;
}

.menu :deep(.el-menu-item:hover),
.menu :deep(.el-sub-menu__title:hover) {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.05);
}

.menu :deep(.el-menu-item.is-active) {
  color: #ffffff;
  background: linear-gradient(90deg, #5f4af5 0%, #4f46e5 100%);
  box-shadow: inset 3px 0 0 rgba(255, 255, 255, 0.8);
}

.menu :deep(.el-sub-menu .el-menu-item) {
  margin: 2px 0 2px 14px;
  padding-left: 52px !important;
  font-size: 13px;
}

.menu :deep(.el-sub-menu__icon-arrow) {
  margin-top: -4px;
  color: #7f8a9b;
}

.menu :deep(.el-icon) {
  margin-right: 12px;
  font-size: 16px;
}

.content-shell {
  min-width: 0;
}

.admin-header {
  height: 74px;
  padding: 0 26px 0 22px;
  background: rgba(255, 255, 255, 0.96);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.shell-breadcrumb {
  display: flex;
  align-items: center;
  gap: 14px;
}

.shell-toggle {
  width: 38px;
  height: 38px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #566174;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.breadcrumb-text {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #909aae;
  font-size: 14px;
  font-weight: 600;
}

.breadcrumb-current {
  color: #394150;
}

.shell-profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
  line-height: 1.1;
}

.profile-name {
  font-size: 14px;
  font-weight: 700;
  color: #2f3747;
}

.profile-role {
  font-size: 12px;
  color: #a1a8b6;
}

.logout-trigger {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #7e8798;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-trigger:hover {
  background: #f2f5fb;
  color: #3f4b5c;
}

.admin-main {
  padding: 18px;
  background: transparent;
}

@media (max-width: 960px) {
  .admin-container {
    display: block;
  }

  .admin-aside {
    width: 100% !important;
  }

  .admin-header {
    padding: 0 16px;
  }

  .profile-copy {
    display: none;
  }
}
</style>
