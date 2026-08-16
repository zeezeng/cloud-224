<template>
  <div class="page-container">
    <n-card>
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="关键词">
            <n-input v-model:value="searchForm.keyword" placeholder="搜索反馈内容" clearable />
          </n-form-item>
          <n-form-item label="类型">
            <n-select
              v-model:value="searchForm.feedbackType"
              placeholder="请选择类型"
              clearable
              style="width: 140px"
              :options="feedbackTypeOptions"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 140px"
              :options="statusOptions"
            />
          </n-form-item>
          <n-form-item label="提交时间">
            <n-date-picker
              v-model:formatted-value="searchForm.timeRange"
              type="datetimerange"
              clearable
              value-format="yyyy-MM-dd HH:mm:ss"
              style="width: 360px"
            />
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch">
                <template #icon><n-icon><SearchOutline /></n-icon></template>
                搜索
              </n-button>
              <n-button @click="handleReset">
                <template #icon><n-icon><RefreshOutline /></n-icon></template>
                重置
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </div>

      <div class="table-toolbar">
        <n-space>
          <n-button v-if="hasPermission('yun:feedback:handle')" type="primary" @click="openContactModal">
            <template #icon><n-icon><ChatbubbleOutline /></n-icon></template>
            联系设置
          </n-button>
          <n-button
            v-if="hasPermission('yun:feedback:remove')"
            type="error"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            <template #icon><n-icon><TrashOutline /></n-icon></template>
            删除{{ selectedIds.length > 0 ? `(${selectedIds.length})` : '' }}
          </n-button>
        </n-space>
      </div>

      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        :scroll-x="1260"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        @update:checked-row-keys="handleCheck"
      />
    </n-card>

    <n-modal v-model:show="detailVisible" preset="card" title="反馈详情" style="width: 760px">
      <div v-if="detailData" class="feedback-detail">
        <div class="detail-header">
          <div>
            <div class="detail-meta">
              <span>{{ getTypeLabel(detailData.feedbackType) }}</span>
              <span>提交时间：{{ detailData.createTime || '-' }}</span>
              <span>页面：{{ detailData.pagePath || '-' }}</span>
            </div>
          </div>
          <n-tag :type="getStatusTagType(detailData.status)" size="small">
            {{ getStatusLabel(detailData.status) }}
          </n-tag>
        </div>
        <n-divider />
        <div class="detail-section">
          <div class="detail-label">反馈内容</div>
          <div class="detail-content">{{ detailData.content }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">客户端信息</div>
          <div class="detail-code">{{ detailData.clientInfo || '-' }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">处理记录</div>
          <div class="detail-content">
            <div>处理人：{{ detailData.handlerName || detailData.handlerId || '-' }}</div>
            <div>处理时间：{{ detailData.handledAt || '-' }}</div>
            <div>内部备注：{{ detailData.handleRemark || '-' }}</div>
          </div>
        </div>
      </div>
    </n-modal>

    <n-modal v-model:show="handleVisible" preset="card" title="处理反馈" style="width: 620px">
      <n-form :model="handleForm" label-placement="left" label-width="92px">
        <n-form-item label="处理状态">
          <n-radio-group v-model:value="handleForm.status">
            <n-radio :value="0">待处理</n-radio>
            <n-radio :value="1">处理中</n-radio>
            <n-radio :value="2">已完成</n-radio>
            <n-radio :value="3">已忽略</n-radio>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="内部备注">
          <n-input
            v-model:value="handleForm.handleRemark"
            type="textarea"
            placeholder="记录处理说明，仅管理端可见"
            :autosize="{ minRows: 5, maxRows: 8 }"
            maxlength="500"
            show-count
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="handleVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="submitHandle">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="contactVisible" preset="card" title="反馈联系设置" style="width: 680px">
      <n-form :model="contactForm" label-placement="left" label-width="100px">
        <n-form-item label="微信号">
          <n-input v-model:value="contactForm.wechatId" placeholder="请输入展示给用户的微信号" maxlength="80" show-count />
        </n-form-item>
        <n-form-item label="微信二维码">
          <div class="qrcode-setting">
            <div v-if="contactForm.qrcodeUrl" class="qrcode-preview">
              <n-image :src="contactForm.qrcodeUrl" width="120" height="120" object-fit="cover" />
              <n-button size="small" quaternary type="error" @click="contactForm.qrcodeUrl = ''">移除</n-button>
            </div>
            <n-upload :custom-request="handleQrcodeUpload" :show-file-list="false" accept="image/*">
              <n-button :loading="uploadLoading">
                <template #icon><n-icon><ImageOutline /></n-icon></template>
                上传二维码
              </n-button>
            </n-upload>
            <n-input v-model:value="contactForm.qrcodeUrl" placeholder="或直接填写图片 URL" />
          </div>
        </n-form-item>
        <n-form-item label="提示文案">
          <n-input
            v-model:value="contactForm.remark"
            type="textarea"
            placeholder="例如：也可以添加微信直接沟通"
            :autosize="{ minRows: 3, maxRows: 5 }"
            maxlength="120"
            show-count
          />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="contactVisible = false">取消</n-button>
          <n-button type="primary" :loading="contactSaving" @click="saveContact">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NIcon,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type SelectOption,
  type TagProps,
  type UploadCustomRequestOptions
} from 'naive-ui'
import {
  ChatbubbleOutline,
  CreateOutline,
  EyeOutline,
  ImageOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import { feedbackApi, type AppFeedback, type FeedbackContact, type FeedbackStatus, type FeedbackType } from '@/api/feedback'
import { fileApi } from '@/api/system'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const hasPermission = (permission: string) => userStore.hasPermission(permission)

const feedbackTypeOptions: Array<SelectOption & { value: FeedbackType }> = [
  { label: '想法建议', value: 1 },
  { label: 'Bug问题', value: 2 },
  { label: '内容错误', value: 3 },
  { label: '其他', value: 4 }
]

const statusOptions: Array<SelectOption & { value: FeedbackStatus }> = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已忽略', value: 3 }
]

const searchForm = reactive<{
  keyword: string
  feedbackType: FeedbackType | null
  status: FeedbackStatus | null
  timeRange: [string, string] | null
}>({
  keyword: '',
  feedbackType: null,
  status: null,
  timeRange: null
})

const tableData = ref<AppFeedback[]>([])
const loading = ref(false)
const selectedIds = ref<number[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const detailVisible = ref(false)
const detailData = ref<AppFeedback | null>(null)
const handleVisible = ref(false)
const submitLoading = ref(false)
const currentHandleId = ref<number | null>(null)
const handleForm = reactive<{
  status: FeedbackStatus
  handleRemark: string
}>({
  status: 1,
  handleRemark: ''
})

const contactVisible = ref(false)
const contactSaving = ref(false)
const uploadLoading = ref(false)
const contactForm = reactive<FeedbackContact>({
  wechatId: '',
  qrcodeUrl: '',
  remark: ''
})

const columns: DataTableColumns<AppFeedback> = [
  { type: 'selection' },
  {
    title: '类型',
    key: 'feedbackType',
    width: 110,
    render(row) {
      return h(NTag, { type: getTypeTagType(row.feedbackType), size: 'small' }, {
        default: () => getTypeLabel(row.feedbackType)
      })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 110,
    render(row) {
      return h(NTag, { type: getStatusTagType(row.status), size: 'small' }, {
        default: () => getStatusLabel(row.status)
      })
    }
  },
  { title: '内容摘要', key: 'contentPreview', minWidth: 460, ellipsis: { tooltip: true } },
  { title: '页面', key: 'pagePath', width: 180, ellipsis: { tooltip: true }, render: row => row.pagePath || '-' },
  { title: '提交时间', key: 'createTime', width: 170, render: row => row.createTime || '-' },
  { title: '处理人', key: 'handlerName', width: 120, render: row => row.handlerName || row.handlerId || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right',
    render(row) {
      const actions = [
        h(NButton, { size: 'small', quaternary: true, onClick: () => handleView(row) }, {
          default: () => [h(NIcon, null, { default: () => h(EyeOutline) }), ' 查看']
        })
      ]

      if (hasPermission('yun:feedback:handle')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => openHandle(row) }, {
          default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 处理']
        }))
      }

      if (hasPermission('yun:feedback:remove')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) }, {
          default: () => [h(NIcon, null, { default: () => h(TrashOutline) }), ' 删除']
        }))
      }

      return h('div', { class: 'action-buttons' }, actions)
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await feedbackApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      feedbackType: searchForm.feedbackType ?? undefined,
      status: searchForm.status ?? undefined,
      beginTime: searchForm.timeRange?.[0],
      endTime: searchForm.timeRange?.[1]
    })
    tableData.value = res.list
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.feedbackType = null
  searchForm.status = null
  searchForm.timeRange = null
  handleSearch()
}

function handlePageChange(page: number) {
  pagination.page = page
  loadData()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

function handleCheck(keys: Array<string | number>) {
  selectedIds.value = keys as number[]
}

async function handleView(row: AppFeedback) {
  detailData.value = await feedbackApi.detail(row.id!)
  detailVisible.value = true
}

async function openHandle(row: AppFeedback) {
  const detail = await feedbackApi.detail(row.id!)
  currentHandleId.value = detail.id!
  handleForm.status = detail.status ?? 1
  handleForm.handleRemark = detail.handleRemark || ''
  handleVisible.value = true
}

async function submitHandle() {
  if (!currentHandleId.value) return
  submitLoading.value = true
  try {
    await feedbackApi.updateStatus(currentHandleId.value, {
      status: handleForm.status,
      handleRemark: handleForm.handleRemark?.trim()
    })
    message.success('处理状态已更新')
    handleVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: AppFeedback) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该反馈吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await feedbackApi.delete([row.id!])
      message.success('删除成功')
      await loadData()
    }
  })
}

function handleBatchDelete() {
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedIds.value.length} 条反馈吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await feedbackApi.delete(selectedIds.value)
      message.success('删除成功')
      selectedIds.value = []
      await loadData()
    }
  })
}

async function openContactModal() {
  const contact = await feedbackApi.getContact()
  contactForm.wechatId = contact.wechatId || ''
  contactForm.qrcodeUrl = contact.qrcodeUrl || ''
  contactForm.remark = contact.remark || ''
  contactVisible.value = true
}

async function handleQrcodeUpload(options: UploadCustomRequestOptions) {
  const { file, onFinish, onError } = options
  uploadLoading.value = true
  try {
    const result = await fileApi.uploadImage(file.file as File)
    contactForm.qrcodeUrl = result.url
    message.success('二维码上传成功')
    onFinish()
  } catch (error) {
    message.error('二维码上传失败')
    onError()
  } finally {
    uploadLoading.value = false
  }
}

async function saveContact() {
  contactSaving.value = true
  try {
    await feedbackApi.updateContact({
      wechatId: contactForm.wechatId?.trim(),
      qrcodeUrl: contactForm.qrcodeUrl?.trim(),
      remark: contactForm.remark?.trim()
    })
    message.success('联系设置已保存')
    contactVisible.value = false
  } finally {
    contactSaving.value = false
  }
}

function getTypeLabel(type?: FeedbackType) {
  return feedbackTypeOptions.find(item => item.value === type)?.label || '想法建议'
}

function getStatusLabel(status?: FeedbackStatus) {
  return statusOptions.find(item => item.value === status)?.label || '待处理'
}

function getTypeTagType(type?: FeedbackType): TagProps['type'] {
  const map: Record<FeedbackType, TagProps['type']> = {
    1: 'info',
    2: 'error',
    3: 'warning',
    4: 'default'
  }
  return map[(type || 1) as FeedbackType]
}

function getStatusTagType(status?: FeedbackStatus): TagProps['type'] {
  const map: Record<FeedbackStatus, TagProps['type']> = {
    0: 'warning',
    1: 'info',
    2: 'success',
    3: 'default'
  }
  return map[(status ?? 0) as FeedbackStatus]
}

onMounted(loadData)
</script>

<style scoped>
.search-form,
.table-toolbar {
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #6b7280;
  font-size: 13px;
}

.detail-section + .detail-section {
  margin-top: 18px;
}

.detail-label {
  margin-bottom: 8px;
  color: #374151;
  font-weight: 600;
}

.detail-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  color: #1f2937;
}

.detail-code {
  padding: 12px;
  border-radius: 8px;
  background: #f9fafb;
  color: #4b5563;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
}

.qrcode-setting {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 420px;
}

.qrcode-preview {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}
</style>
