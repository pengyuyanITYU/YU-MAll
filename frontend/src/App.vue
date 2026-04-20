<template>
  <el-config-provider :locale="zhCn">
    <div class="app-wrapper">
      <NavBar v-if="showNavBar" />

      <div class="main-view" :class="{ 'main-view--shell': mainViewHasTopOffset }">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>

      <footer v-if="showFooter" class="site-footer">
        <div class="site-footer__shell">
          <div class="site-footer__services">
            <div v-for="service in footerServices" :key="service.title" class="service-item">
              <span class="service-icon" :class="service.tone">
                <el-icon><component :is="service.icon" /></el-icon>
              </span>
              <div class="service-copy">
                <strong>{{ service.title }}</strong>
                <span>{{ service.desc }}</span>
              </div>
            </div>
          </div>

          <div class="site-footer__divider" />

          <div class="site-footer__info">
            <div class="footer-columns">
              <div v-for="column in footerColumns" :key="column.title" class="footer-column">
                <strong>{{ column.title }}</strong>
                <span v-for="line in column.lines" :key="line">{{ line }}</span>
              </div>
            </div>

            <div class="footer-brand">
              <div class="footer-brand__top">
                <span class="footer-brand__badge">YU</span>
                <strong>YU-Mall</strong>
              </div>
              <span>更快找货，更轻松下单</span>
              <small>© 2026 YU-Mall</small>
            </div>
          </div>
        </div>
      </footer>

      <GlobalAiEntry v-if="showAssistantEntry" />
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElConfigProvider } from 'element-plus'
import { Collection, Goods, Odometer, Service } from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import NavBar from '@/components/NavBar.vue'
import GlobalAiEntry from '@/components/GlobalAiEntry.vue'

const route = useRoute()

const footerServices = [
  { title: '正品保障', desc: '核心商品清晰直达', icon: Collection, tone: 'tone-blue' },
  { title: '极速发货', desc: '筛选后快速进入购买', icon: Odometer, tone: 'tone-green' },
  { title: '售后无忧', desc: '订单与售后入口前置', icon: Service, tone: 'tone-orange' },
  { title: '精选品类', desc: '频道入口更少更明确', icon: Goods, tone: 'tone-purple' }
] as const

const footerColumns = [
  { title: '购物指南', lines: ['分类浏览', '搜索找货'] },
  { title: '订单服务', lines: ['查看订单', '售后入口'] },
  { title: '联系方式', lines: ['在线客服', '服务时间 09:00 - 21:00'] }
] as const

const showFooter = computed(() => !route.meta.hideLayout)
const showNavBar = computed(() => !route.meta.hideLayout && !route.meta.hideNavBar)
const mainViewHasTopOffset = computed(() => showNavBar.value)
const showAssistantEntry = computed(() => showFooter.value && route.path !== '/ai-assistant')
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  background-color: #e7f1fa;
  color: #303133;
}

#app {
  min-height: 100vh;
}

.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-view {
  flex: 1;
  width: 100%;
}

.main-view--shell {
  padding-top: 124px;
}

.site-footer {
  padding: 24px 0 28px;
  background: transparent;
}

.site-footer__shell {
  width: min(1280px, calc(100% - 32px));
  margin: 0 auto;
  padding: 22px 24px;
  border: 1px solid #d9e4f6;
  border-radius: 26px;
  background: #f2f6fc;
  box-shadow: 0 10px 24px rgba(18, 38, 77, 0.06);
}

.site-footer__services {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
  align-items: center;
}

.service-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.service-icon,
.footer-brand__badge {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.service-copy,
.footer-column,
.footer-brand {
  display: flex;
  flex-direction: column;
}

.service-copy strong,
.footer-column strong {
  color: #242e42;
  font-size: 14px;
  font-weight: 700;
}

.service-copy span,
.footer-column span,
.footer-brand span,
.footer-brand small {
  color: #6e7c93;
  font-size: 12px;
  line-height: 1.6;
}

.site-footer__divider {
  height: 1px;
  margin: 20px 0;
  background: #edf2fa;
}

.site-footer__info {
  display: flex;
  justify-content: space-between;
  gap: 24px;
}

.footer-columns {
  display: flex;
  gap: 48px;
}

.footer-column {
  gap: 6px;
}

.footer-brand {
  align-items: flex-end;
  gap: 6px;
}

.footer-brand__top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.footer-brand__top strong {
  color: #11223c;
  font-size: 18px;
  font-weight: 700;
}

.footer-brand__badge {
  background: #edf4ff;
  color: #2f74ff;
  font-size: 14px;
  font-weight: 700;
}

.tone-blue {
  background: #edf4ff;
  color: #2f74ff;
}

.tone-green {
  background: #eef8f2;
  color: #298d61;
}

.tone-orange {
  background: #fff2ed;
  color: #dc552e;
}

.tone-purple {
  background: #f2f1ff;
  color: #6c4ed2;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 1024px) {
  .main-view--shell {
    padding-top: 154px;
  }

  .site-footer__services {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .site-footer__info {
    flex-direction: column;
  }

  .footer-brand {
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .main-view--shell {
    padding-top: 210px;
  }

  .site-footer__shell {
    width: calc(100% - 20px);
    padding: 18px;
  }

  .site-footer__services {
    grid-template-columns: 1fr;
  }

  .footer-columns {
    flex-direction: column;
    gap: 18px;
  }
}
</style>
