<template>
  <div class="page-container">
    <n-card>
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="标题">
            <n-input v-model:value="searchForm.title" placeholder="请输入公告标题" clearable />
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
          <n-button v-if="hasPermission('yun:notice:add')" type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增公告
          </n-button>
          <n-button
            v-if="hasPermission('yun:notice:remove')"
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
        :scroll-x="1180"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        @update:checked-row-keys="handleCheck"
      />
    </n-card>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 760px">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="92px">
        <n-grid :cols="2" :x-gap="24">
          <n-form-item-gi label="公告标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入公告标题" maxlength="120" show-count />
          </n-form-item-gi>
          <n-form-item-gi label="状态" path="status">
            <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
              <template #checked>发布</template>
              <template #unchecked>下线</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="排序" path="sort">
            <n-input-number v-model:value="formData.sort" :min="0" :step="1" style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="备注" path="remark">
            <n-input v-model:value="formData.remark" placeholder="请输入备注" maxlength="200" show-count />
          </n-form-item-gi>
          <n-form-item-gi label="公告内容" path="content" :span="2">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请输入公告内容"
              :autosize="{ minRows: 8, maxRows: 12 }"
              maxlength="2000"
              show-count
            />
          </n-form-item-gi>
        </n-grid>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="detailVisible" preset="card" title="公告详情" style="width: 700px">
      <div v-if="detailData" class="notice-detail">
        <div class="notice-detail-header">
          <div class="notice-detail-title">{{ detailData.title }}</div>
          <n-tag :type="detailData.status === 1 ? 'success' : 'default'" size="small">
            {{ detailData.status === 1 ? '已发布' : '已下线' }}
          </n-tag>
        </div>
        <div class="notice-detail-meta">
          <span>排序：{{ detailData.sort ?? 0 }}</span>
          <span>发布时间：{{ detailData.publishedAt || '-' }}</span>
          <span>更新时间：{{ detailData.updateTime || '-' }}</span>
        </div>
        <n-divider />
        <div class="notice-detail-content">{{ detailData.content }}</div>
      </div>
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
  type FormInst,
  type FormRules
} from 'naive-ui'
import { AddOutline, CreateOutline, EyeOutline, RefreshOutline, SearchOutline, StopCircleOutline, TrashOutline, VolumeHighOutline } from '@vicons/ionicons5'
import { noticeApi, type AppNotice, type NoticeStatus } from '@/api/notice'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const hasPermission = (permission: string) => userStore.hasPermission(permission)

const statusOptions: Array<{ label: string; value: NoticeStatus }> = [
  { label: '下线', value: 0 },
  { label: '发布', value: 1 }
]

const searchForm = reactive<{
  title: string
  status: NoticeStatus | null
}>({
  title: '',
  status: null
})

const tableData = ref<AppNotice[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const selectedIds = ref<number[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const modalVisible = ref(false)
const modalTitle = ref('')
const formRef = ref<FormInst | null>(null)
const defaultFormData: AppNotice = {
  title: '',
  content: '',
  sort: 0,
  status: 0,
  remark: ''
}
const formData = reactive<AppNotice>({ ...defaultFormData })

const detailVisible = ref(false)
const detailData = ref<AppNotice | null>(null)

const formRules: FormRules = {
  title: { required: true, message: '请输入公告标题', trigger: ['blur', 'input'] },
  content: { required: true, message: '请输入公告内容', trigger: ['blur', 'input'] }
}

const columns: DataTableColumns<AppNotice> = [
  { type: 'selection' },
  { title: '标题', key: 'title', minWidth: 180, ellipsis: { tooltip: true } },
  { title: '内容预览', key: 'contentPreview', minWidth: 320, ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render(row) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, {
        default: () => (row.status === 1 ? '已发布' : '已下线')
      })
    }
  },
  { title: '发布时间', key: 'publishedAt', width: 170, render: row => row.publishedAt || '-' },
  { title: '更新时间', key: 'updateTime', width: 170, render: row => row.updateTime || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 320,
    fixed: 'right',
    render(row) {
      const actions = [
        h(NButton, { size: 'small', quaternary: true, onClick: () => handleView(row) }, {
          default: () => [h(NIcon, null, { default: () => h(EyeOutline) }), ' 查看']
        })
      ]

      if (hasPermission('yun:notice:edit')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, onClick: () => handleEdit(row) }, {
          default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑']
        }))

        if (row.status === 1) {
          actions.push(h(NButton, { size: 'small', quaternary: true, type: 'warning', onClick: () => handleOffline(row) }, {
            default: () => [h(NIcon, null, { default: () => h(StopCircleOutline) }), ' 下线']
          }))
        } else {
          actions.push(h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => handlePublish(row) }, {
            default: () => [h(NIcon, null, { default: () => h(VolumeHighOutline) }), ' 发布']
          }))
        }
      }

      if (hasPermission('yun:notice:remove')) {
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
    const res = await noticeApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined,
      status: searchForm.status ?? undefined
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
  searchForm.title = ''
  searchForm.status = null
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

function resetForm() {
  Object.assign(formData, { ...defaultFormData })
  formRef.value?.restoreValidation()
}

function handleAdd() {
  modalTitle.value = '新增公告'
  resetForm()
  modalVisible.value = true
}

async function handleEdit(row: AppNotice) {
  modalTitle.value = '编辑公告'
  resetForm()
  const detail = await noticeApi.detail(row.id!)
  Object.assign(formData, detail)
  modalVisible.value = true
}

async function handleView(row: AppNotice) {
  detailData.value = await noticeApi.detail(row.id!)
  detailVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const submitData: AppNotice = {
      ...formData,
      title: formData.title?.trim(),
      content: formData.content?.trim(),
      remark: formData.remark?.trim()
    }
    if (submitData.id) {
      await noticeApi.update(submitData)
      message.success('修改成功')
    } else {
      await noticeApi.create(submitData)
      message.success('新增成功')
    }
    modalVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

function handlePublish(row: AppNotice) {
  dialog.warning({
    title: '提示',
    content: '确定要发布该公告吗？发布后会展示在客户端首页。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await noticeApi.publish(row.id!)
      message.success('发布成功')
      await loadData()
    }
  })
}

function handleOffline(row: AppNotice) {
  dialog.warning({
    title: '提示',
    content: '确定要下线该公告吗？下线后客户端首页将不再展示。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await noticeApi.offline(row.id!)
      message.success('下线成功')
      await loadData()
    }
  })
}

function handleDelete(row: AppNotice) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该公告吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await noticeApi.delete([row.id!])
      message.success('删除成功')
      await loadData()
    }
  })
}

function handleBatchDelete() {
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedIds.value.length} 条公告吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await noticeApi.delete(selectedIds.value)
      message.success('删除成功')
      selectedIds.value = []
      await loadData()
    }
  })
}

onMounted(loadData)
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}

.table-toolbar {
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

.notice-detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.notice-detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.notice-detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 12px;
  color: #6b7280;
  font-size: 13px;
}

.notice-detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #1f2937;
}
</style>
