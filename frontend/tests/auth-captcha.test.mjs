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

test('login view should wire slider captcha retry flow', async () => {
  const source = await readSource('src/views/login/Login.vue');

  assert.match(source, /SliderCaptchaDialog/);
  assert.match(source, /useSliderCaptcha/);
  assert.match(source, /code === 428/);
});

test('register view should require captcha before submit', async () => {
  const source = await readSource('src/views/register/Register.vue');

  assert.match(source, /SliderCaptchaDialog/);
  assert.match(source, /useSliderCaptcha/);
  assert.match(source, /captchaTicket/);
  assert.match(source, /await formEl\.validate/);
  assert.match(source, /await openCaptcha\('REGISTER'\)/);
});

test('captcha dialog should use track mode metadata instead of puzzle images', async () => {
  const source = await readSource('src/components/auth/SliderCaptchaDialog.vue');
  const apiSource = await readSource('src/api/captcha.ts');

  assert.match(apiSource, /renderMode/);
  assert.match(apiSource, /trackWidth/);
  assert.match(apiSource, /handleWidth/);
  assert.match(apiSource, /targetLeft/);
  assert.match(apiSource, /targetWidth/);
  assert.doesNotMatch(source, /backgroundImage/);
  assert.doesNotMatch(source, /sliderImage/);
  assert.match(source, /targetLeft/);
  assert.match(source, /targetWidth/);
  assert.match(source, /完成安全验证/);
});

test('auth views should not use remote placeholders or deprecated el-link underline booleans', async () => {
  const loginSource = await readSource('src/views/login/Login.vue');
  const userSource = await readSource('src/views/user/User.vue');
  const registerSource = await readSource('src/views/register/Register.vue');
  const shopSource = await readSource('src/views/shop/Shop.vue');
  const searchSource = await readSource('src/views/search/Search.vue');

  assert.doesNotMatch(loginSource, /:underline="false"/);
  assert.doesNotMatch(userSource, /:underline="false"/);
  assert.doesNotMatch(registerSource, /via\.placeholder\.com/);
  assert.doesNotMatch(shopSource, /via\.placeholder\.com/);
  assert.doesNotMatch(searchSource, /via\.placeholder\.com/);
});
