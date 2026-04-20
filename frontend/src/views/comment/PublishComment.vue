<template>
  <div class="publish-comment-page">
    <el-card class="comment-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="title">{{ isResubmit ? '修改评价' : '发表评价' }}</span>
          <span class="sub-title">订单号 {{ form.orderId }}</span>
        </div>
      </template>

      <div class="product-info">
        <el-image :src="productInfo.image" fit="cover" class="product-image" />
        <div class="product-main">
          <h3 class="product-name">{{ productInfo.name }}</h3>
          <p v-if="productInfo.skuSpecs" class="product-spec">{{ productInfo.skuSpecs }}</p>
          <p v-if="isResubmit && rejectReason" class="reject-reason">驳回原因：{{ rejectReason }}</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="商品评分" prop="rating">
          <div class="rating-row">
            <el-rate
              v-model="form.rating"
              show-text
              :texts="['非常差', '差', '一般', '好', '非常好']"
              :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            />
            <span v-if="form.rating > 0" class="rating-text">{{ ratingText }}</span>
          </div>
        </el-form-item>

        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="写下你对这件商品的真实体验"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="晒单图片（最多 9 张）">
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
          <el-dialog v-model="dialogVisible">
            <img :src="dialogImageUrl" alt="preview" style="width: 100%">
          </el-dialog>
        </el-form-item>

        <el-form-item>
          <div class="anonymous-row">
            <el-checkbox v-model="form.isAnonymous" border>
              <el-icon class="checkbox-icon"><Hide /></el-icon>
              匿名评价
            </el-checkbox>
            <span class="anonymous-tip">勾选后会隐藏昵称</span>
          </div>
        </el-form-item>

        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="loading" @click="submitForm(formRef)">
            {{ isResubmit ? '重新提交审核' : '提交评价' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type UploadProps, type UploadRequestOptions, type UploadUserFile } from 'element-plus'
import { Hide, Plus } from '@element-plus/icons-vue'
import { addComment, getUserCommentDetail, updateComment, type CommentDTO } from '@/api/comment'
import { uploadFile } from '@/api/upload'
import { useUserStore } from '@/stores/useUserStore'
import { isHandledRequestError } from '@/utils/request'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const imageUploading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const rejectReason = ref('')
const { userInfo } = useUserStore()

const productInfo = reactive({
  name: '加载中...',
  image: '',
  skuSpecs: ''
})

const form = reactive<CommentDTO>({
  itemId: '',
  skuId: undefined,
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
    { required: true, message: '请给商品打分', trigger: 'change' },
    { type: 'number', min: 1, message: '评分最低为 1 分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请填写评价内容', trigger: 'blur' },
    { min: 5, message: '至少输入 5 个字', trigger: 'blur' }
  ]
}

const commentId = computed(() => (route.query.commentId as string) || '')
const isResubmit = computed(() => Boolean(commentId.value))

const ratingText = computed(() => {
  const texts = ['', '失望', '一般', '还行', '满意', '惊喜']
  return texts[form.rating] || ''
})

const customUploadRequest = async (options: UploadRequestOptions) => {
  const { file } = options
  imageUploading.value = true
  try {
    const res: any = await uploadFile(file as any)
    const uploadedUrl = res?.url || res?.data?.url
    if (!uploadedUrl) {
      throw new Error('上传未返回图片地址')
    }
    form.images = [...(form.images || []), uploadedUrl]
    const currentFile = fileList.value.find(item => item.uid === file.uid)
    if (currentFile) {
      currentFile.url = uploadedUrl
    }
    ElMessage.success('图片上传成功')
  } catch (error) {
    console.error('上传评价图片失败', error)
    ElMessage.error('图片上传失败，请稍后重试')
    fileList.value = fileList.value.filter(item => item.uid !== file.uid)
  } finally {
    imageUploading.value = false
  }
}

const handlePictureCardPreview: UploadProps['onPreview'] = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url || ''
  dialogVisible.value = true
}

const handleRemove: UploadProps['onRemove'] = (uploadFile) => {
  const removedUrl = uploadFile.url
  if (!removedUrl) {
    return
  }
  form.images = (form.images || []).filter(url => url !== removedUrl)
}

const beforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  const isLt5M = rawFile.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('图片必须是 JPG 或 PNG 格式')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

function buildPayload(): CommentDTO {
  const payload: CommentDTO = {
    itemId: form.itemId,
    orderId: form.orderId,
    orderDetailId: form.orderDetailId,
    rating: form.rating,
    content: form.content,
    images: form.images || [],
    isAnonymous: form.isAnonymous,
    userNickname: form.userNickname,
    userAvatar: form.userAvatar
  }
  if (form.skuId && Number(form.skuId) > 0) {
    payload.skuId = form.skuId
  }
  return payload
}

function fillProductInfoFromQuery() {
  const { itemId, skuId, orderId, orderDetailId, productName, productImage, skuSpecs } = route.query
  form.itemId = (itemId as string) || ''
  form.orderId = (orderId as string) || ''
  form.orderDetailId = (orderDetailId as string) || ''
  if (skuId && Number(skuId) > 0) {
    form.skuId = skuId as string
  }
  productInfo.name = (productName as string) || '商品信息缺失'
  productInfo.image = (productImage as string) || '/placeholder-image.svg'
  productInfo.skuSpecs = (skuSpecs as string) || ''
}

function syncFileList(images?: string[]) {
  fileList.value = (images || []).map((url, index) => ({
    name: `image-${index + 1}`,
    url
  }))
}

async function loadRejectedComment() {
  if (!isResubmit.value) {
    return
  }
  try {
    const res: any = await getUserCommentDetail(commentId.value)
    const detail = res?.data || res
    if (!detail) {
      throw new Error('评论详情不存在')
    }
    if (Number(detail.status) !== 2) {
      ElMessage.error('只有已驳回评论可以重新提交')
      router.replace({ path: '/user', query: { tab: 'comments' } })
      return
    }
    form.rating = Number(detail.rating || 0)
    form.content = detail.content || ''
    form.images = Array.isArray(detail.images) ? [...detail.images] : []
    form.isAnonymous = Boolean(detail.isAnonymous)
    rejectReason.value = detail.rejectReason || ''
    syncFileList(form.images)
  } catch (error) {
    console.error('加载评论详情失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('评论服务开小差了，请稍后再试')
    }
    router.replace({ path: '/user', query: { tab: 'comments' } })
  }
}

async function submitForm(formEl: FormInstance | undefined) {
  if (!formEl) {
    return
  }
  if (imageUploading.value) {
    ElMessage.warning('图片仍在上传，请稍后')
    return
  }
  const valid = await formEl.validate().catch(() => false)
  if (!valid) {
    return
  }
  loading.value = true
  try {
    if (isResubmit.value) {
      await updateComment(commentId.value, buildPayload())
    } else {
      await addComment(buildPayload())
    }
    ElMessage.success('已提交审核')
    if (isResubmit.value) {
      router.push({ path: '/user', query: { tab: 'comments' } })
      return
    }
    router.push(`/order/${form.orderId}`)
  } catch (error) {
    console.error('提交评论失败', error)
    if (!isHandledRequestError(error)) {
      ElMessage.error('评论服务开小差了，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (isResubmit.value) {
    router.push({ path: '/user', query: { tab: 'comments' } })
    return
  }
  router.back()
}

onMounted(async () => {
  fillProductInfoFromQuery()
  form.userAvatar = userInfo.avatar
  form.userNickname = userInfo.nickName
  await loadRejectedComment()
})
</script>

<style scoped lang="scss">
.publish-comment-page {
  min-height: calc(100vh - 64px);
  max-width: 980px;
  margin: 0 auto;
  padding: 96px 16px 88px;
}

.comment-card {
  border-radius: 18px;
  border: none;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.title {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.sub-title {
  color: #6b7280;
}

.product-info {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 18px;
  border-radius: 16px;
  background: #f9fafb;
  margin-bottom: 24px;
}

.product-image {
  width: 88px;
  height: 88px;
  border-radius: 16px;
  overflow: hidden;
}

.product-name {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.product-spec {
  margin: 10px 0 0;
  color: #6b7280;
}

.reject-reason {
  margin: 12px 0 0;
  color: #dc2626;
  font-size: 13px;
}

.rating-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.rating-text {
  color: #f59e0b;
  font-weight: 600;
}

.anonymous-row {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.checkbox-icon {
  margin-right: 6px;
}

.anonymous-tip {
  color: #6b7280;
  font-size: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 640px) {
  .publish-comment-page {
    padding: 84px 12px 72px;
  }

  .product-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-actions {
    justify-content: stretch;
  }
}
</style>
