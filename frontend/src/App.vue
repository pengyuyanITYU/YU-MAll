<template>
  <!-- Element Plus 全局配置 (例如中文包) -->
  <el-config-provider :locale="zhCn">
    <div class="app-wrapper">
      
      <!-- 顶部导航 (根据路由 meta 判断是否隐藏) -->
      <NavBar v-if="!$route.meta.hideLayout" />

      <!-- 路由视图区 -->
      <div class="main-view">
        <router-view v-slot="{ Component }">
          <!-- 添加淡入淡出过渡动画 -->
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>

      <!-- 底部页脚 (通用) -->
      <footer class="footer" v-if="!$route.meta.hideLayout">
        <div class="container">
          <p>&copy; 2025 Mall-V3 E-Commerce System. All rights reserved.</p>
        </div>
      </footer>

    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ElConfigProvider } from 'element-plus'
// 改成从 es/locale 引入，TS 类型支持更好
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import NavBar from '@/components/NavBar.vue' 
// 引入刚才写的组件
</script>

<style>
/* 全局重置样式 */
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  background-color: #f5f7fa;
  color: #303133;
}

/* 布局结构：确保 Footer 在底部 */
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-view {
  flex: 1; /* 撑开中间内容区域 */
  width: 100%;
}

/* 页脚样式 */
.footer {
  background: #2c3e50;
  color: #909399;
  padding: 40px 0;
  text-align: center;
  margin-top: auto;
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>