<template>
  <div class="publish-comment-container">
    <el-card class="box-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">发表评价</span>
          <span class="sub-title">订单号: {{ form.orderId }}</span>
        </div>
      </template>

      <!-- 1. 商品信息展示区 -->
      <div class="product-info">
        <el-image :src="productInfo.image" fit="cover" class="product-img" />
        <div class="product-detail">
          <h3 class="product-name">{{ productInfo.name }}</h3>
          <p class="product-sku">{{ productInfo.skuSpecs }}</p>
        </div>
      </div>

      <!-- 2. 评价表单区 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        class="comment-form"
      >
        <!-- 评分 -->
        <el-form-item label="商品评分" prop="rating">
          <div class="rating-wrapper">
            <el-rate
              v-model="form.rating"
              show-text
              :texts="['非常差', '差', '一般', '好', '非常好']"
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            />
            <span class="rating-tip" v-if="form.rating > 0">{{ ratingText }}</span>
          </div>
        </el-form-item>

        <!-- 评价内容 -->
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="商品满足你的期待吗？说说它的优点和不足的地方吧~"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 图片上传 (修改点：增加 http-request，移除 auto-upload) -->
        <el-form-item label="晒图 (最多9张)" prop="images">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :http-request="customUploadRequest"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :limit="9"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <!-- 图片预览弹窗 -->
          <el-dialog v-model="dialogVisible">
            <img w-full :src="dialogImageUrl" alt="Preview Image" style="width: 100%" />
          </el-dialog>
        </el-form-item>

        <!-- 匿名选项 -->
        <el-form-item>
          <div class="options-row">
            <el-checkbox v-model="form.isAnonymous" border>
              <el-icon class="el-icon--left"><Hide /></el-icon>
              匿名评价
            </el-checkbox>
            <span class="tip-text">勾选后，您的头像和昵称将对其他用户隐藏</span>
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <div class="form-footer">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="loading" @click="submitForm(formRef)">
            发布评价
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type UploadProps, type UploadUserFile, type UploadRequestOptions } from 'element-plus'
import { Plus, Hide } from '@element-plus/icons-vue'
import { addComment, type CommentDTO } from '@/api/comment'
import { uploadFile } from "@/api/upload"; // 引入上传接口
import { useUserStore } from '@/stores/useUserStore'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const imageUploading = ref(false) // 图片上传状态

// 图片上传相关
const fileList = ref<UploadUserFile[]>([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const {userInfo} = useUserStore()

const productInfo = reactive({
  name: '加载中...',
  image: '',
  skuSpecs: ''
})

const form = reactive<CommentDTO>({
  itemId: '',
  skuId: '',
  orderId: '',
  orderDetailId: '',
  rating: 0,
  content: '',
  images: [],
  isAnonymous: false,
  userNickname: '',
  userAvatar: ''
})

const rules = {
  rating: [
    { required: true, message: '请给商品打个分吧', trigger: 'change' },
    { type: 'number', min: 1, message: '评分最低为1分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请填写评价内容', trigger: 'blur' },
    { min: 5, message: '多写几个字吧,至少5个字哦', trigger: 'blur' }
  ]
}

const ratingText = computed(() => {
  const texts = ['', '失望', '一般', '不错', '满意', '惊喜']
  return texts[form.rating] || ''
})

onMounted(() => {
  const { itemId, skuId, orderId, orderDetailId, productName, productImage, skuSpecs } = route.query
  form.itemId = itemId as string
  if (skuId) form.skuId = skuId as string
  if (orderId) form.orderId = orderId as string
  if (orderDetailId) form.orderDetailId = orderDetailId as string

  productInfo.name = (productName as string) || '示例商品名称'
  productInfo.image = (productImage as string) || 'https://via.placeholder.com/150'
  productInfo.skuSpecs = (skuSpecs as string) || '默认规格'
  form.userAvatar = userInfo.avatar
  form.userNickname = userInfo.nickName
})

// --- 核心修改：自定义上传逻辑 ---
const customUploadRequest = async (options: UploadRequestOptions) => {
  const { file } = options
  imageUploading.value = true
  try {
    const res: any = await uploadFile(file as any)
    const uploadedUrl = res?.url || res?.data?.url

    if (uploadedUrl) {
      // 将上传成功的地址存入表单
      form.images?.push(uploadedUrl)
      // 必须更新 fileList 中对应项的 url，否则预览会失效（显示为 blob 地址）
      const currentFile = fileList.value.find(f => f.uid === file.uid)
      if (currentFile) {
        currentFile.url = uploadedUrl
      }
      ElMessage.success("图片上传成功")
    }
  } catch (error: any) {
    ElMessage.error("图片上传失败")
    // 上传失败时，从文件列表中移除该项
    const index = fileList.value.findIndex(f => f.uid === file.uid)
    if (index !== -1) fileList.value.splice(index, 1)
  } finally {
    imageUploading.value = false
  }
}

// 图片预览
const handlePictureCardPreview: UploadProps['onPreview'] = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url!
  dialogVisible.value = true
}

// 图片移除 (修改点：同步移除 form.images 中的数据)
const handleRemove: UploadProps['onRemove'] = (uploadFile) => {
  const removedUrl = uploadFile.url
  if (form.images) {
    form.images = form.images.filter(url => url !== removedUrl)
  }
}

// 上传前校验
const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImg = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  const isLt5M = rawFile.size / 1024 / 1024 < 5

  if (!isImg) {
    ElMessage.error('图片必须是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 提交表单
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  if (imageUploading.value) {
    return ElMessage.warning("图片正在上传中，请稍候")
  }

  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 由于已经在 customUploadRequest 中维护了 form.images，
        // 这里直接提交 form 即可，无需再次处理 fileList
        await addComment(form)
        ElMessage.success('评价发布成功！')
        router.push(`/order/${form.orderId}`)
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
/* 样式保持不变 */
.publish-comment-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;

  .box-card {
    border-radius: 12px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
    }
    .sub-title {
      font-size: 14px;
      color: #909399;
    }
  }

  .product-info {
    display: flex;
    align-items: center;
    padding: 20px;
    background-color: #f8f9fa;
    border-radius: 8px;
    margin-bottom: 30px;

    .product-img {
      width: 80px;
      height: 80px;
      border-radius: 6px;
      border: 1px solid #ebeef5;
      margin-right: 16px;
    }

    .product-detail {
      .product-name {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
        margin: 0 0 8px 0;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      .product-sku {
        font-size: 13px;
        color: #909399;
        margin: 0;
      }
    }
  }

  .comment-form {
    .rating-wrapper {
      display: flex;
      align-items: center;
      gap: 15px;

      .rating-tip {
        font-size: 14px;
        color: #ff9900;
        font-weight: bold;
      }
    }

    .options-row {
      display: flex;
      align-items: center;
      gap: 15px;

      .tip-text {
        font-size: 12px;
        color: #909399;
      }
    }

    .form-footer {
      margin-top: 40px;
      display: flex;
      justify-content: flex-end;
      gap: 15px;
    }
  }
}

@media (max-width: 768px) {
  .publish-comment-container {
    margin: 10px auto;
    padding: 0;

    .product-info {
      padding: 10px;
      .product-img {
        width: 60px;
        height: 60px;
      }
    }
  }
}
</style>