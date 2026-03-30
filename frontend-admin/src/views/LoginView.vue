<template>
  <div class="login-page">
    <div class="bg-orb orb-left"></div>
    <div class="bg-orb orb-right"></div>

    <el-card class="login-card" shadow="never">
      <div class="login-header">
        <div class="logo">YU</div>
        <h1>YU-Mall 管理端</h1>
        <p>登录后可管理用户、商品、分类、订单与控制台看板</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
        </el-form-item>

        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="submit">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRouter } from 'vue-router';
import { login } from '@/api/auth';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const form = reactive({
  username: '',
  password: '',
  rememberMe: false
});

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

async function submit() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;

  loading.value = true;
  try {
    const res = await login(form);
    if (res.code !== 200 || !res.data?.token) {
      ElMessage.error(res.msg || '登录失败');
      return;
    }
    authStore.setLogin({
      token: res.data.token,
      userId: res.data.userId,
      nickName: res.data.nickName,
      avatar: res.data.avatar
    });
    ElMessage.success('登录成功');
    router.replace('/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
}

.orb-left {
  width: 360px;
  height: 360px;
  left: -80px;
  top: -120px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.24), transparent 70%);
}

.orb-right {
  width: 420px;
  height: 420px;
  right: -120px;
  bottom: -140px;
  background: radial-gradient(circle, rgba(54, 207, 201, 0.28), transparent 70%);
}

.login-card {
  width: 440px;
  max-width: 100%;
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(12px);
}

.login-header {
  text-align: center;
  margin-bottom: 14px;

  .logo {
    width: 58px;
    height: 58px;
    margin: 0 auto 12px;
    border-radius: 16px;
    background: linear-gradient(135deg, #36cfc9, #409eff);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    font-weight: 800;
  }

  h1 {
    margin: 0;
    font-size: 26px;
    background: linear-gradient(45deg, #409eff, #36cfc9);
    -webkit-background-clip: text;
    color: transparent;
  }

  p {
    margin: 8px 0 0;
    color: #64748b;
    font-size: 13px;
  }
}

.submit-btn {
  width: 100%;
}
</style>