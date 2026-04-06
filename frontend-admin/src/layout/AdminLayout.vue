<template>
  <div class="admin-shell">
    <el-container class="admin-container">
      <el-aside class="admin-aside" width="220px">
        <div class="brand" @click="router.push('/dashboard')">
          <span class="brand-mark">YU</span>
          <div>
            <div class="brand-title">YU-Mall</div>
            <div class="brand-sub">Admin Console</div>
          </div>
        </div>

        <el-menu
          :default-active="route.path"
          class="menu"
          router
          background-color="transparent"
          text-color="#dbeafe"
          active-text-color="#ffffff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>控制台</span>
          </el-menu-item>
          <el-menu-item index="/users">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/items">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/shops">
            <el-icon><Shop /></el-icon>
            <span>店铺管理</span>
          </el-menu-item>
          <el-menu-item index="/categories">
            <el-icon><Grid /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/comments">
            <el-icon><ChatLineSquare /></el-icon>
            <span>评论审核</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <div class="title">{{ pageTitle }}</div>
          <div class="header-right">
            <span class="nickname">{{ authStore.nickName || '管理员' }}</span>
            <el-button type="primary" plain @click="handleLogout">退出</el-button>
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
import { ChatLineSquare, Goods, Grid, List, Odometer, Shop, UserFilled } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const pageTitle = computed(() => (route.meta.title as string) || '管理端');

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
}

.admin-aside {
  background: linear-gradient(180deg, #0f2a46, #1b4e78 70%, #1f6f8b);
  color: #dbeafe;
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  cursor: pointer;
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, #36cfc9, #409eff);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: #fff;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.brand-sub {
  font-size: 12px;
  opacity: 0.8;
}

.menu {
  border-right: none;
}

.menu :deep(.el-menu-item) {
  margin: 4px 10px;
  border-radius: 10px;
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.22);
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid rgba(15, 42, 70, 0.08);
  padding: 0 24px;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nickname {
  color: #334155;
}

.admin-main {
  padding: 20px;
}

@media (max-width: 900px) {
  .admin-aside {
    width: 72px !important;
  }

  .brand {
    justify-content: center;
    padding: 16px 8px;
  }

  .brand-title,
  .brand-sub,
  .menu :deep(.el-menu-item span) {
    display: none;
  }

  .menu :deep(.el-menu-item) {
    justify-content: center;
    margin: 6px;
  }
}
</style>
