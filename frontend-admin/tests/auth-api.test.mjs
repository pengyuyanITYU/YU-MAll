import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

test('admin auth api should use admin login endpoint', async () => {
  const authApiPath = path.resolve(__dirname, '../src/api/auth.ts');
  const source = await readFile(authApiPath, 'utf8');

  assert.match(source, /\/admins\/login/);
  assert.doesNotMatch(source, /\/users\/login/);
});

test('admin auth api should expose register endpoint on admin service', async () => {
  const authApiPath = path.resolve(__dirname, '../src/api/auth.ts');
  const source = await readFile(authApiPath, 'utf8');

  assert.match(source, /\/admins\/register/);
  assert.match(source, /export function register/);
});
