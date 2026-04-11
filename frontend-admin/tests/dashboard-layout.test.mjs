import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

async function readSource(relativePath) {
  return readFile(path.resolve(__dirname, '..', relativePath), 'utf8');
}

test('admin layout should expose screenshot-style shell structure', async () => {
  const source = await readSource('src/layout/AdminLayout.vue');

  assert.match(source, /shell-breadcrumb/);
  assert.match(source, /menu-groups/);
  assert.match(source, /商品管理/);
  assert.match(source, /订单管理/);
  assert.match(source, /用户管理/);
  assert.match(source, /权限管理/);
  assert.match(source, /shell-profile/);
  assert.match(source, /el-avatar/);
});

test('dashboard view should expose visual blocks required by the new mockup', async () => {
  const source = await readSource('src/views/dashboard/DashboardView.vue');

  assert.match(source, /dashboard-shell/);
  assert.match(source, /metric-card/);
  assert.match(source, /trend-panel/);
  assert.match(source, /trend-chart/);
  assert.match(source, /channel-panel/);
  assert.match(source, /channel-ring/);
  assert.match(source, /ranking-panel/);
  assert.match(source, /orders-panel/);
});
