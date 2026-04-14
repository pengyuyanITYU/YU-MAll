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

test('admin router and layout should expose brand management entry', async () => {
  const [routerSource, layoutSource] = await Promise.all([
    readSource('src/router/index.ts'),
    readSource('src/layout/AdminLayout.vue')
  ]);

  assert.match(routerSource, /\/brands/);
  assert.match(routerSource, /BrandManageView\.vue/);
  assert.match(layoutSource, /\/brands/);
});

test('item manage view should load brand options instead of free text brand input', async () => {
  const source = await readSource('src/views/items/ItemManageView.vue');

  assert.match(source, /listBrandsSimple/);
  assert.match(source, /brandOptions/);
  assert.match(source, /<el-select v-model="form\.brand"/);
});
