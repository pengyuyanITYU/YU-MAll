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

test('router should expose a public register route for the shared auth page', async () => {
  const source = await readSource('src/router/index.ts');

  assert.match(source, /path:\s*'\/register'/);
  assert.match(source, /meta:\s*\{\s*title:\s*'管理端注册',\s*public:\s*true\s*\}/);
});

test('login view should provide a shared auth shell with mode switching', async () => {
  const source = await readSource('src/views/LoginView.vue');

  assert.match(source, /auth-shell/);
  assert.match(source, /switchAuthMode/);
  assert.match(source, /activeMode/);
  assert.match(source, /registerForm/);
  assert.match(source, /router\.replace\('\/register'\)/);
  assert.match(source, /router\.replace\('\/login'\)/);
});
