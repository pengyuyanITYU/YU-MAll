<template>
  <div class="register-container">
    <!-- 背景装饰圆球 -->
    <div class="circle circle-1"></div>
    <div class="circle circle-2"></div>

    <el-card class="register-card animate__animated">
      <!-- 品牌 Logo 区域 -->
      <div class="brand-header">
        <div class="logo-box">
          <el-icon :size="32" color="#fff"><ShoppingBag /></el-icon>
        </div>
        <h2>创建新账号</h2>
        <p>加入 YU-Mall,开启尊享购物之旅</p>
      </div>

      <el-form
        ref="ruleFormRef"
        :model="form"
        :rules="rules"
        size="large"
        class="register-form"
        status-icon
      >
        <!-- 创意头像区域 -->
        <div class="creative-avatar-wrapper">
          <el-form-item prop="avatar" class="avatar-form-item">
            <!-- 
              修改说明：
              1. action 改为 "#" (禁用默认上传地址)
              2. 增加 :http-request="customUploadRequest" (接管上传逻辑)
              3. 去掉 :on-success (逻辑移入 http-request 中处理)
            -->
            <el-upload
              ref="uploadRef"
              class="avatar-uploader"
              action="#"
              :http-request="customUploadRequest"
              :show-file-list="false"
              :before-upload="BeforeUpload"
              accept="image/*"
            >
              <!-- 状态 A: 已上传 (有 URL 时显示) -->
              <div
                v-if="form.avatar"
                class="avatar-preview-box animate__animated animate__zoomIn"
              >
                <img :src="form.avatar" class="avatar-img" />
                <div class="avatar-hover-mask">
                  <el-icon :size="24"><Edit /></el-icon>
                  <span>更换</span>
                </div>
              </div>

              <!-- 状态 B: 上传中 (Loading) -->
              <div v-else-if="avatarLoading" class="avatar-upload-trigger">
                <div class="trigger-circle">
                  <el-icon class="is-loading" :size="28"><Loading /></el-icon>
                </div>
                <div class="trigger-label">上传中...</div>
              </div>

              <!-- 状态 C: 未上传 -->
              <div v-else class="avatar-upload-trigger">
                <div class="trigger-circle">
                  <el-icon :size="28" class="camera-icon"
                    ><CameraFilled
                  /></el-icon>
                </div>
                <div class="trigger-label">设置头像</div>
              </div>
            </el-upload>
          </el-form-item>
        </div>

        <!-- 用户名 (账号) -->
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="设置账号 (登录使用)"
            :prefix-icon="User"
            class="custom-input"
          />
        </el-form-item>

        <!-- 新增：昵称 -->
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickName"
            placeholder="设置昵称 (显示名称)"
            :prefix-icon="Postcard"
            class="custom-input"
          />
        </el-form-item>

        <!-- 手机/邮箱 -->
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            :prefix-icon="Iphone"
            class="custom-input"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="设置密码 (6-20位)"
            :prefix-icon="Lock"
            show-password
            class="custom-input"
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次确认密码"
            :prefix-icon="Lock"
            show-password
            class="custom-input"
          />
        </el-form-item>

        <!-- 协议勾选 -->
        <el-form-item prop="agree" class="agree-item">
          <div class="agree-wrapper">
            <el-checkbox v-model="form.agree" class="custom-checkbox">
              <span class="agree-text">我已阅读并同意</span>
            </el-checkbox>
            <span class="protocol-link" @click="openAgreement"
              >《用户服务协议》</span
            >
          </div>
        </el-form-item>
        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            class="register-btn"
            @click="handleRegister(ruleFormRef)"
            :loading="loading"
            round
          >
            立即注册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部跳转 -->
      <div class="register-footer">
        <span>已有账号？</span>
        <el-link
          type="primary"
          :underline="false"
          @click="login"
          class="login-link"
        >
          立即登录 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import {
  User,
  Lock,
  ShoppingBag,
  ArrowRight,
  Edit,
  Loading,
  CameraFilled,
  Postcard,
  Iphone,
} from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import type {
  FormInstance,
  FormRules,
  UploadRequestOptions,
  UploadInstance,
} from "element-plus";
import { register } from "@/api/register";
import { uploadFile } from "@/api/upload";
import { useUserStore } from "@/stores/useUserStore";
const router = useRouter();
const ruleFormRef = ref<FormInstance>();
const loading = ref(false);
const avatarLoading = ref(false);
const uploadRef = ref<UploadInstance>();
const userStore:any = useUserStore();
// 定义表单接口
interface RegisterForm {
  avatar: string;
  avatarUrl: string;
  username: string;
  nickName: string;
  phone: string;
  password: string;
  confirmPassword: string;
  agree: boolean;
}

const form = reactive<RegisterForm>({
  avatar: "",
  avatarUrl: "",
  username: "",
  nickName: "",
  phone: "",
  password: "",
  confirmPassword: "",
  agree: false,
});

const login = () => {
  router.push("/login");
};

const openAgreement = () => {
  const url = "https://www.jd.com/help/question-61.html";
  window.open(url, "_blank");
};

// 1. 上传前的校验
const BeforeUpload = (file: File) => {
  const isImage = file.type.startsWith("image/");
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error("只能上传图片文件!");
    return false; // 阻止上传
  }
  if (!isLt2M) {
    ElMessage.error("图片大小不能超过 2MB!");
    return false; // 阻止上传
  }
  return true; // 允许上传
};

// 2. 自定义上传方法 (核心修复)
const customUploadRequest = async (options: UploadRequestOptions) => {
  const { file } = options;

  // 开启 Loading
  avatarLoading.value = true;

  try {
  
    const res: any = await uploadFile(file as any); 

    // 处理返回结果
    // 兼容不同的后端返回结构，有的在 res.url，有的在 res.data.url
    const uploadedUrl = res?.url || res?.data?.url;

    if (uploadedUrl) {
      form.avatar = uploadedUrl;
      // form.avatarUrl = uploadedUrl; // 如果需要双重赋值
      ElMessage.success("头像上传成功");
    } else {
      throw new Error("返回数据中未找到图片地址");
    }
  } catch (error: any) {
    console.error("上传失败详情:", error);
    ElMessage.error(error.message || "头像上传失败，请检查网络");
  } finally {
    // 无论成功还是失败，都关闭 loading
    avatarLoading.value = false;
  }
};

// 密码一致性校验
const validatePass = (rule: any, value: string, callback: any) => {
  if (value === "") {
    callback(new Error("请再次输入密码"));
  } else if (value !== form.password) {
    callback(new Error("两次输入密码不一致!"));
  } else {
    callback();
  }
};

const phoneReg = /^1[3-9]\d{9}$/;

// 校验规则
const rules = reactive<FormRules<RegisterForm>>({
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 15, message: "长度在 2 到 15 个字符", trigger: "blur" },
  ],
  nickName: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { max: 10, message: "昵称最长 10 个字符", trigger: "blur" },
  ],
  phone: [
    { required: true, message: "请输入手机号", trigger: "blur" },
    {
      pattern: phoneReg,
      message: "手机格式不正确",
      trigger: ["blur", "change"],
    },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 20, message: "长度在 6 到 20 个字符", trigger: "blur" },
  ],
  confirmPassword: [{ validator: validatePass, trigger: "blur" }],
  agree: [
    {
      validator: (rule: any, value: boolean, callback: any) => {
        value ? callback() : callback(new Error("请先勾选协议"));
      },
      trigger: "change",
    },
  ],
});

const handleRegister = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;

  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const { confirmPassword, agree, ...params } = form;
        const result: any = await register(params);
         userStore.updateUserInfo(result.data)
        // 假设注册成功返回 token
        if (!result) { // 这里根据你的后端实际返回判断
           throw new Error("注册无响应");
        }
        
        // 存储 Token (如果有)
        if(result.data.token) {
           localStorage.setItem("Authorization", result.data.token);
        }

        ElMessage.success({
          message: "注册成功，欢迎加入 YU-Mall",
          type: "success",
        });

        setTimeout(() => {
          router.push("/");
        }, 900);
        
      } catch (error:any) {
        console.error(error);
        ElMessage.error(error.message || "注册失败，请稍后重试");
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<style scoped>
/* 样式保持原样 */
.register-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #a0cfff);
  filter: blur(40px);
  z-index: 0;
  animation: float 6s ease-in-out infinite;
  opacity: 0.6;
}
.circle-1 {
  top: 5%;
  left: 15%;
  width: 200px;
  height: 200px;
}
.circle-2 {
  bottom: 10%;
  right: 10%;
  width: 300px;
  height: 300px;
  animation-delay: -3s;
}

.register-card {
  width: 100%;
  max-width: 440px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
  padding: 10px 15px;
  z-index: 1;
  animation: fadeInUp 0.8s ease-out;
}

.brand-header {
  text-align: center;
  margin-bottom: 20px;
}

.logo-box {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.brand-header h2 {
  margin: 0 0 8px;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.brand-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* --- 头像上传样式 --- */
.avatar-item {
  display: flex;
  justify-content: center;
  margin-bottom: 25px !important;
}

:deep(.avatar-item .el-form-item__content) {
  justify-content: center;
  margin-left: 0 !important;
}

.avatar-uploader {
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s;
}

.avatar-uploader:hover {
  transform: scale(1.05);
}

.avatar-uploader-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.7);
  border: 2px dashed #dcdfe6;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
  transition: all 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
  background-color: #fff;
}

/* --- 创意头像区域 --- */
.creative-avatar-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 25px;
  position: relative;
}

.avatar-form-item {
  margin-bottom: 0 !important;
}

.avatar-upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  group: hover;
}

.trigger-circle {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: linear-gradient(
    145deg,
    rgba(255, 255, 255, 0.9),
    rgba(236, 245, 255, 0.6)
  );
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.15),
    inset 0 0 15px rgba(255, 255, 255, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  z-index: 1;
}

.trigger-circle::after {
  content: "";
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  border: 1px dashed rgba(64, 158, 255, 0.4);
  animation: rotateCircle 10s linear infinite;
  opacity: 0;
  transition: opacity 0.3s;
}

.camera-icon {
  color: #a0cfff;
  transition: all 0.3s;
}

.trigger-label {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  opacity: 0.8;
  transition: all 0.3s;
}

.avatar-upload-trigger:hover .trigger-circle {
  transform: translateY(-4px) scale(1.05);
  background: #fff;
  box-shadow: 0 12px 30px rgba(64, 158, 255, 0.3);
  border-color: #fff;
}

.avatar-upload-trigger:hover .trigger-circle::after {
  opacity: 1;
}

.avatar-upload-trigger:hover .camera-icon {
  color: #409eff;
  transform: scale(1.1);
}

.avatar-upload-trigger:hover .trigger-label {
  color: #409eff;
  transform: translateY(2px);
}

.avatar-preview-box {
  position: relative;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  padding: 3px;
  background: #fff;
  box-shadow: 0 10px 25px rgba(31, 38, 135, 0.15);
  cursor: pointer;
  transition: transform 0.3s;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}

.avatar-hover-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(23, 23, 23, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  opacity: 0;
  transition: all 0.3s ease;
  font-size: 12px;
}

.avatar-hover-mask .el-icon {
  margin-bottom: 2px;
}

.avatar-preview-box:hover {
  transform: scale(1.02);
}

.avatar-preview-box:hover .avatar-hover-mask {
  opacity: 1;
}

@keyframes rotateCircle {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

:deep(.el-input__wrapper) {
  border-radius: 50px;
  padding: 8px 20px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
  background-color: rgba(255, 255, 255, 0.7);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset, 0 0 0 3px rgba(64, 158, 255, 0.2) !important;
  background-color: #fff;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  margin-top: 10px;
  background: linear-gradient(90deg, #409eff 0%, #337ecc 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.4);
  background: linear-gradient(90deg, #53a8ff 0%, #3a8ee6 100%);
}

.agree-item {
  margin-bottom: 20px;
}

.agree-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 14px;
  line-height: 1;
}

.agree-text {
  color: #606266;
  margin-right: 4px;
  transition: color 0.3s;
}

.protocol-link {
  color: #409eff;
  cursor: pointer;
  font-weight: 600;
  position: relative;
  transition: all 0.3s ease;
  user-select: none;
}

.protocol-link::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 1.5px;
  bottom: -2px;
  left: 0;
  background-color: #409eff;
  transform: scaleX(0);
  transform-origin: bottom right;
  transition: transform 0.3s ease-out;
}

.protocol-link:hover {
  color: #337ecc;
}

.protocol-link:hover::after {
  transform: scaleX(1);
  transform-origin: bottom left;
}

:deep(.el-checkbox__inner) {
  border-radius: 4px;
  width: 16px;
  height: 16px;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #409eff;
  border-color: #409eff;
}

:deep(.el-checkbox__input.is-checked + .el-checkbox__label .agree-text) {
  color: #303133;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #ebeef5;
  font-size: 14px;
  color: #606266;
}

.login-link {
  margin-left: 8px;
  font-weight: 600;
  transition: transform 0.3s;
}

.login-link:hover {
  transform: translateX(4px);
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
  100% {
    transform: translateY(0px);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translate3d(0, 40px, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

@media screen and (max-width: 480px) {
  .register-card {
    max-width: 90%;
    padding: 20px 10px;
  }
}
</style>