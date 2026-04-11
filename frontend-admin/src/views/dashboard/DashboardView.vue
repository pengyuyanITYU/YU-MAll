<template>
  <div class="dashboard-shell" v-loading="loading">
    <section class="metrics-grid">
      <article v-for="card in metricCards" :key="card.key" class="metric-card">
        <div class="metric-copy">
          <p class="metric-label">{{ card.label }}</p>
          <div class="metric-value">
            {{ card.value }}
            <span v-if="card.unit" class="metric-unit">{{ card.unit }}</span>
          </div>
          <p class="metric-hint">{{ card.hint }}</p>
        </div>

        <div class="metric-icon" :style="{ background: card.iconBg, color: card.iconColor }">
          <component :is="card.icon" />
        </div>
      </article>
    </section>

    <section class="dashboard-main">
      <article class="panel trend-panel">
        <div class="panel-head">
          <div class="panel-title">
            <span class="panel-bar"></span>
            <span>{{ trendTitle }}</span>
          </div>
          <div class="panel-tabs">
            <span class="panel-tab is-active">本周</span>
            <span class="panel-tab">本月</span>
          </div>
        </div>

        <div class="trend-chart">
          <svg viewBox="0 0 720 320" preserveAspectRatio="none" aria-label="trend chart">
            <defs>
              <linearGradient id="trendFill" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stop-color="rgba(97, 93, 255, 0.24)" />
                <stop offset="100%" stop-color="rgba(97, 93, 255, 0.03)" />
              </linearGradient>
            </defs>

            <g v-for="line in trendGridLines" :key="line.y">
              <line :x1="52" :y1="line.y" :x2="680" :y2="line.y" class="chart-grid-line" />
              <text x="16" :y="line.y + 4" class="chart-grid-text">{{ line.label }}</text>
            </g>

            <path :d="trendAreaPath" class="chart-area" />
            <path :d="trendLinePath" class="chart-line" />

            <g v-for="point in trendPoints" :key="point.key">
              <circle :cx="point.x" :cy="point.y" r="4.5" class="chart-point" />
              <text :x="point.x" y="298" class="chart-axis-text">{{ point.label }}</text>
            </g>
          </svg>
        </div>
      </article>

      <article class="panel channel-panel">
        <div class="panel-head">
          <div class="panel-title">
            <span class="panel-bar"></span>
            <span>支付渠道活跃度</span>
          </div>
        </div>

        <div class="channel-content">
          <div class="channel-ring" :style="{ background: channelGradient }">
            <div class="channel-ring-hole">
              <strong>{{ formatInteger(channelTotal) }}</strong>
              <span>支付笔数</span>
            </div>
          </div>

          <div class="channel-legend">
            <div v-for="item in paymentChannels" :key="item.key" class="channel-legend-item">
              <span class="legend-dot" :style="{ background: item.color }"></span>
              <span class="legend-label">{{ item.label }}</span>
              <span class="legend-value">{{ item.count }}</span>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="dashboard-bottom">
      <article class="panel ranking-panel">
        <div class="panel-head">
          <div class="panel-title">
            <span class="panel-bar"></span>
            <span>商品热销 Top10</span>
          </div>
        </div>

        <div v-if="rankingItems.length" class="ranking-list">
          <div v-for="item in rankingItems" :key="item.name" class="ranking-item">
            <div class="ranking-meta">
              <span class="ranking-name">{{ item.name }}</span>
              <span class="ranking-value">{{ item.displayValue }}</span>
            </div>
            <div class="ranking-track">
              <div class="ranking-bar" :style="{ width: `${item.percent}%` }"></div>
            </div>
          </div>
        </div>
        <div v-else class="panel-empty">暂无销量数据</div>
      </article>

      <article class="panel orders-panel">
        <div class="panel-head">
          <div class="panel-title">
            <span class="panel-bar"></span>
            <span>最新实时订单</span>
          </div>
          <span class="panel-link">查看全部</span>
        </div>

        <div class="orders-table">
          <div class="orders-row orders-head">
            <span>订单号</span>
            <span>用户</span>
            <span>钱</span>
            <span>状态</span>
            <span>下单时间</span>
          </div>

          <template v-if="recentOrderItems.length">
            <div v-for="row in recentOrderItems" :key="row.id" class="orders-row">
              <span class="order-no">{{ row.id }}</span>
              <span class="order-user">
                <el-avatar :size="28" :src="row.avatar || undefined">{{ row.initial }}</el-avatar>
                <span>{{ row.nickName || '匿名用户' }}</span>
              </span>
              <span class="order-amount">{{ row.amountLabel }}</span>
              <span class="order-status">
                <span class="status-dot" :class="statusClass(row.status)"></span>
                {{ statusText(row.status) }}
              </span>
              <span class="order-time">{{ row.createTime || '--' }}</span>
            </div>
          </template>

          <div v-else class="panel-empty">暂无订单数据</div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { Goods, Odometer, ShoppingCart, Tickets } from '@element-plus/icons-vue';
import { fetchItemDashboard, fetchItemOverview, fetchRecentOrders, fetchUserOverview } from '@/api/dashboard';
import { listOrders } from '@/api/orders';
import type { ItemDashboard, OrderModel, RecentOrder } from '@/types/domain';

const ORDER_SNAPSHOT_SIZE = 5000;
const CHART_LEFT = 52;
const CHART_RIGHT = 40;
const CHART_TOP = 24;
const CHART_BOTTOM = 54;
const CHART_WIDTH = 720;
const CHART_HEIGHT = 320;

const loading = ref(false);

function createEmptyItemDashboard(): ItemDashboard {
  return {
    totalItems: 0,
    onShelfItems: 0,
    offShelfItems: 0,
    lowStockItems: 0,
    totalCategories: 0,
    categoryDistribution: [],
    topSellingItems: []
  };
}

const overview = reactive<{
  userTotal: number;
  itemTotal: number;
  orderTotal: number;
  itemDashboard: ItemDashboard;
  recentOrders: RecentOrder[];
  orderRows: OrderModel[];
}>({
  userTotal: 0,
  itemTotal: 0,
  orderTotal: 0,
  itemDashboard: createEmptyItemDashboard(),
  recentOrders: [],
  orderRows: []
});

function formatMoney(amount?: number | null) {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(Number(amount || 0) / 100);
}

function formatInteger(value?: number | null) {
  return new Intl.NumberFormat('en-US').format(Number(value || 0));
}

function normalizeStatus(status: unknown): number {
  if (typeof status === 'number') return status;
  if (typeof status === 'string') return Number(status) || 0;
  if (status && typeof status === 'object' && 'value' in (status as Record<string, unknown>)) {
    return Number((status as { value?: number }).value) || 0;
  }
  return 0;
}

function isPaidStatus(status: unknown) {
  return [2, 3, 4, 6].includes(normalizeStatus(status));
}

function statusText(status: unknown) {
  const value = normalizeStatus(status);
  const map: Record<number, string> = {
    1: '待支付',
    2: '已支付',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '已评价'
  };
  return map[value] || '未知';
}

function statusClass(status: unknown) {
  const value = normalizeStatus(status);
  if (value === 4 || value === 6) return 'is-success';
  if (value === 5) return 'is-danger';
  if (value === 1) return 'is-warning';
  return 'is-info';
}

function parseDateTime(value?: string) {
  if (!value) return null;
  const date = new Date(value.replace(' ', 'T'));
  return Number.isNaN(date.getTime()) ? null : date;
}

function toDayKey(date: Date) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function toAxisLabel(date: Date) {
  return `${`${date.getMonth() + 1}`.padStart(2, '0')}-${`${date.getDate()}`.padStart(2, '0')}`;
}

function toYAxisLabel(amount: number) {
  return formatInteger(Math.round(amount / 100));
}

function createSmoothPath(points: Array<{ x: number; y: number }>) {
  if (!points.length) return '';
  if (points.length === 1) return `M ${points[0].x} ${points[0].y}`;

  let path = `M ${points[0].x} ${points[0].y}`;
  for (let index = 1; index < points.length; index += 1) {
    const previous = points[index - 1];
    const current = points[index];
    const controlX = previous.x + (current.x - previous.x) / 2;
    path += ` C ${controlX} ${previous.y}, ${controlX} ${current.y}, ${current.x} ${current.y}`;
  }
  return path;
}

const orderSnapshotExact = computed(() => overview.orderRows.length >= overview.orderTotal);

const paidOrders = computed(() => overview.orderRows.filter((order) => isPaidStatus(order.status)));

const todaySummary = computed(() => {
  const today = toDayKey(new Date());
  return overview.orderRows.reduce(
    (summary, order) => {
      const date = parseDateTime(order.createTime);
      if (!date || toDayKey(date) !== today) {
        return summary;
      }
      summary.totalOrders += 1;
      if (isPaidStatus(order.status)) {
        summary.paidTotal += Number(order.totalFee || 0);
      }
      return summary;
    },
    { totalOrders: 0, paidTotal: 0 }
  );
});

const recentSummary = computed(() => {
  const start = new Date();
  start.setHours(0, 0, 0, 0);
  start.setDate(start.getDate() - 6);
  return overview.orderRows.reduce(
    (summary, order) => {
      const date = parseDateTime(order.createTime);
      if (!date || date < start) {
        return summary;
      }
      summary.totalOrders += 1;
      if (isPaidStatus(order.status)) {
        summary.paidTotal += Number(order.totalFee || 0);
      }
      return summary;
    },
    { totalOrders: 0, paidTotal: 0 }
  );
});

const cumulativeSales = computed(() =>
  paidOrders.value.reduce((total, order) => total + Number(order.totalFee || 0), 0)
);

const metricCards = computed(() => {
  const exact = orderSnapshotExact.value;
  return [
    {
      key: 'sales',
      label: exact ? '今日销售额' : '近7天销售额',
      value: formatMoney(exact ? todaySummary.value.paidTotal : recentSummary.value.paidTotal),
      unit: '元',
      hint: exact ? '仅统计已支付订单' : '按最近 7 天已支付订单统计',
      icon: Odometer,
      iconBg: 'rgba(91, 145, 255, 0.14)',
      iconColor: '#4f8cff'
    },
    {
      key: 'orders',
      label: exact ? '今日订单数' : '近7天订单数',
      value: formatInteger(exact ? todaySummary.value.totalOrders : recentSummary.value.totalOrders),
      unit: '',
      hint: exact ? '包含待支付与已完成订单' : '按最近 7 天全部订单统计',
      icon: Tickets,
      iconBg: 'rgba(168, 126, 255, 0.14)',
      iconColor: '#9467ff'
    },
    {
      key: 'revenue',
      label: exact ? '累计销售额' : '当前销售额',
      value: formatMoney(cumulativeSales.value),
      unit: '元',
      hint: exact ? `近7天销售 ${formatMoney(recentSummary.value.paidTotal)} 元` : '订单量较大时按当前样本计算',
      icon: Goods,
      iconBg: 'rgba(55, 214, 157, 0.16)',
      iconColor: '#19b884'
    },
    {
      key: 'total',
      label: '订单总数',
      value: formatInteger(overview.orderTotal),
      unit: '',
      hint: `${formatInteger(overview.recentOrders.length)} 笔实时订单`,
      icon: ShoppingCart,
      iconBg: 'rgba(255, 192, 72, 0.18)',
      iconColor: '#f2a11c'
    }
  ];
});

const trendSeries = computed(() => {
  const buckets = new Map<string, number>();
  const dates: Array<{ key: string; label: string }> = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  for (let offset = 6; offset >= 0; offset -= 1) {
    const current = new Date(today);
    current.setDate(today.getDate() - offset);
    const key = toDayKey(current);
    dates.push({ key, label: toAxisLabel(current) });
    buckets.set(key, 0);
  }

  paidOrders.value.forEach((order) => {
    const date = parseDateTime(order.createTime);
    if (!date) return;
    const key = toDayKey(date);
    if (!buckets.has(key)) return;
    buckets.set(key, (buckets.get(key) || 0) + Number(order.totalFee || 0));
  });

  return dates.map((item) => ({
    key: item.key,
    label: item.label,
    value: buckets.get(item.key) || 0
  }));
});

const trendMax = computed(() => {
  const max = Math.max(...trendSeries.value.map((item) => item.value), 0);
  return max > 0 ? Math.ceil(max * 1.1) : 100;
});

const trendPoints = computed(() => {
  const usableWidth = CHART_WIDTH - CHART_LEFT - CHART_RIGHT;
  const usableHeight = CHART_HEIGHT - CHART_TOP - CHART_BOTTOM;
  const baseline = CHART_HEIGHT - CHART_BOTTOM;

  return trendSeries.value.map((item, index) => {
    const x =
      trendSeries.value.length === 1
        ? CHART_LEFT + usableWidth / 2
        : CHART_LEFT + (usableWidth * index) / (trendSeries.value.length - 1);
    const y = baseline - (item.value / trendMax.value) * usableHeight;
    return {
      ...item,
      x,
      y
    };
  });
});

const trendLinePath = computed(() => createSmoothPath(trendPoints.value));

const trendAreaPath = computed(() => {
  if (!trendPoints.value.length) return '';
  const baseline = CHART_HEIGHT - CHART_BOTTOM;
  const first = trendPoints.value[0];
  const last = trendPoints.value[trendPoints.value.length - 1];
  return `${createSmoothPath(trendPoints.value)} L ${last.x} ${baseline} L ${first.x} ${baseline} Z`;
});

const trendGridLines = computed(() => {
  const usableHeight = CHART_HEIGHT - CHART_TOP - CHART_BOTTOM;
  return Array.from({ length: 5 }, (_, index) => {
    const ratio = index / 4;
    const value = Math.round(trendMax.value * (1 - ratio));
    return {
      y: CHART_TOP + usableHeight * ratio,
      label: toYAxisLabel(value)
    };
  });
});

const trendTitle = computed(() => '近7天销售趋势');

const paymentChannels = computed(() => {
  const definitions = [
    { key: 1, label: '支付宝', color: '#5b52ff' },
    { key: 3, label: '余额支付', color: '#4588ff' },
    { key: 2, label: '微信', color: '#22bed7' }
  ];

  const counts = new Map<number, number>();
  paidOrders.value.forEach((order) => {
    if (!order.paymentType) return;
    counts.set(order.paymentType, (counts.get(order.paymentType) || 0) + 1);
  });

  const total = Array.from(counts.values()).reduce((sum, count) => sum + count, 0);
  return definitions.map((item) => {
    const count = counts.get(item.key) || 0;
    return {
      ...item,
      count,
      percent: total ? (count / total) * 100 : 0
    };
  });
});

const channelTotal = computed(() =>
  paymentChannels.value.reduce((total, item) => total + item.count, 0)
);

const channelGradient = computed(() => {
  if (!channelTotal.value) {
    return 'conic-gradient(#e6ebf5 0deg 360deg)';
  }

  let current = 0;
  const chunks = paymentChannels.value
    .filter((item) => item.percent > 0)
    .map((item) => {
      const start = current;
      current += item.percent * 3.6;
      return `${item.color} ${start}deg ${current}deg`;
    });

  return `conic-gradient(${chunks.join(', ')})`;
});

const rankingItems = computed(() => {
  const source = overview.itemDashboard.topSellingItems || [];
  const max = Math.max(...source.map((item) => Number(item.value) || 0), 1);
  return source.slice(0, 10).map((item) => ({
    ...item,
    displayValue: formatInteger(Number(item.value) || 0),
    percent: Math.max(8, Math.round(((Number(item.value) || 0) / max) * 100))
  }));
});

const recentOrderItems = computed(() =>
  overview.recentOrders.slice(0, 5).map((row) => ({
    ...row,
    amountLabel: `${formatMoney(row.totalPrice)}元`,
    initial: (row.nickName || '鱼').trim().slice(0, 1).toUpperCase()
  }))
);

async function load() {
  loading.value = true;
  try {
    const [itemDashboardRes, recentRes, userRes, itemRes, orderRes] = await Promise.all([
      fetchItemDashboard(),
      fetchRecentOrders(),
      fetchUserOverview(),
      fetchItemOverview(),
      listOrders({ pageNo: 1, pageSize: ORDER_SNAPSHOT_SIZE })
    ]);

    overview.itemDashboard =
      itemDashboardRes.code === 200 && itemDashboardRes.data
        ? { ...createEmptyItemDashboard(), ...itemDashboardRes.data }
        : createEmptyItemDashboard();
    overview.recentOrders = recentRes.code === 200 && Array.isArray(recentRes.data) ? recentRes.data : [];
    overview.userTotal = Number(userRes.total || 0);
    overview.itemTotal = Number(itemRes.total || 0);
    overview.orderTotal = Number(orderRes.total || 0);
    overview.orderRows = Array.isArray(orderRes.rows) ? orderRes.rows : [];
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped lang="scss">
.dashboard-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.metric-card {
  min-height: 142px;
  border-radius: 22px;
  background: #ffffff;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05);
  padding: 24px 24px 20px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.metric-copy {
  min-width: 0;
}

.metric-label {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #5f6a7f;
}

.metric-value {
  margin-top: 14px;
  font-size: 48px;
  font-weight: 800;
  line-height: 1;
  color: #202738;
  letter-spacing: -0.04em;
}

.metric-unit {
  margin-left: 4px;
  font-size: 20px;
  font-weight: 700;
  color: #4f5668;
}

.metric-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: #a1a9ba;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.dashboard-main {
  display: grid;
  grid-template-columns: minmax(0, 1.7fr) minmax(320px, 0.8fr);
  gap: 18px;
}

.dashboard-bottom {
  display: grid;
  grid-template-columns: minmax(280px, 0.8fr) minmax(0, 1.5fr);
  gap: 18px;
}

.panel {
  border-radius: 22px;
  background: #ffffff;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05);
  padding: 20px 22px 22px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.panel-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 800;
  color: #2a3244;
}

.panel-bar {
  width: 4px;
  height: 18px;
  border-radius: 999px;
  background: linear-gradient(180deg, #5f4af5 0%, #4f7cff 100%);
}

.panel-tabs {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px;
  border-radius: 10px;
  background: #f2f5fb;
}

.panel-tab {
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  color: #768196;
}

.panel-tab.is-active {
  background: #4f8cff;
  color: #ffffff;
}

.trend-chart {
  margin-top: 16px;
  height: 330px;
}

.trend-chart svg {
  width: 100%;
  height: 100%;
  display: block;
}

.chart-grid-line {
  stroke: #e9edf5;
  stroke-width: 1;
  stroke-dasharray: 4 6;
}

.chart-grid-text,
.chart-axis-text {
  font-size: 12px;
  fill: #a1a9ba;
  text-anchor: middle;
}

.chart-grid-text {
  text-anchor: start;
}

.chart-area {
  fill: url(#trendFill);
}

.chart-line {
  fill: none;
  stroke: #615dff;
  stroke-width: 4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.chart-point {
  fill: #615dff;
  stroke: #ffffff;
  stroke-width: 3;
}

.channel-content {
  min-height: 330px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 28px;
}

.channel-ring {
  width: 232px;
  height: 232px;
  border-radius: 50%;
  position: relative;
}

.channel-ring::after {
  content: '';
  position: absolute;
  inset: 30px;
  background: #ffffff;
  border-radius: 50%;
}

.channel-ring-hole {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.channel-ring-hole strong {
  font-size: 32px;
  color: #253045;
}

.channel-ring-hole span {
  font-size: 13px;
  color: #97a2b5;
}

.channel-legend {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.channel-legend-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #667286;
}

.legend-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
}

.legend-value {
  font-weight: 800;
  color: #273043;
}

.ranking-list {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.ranking-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ranking-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 14px;
}

.ranking-name {
  color: #657086;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ranking-value {
  font-weight: 800;
  color: #8c95a6;
}

.ranking-track {
  height: 12px;
  border-radius: 999px;
  background: #f4f6fb;
  overflow: hidden;
}

.ranking-bar {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #ffcb32 0%, #ffb400 100%);
}

.panel-link {
  font-size: 13px;
  font-weight: 700;
  color: #4f8cff;
}

.orders-table {
  margin-top: 18px;
}

.orders-row {
  display: grid;
  grid-template-columns: minmax(180px, 1.4fr) minmax(160px, 1.1fr) 120px 120px minmax(160px, 1fr);
  align-items: center;
  gap: 16px;
  padding: 16px 10px;
  font-size: 14px;
  color: #5f6a80;
  border-bottom: 1px solid #f1f4f9;
}

.orders-head {
  padding-top: 6px;
  padding-bottom: 14px;
  color: #8893a5;
  font-weight: 700;
  background: #f7f9fc;
  border-radius: 14px;
  border-bottom: none;
}

.orders-row:last-child {
  border-bottom: none;
}

.order-no,
.order-time {
  color: #8792a5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-user {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  color: #2c3547;
  font-weight: 700;
}

.order-user span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-amount {
  color: #202738;
  font-weight: 800;
}

.order-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #5f6a80;
  font-weight: 700;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #97a2b5;
}

.status-dot.is-success {
  background: #19b884;
}

.status-dot.is-danger {
  background: #f56c6c;
}

.status-dot.is-warning {
  background: #f2a11c;
}

.status-dot.is-info {
  background: #6f7c91;
}

.panel-empty {
  padding: 40px 0;
  text-align: center;
  color: #a1a9ba;
  font-size: 14px;
}

@media (max-width: 1280px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-main,
  .dashboard-bottom {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .metric-card {
    min-height: 128px;
    padding: 20px;
  }

  .metric-value {
    font-size: 38px;
  }

  .trend-chart {
    height: 280px;
  }

  .channel-ring {
    width: 188px;
    height: 188px;
  }

  .channel-ring::after {
    inset: 24px;
  }

  .orders-row,
  .orders-head {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .orders-head {
    display: none;
  }

  .orders-row {
    padding: 14px 0;
  }
}
</style>
