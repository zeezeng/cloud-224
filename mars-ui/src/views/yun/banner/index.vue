<template>
  <div class="page-container">
    <n-card>
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="标题">
            <n-input v-model:value="searchForm.title" placeholder="请输入标题" clearable />
          </n-form-item>
          <n-form-item label="跳转类型">
            <n-select
              v-model:value="searchForm.jumpType"
              placeholder="请选择跳转类型"
              clearable
              style="width: 160px"
              :options="jumpTypeOptions"
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
          <n-button v-if="hasPermission('yun:banner:add')" type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增
          </n-button>
          <n-button
            v-if="hasPermission('yun:banner:remove')"
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
        :scroll-x="1200"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        @update:checked-row-keys="handleCheck"
      />
    </n-card>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 760px">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="110px">
        <n-grid :cols="2" :x-gap="24">
          <n-form-item-gi label="标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入标题" maxlength="80" show-count />
          </n-form-item-gi>
          <n-form-item-gi label="状态" path="status">
            <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
              <template #checked>启用</template>
              <template #unchecked>禁用</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="描述" path="description" :span="2">
            <n-input v-model:value="formData.description" placeholder="请输入描述" maxlength="200" show-count />
          </n-form-item-gi>
          <n-form-item-gi label="轮播图片" path="imageUrl" :span="2">
            <BannerCropUpload v-model="formData.imageUrl" />
          </n-form-item-gi>
          <n-form-item-gi label="跳转类型" path="jumpType">
            <n-select v-model:value="formData.jumpType" :options="jumpTypeOptions" />
          </n-form-item-gi>
          <n-form-item-gi label="排序" path="sort">
            <n-input-number v-model:value="formData.sort" :min="0" :step="1" style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi v-if="formData.jumpType === 1" label="小程序页面" path="jumpTarget" :span="2">
            <n-select
              v-model:value="formData.jumpTarget"
              filterable
              tag
              clearable
              placeholder="选择预设页面，或输入 /pages/... 自定义路径"
              :options="pagePathOptions"
            />
          </n-form-item-gi>
          <n-form-item-gi v-if="formData.jumpType === 2" label="网页 URL" path="jumpTarget" :span="2">
            <n-input v-model:value="formData.jumpTarget" placeholder="请输入 http:// 或 https:// 开头的网页地址" />
          </n-form-item-gi>
          <n-form-item-gi label="备注" path="remark" :span="2">
            <n-input
              v-model:value="formData.remark"
              type="textarea"
              placeholder="请输入备注"
              :autosize="{ minRows: 2, maxRows: 4 }"
            />
          </n-form-item-gi>
        </n-grid>
      </n-form>
      <n-alert v-if="formData.jumpType === 2" type="info" class="webview-tip" :bordered="false">
        微信小程序打开网页 URL 时，会进入小程序 web-view 页面，目标域名需要先在微信后台配置业务域名。
      </n-alert>
      <template #footer>
        <n-space justify="end">
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import {
  NButton,
  NIcon,
  NImage,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules
} from 'naive-ui'
import { AddOutline, CreateOutline, RefreshOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
import { bannerApi, type AppBanner, type BannerJumpType, type BannerStatus } from '@/api/banner'
import BannerCropUpload from '@/components/BannerCropUpload.vue'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const hasPermission = (permission: string) => userStore.hasPermission(permission)

const jumpTypeOptions: Array<{ label: string; value: BannerJumpType }> = [
  { label: '不跳转', value: 0 },
  { label: '小程序页面', value: 1 },
  { label: '网页 URL', value: 2 }
]

const statusOptions: Array<{ label: string; value: BannerStatus }> = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const pagePathOptions = [
  { label: '首页 /pages/index/index', value: '/pages/index/index' },
  { label: '主播 /pages/anchors/anchors', value: '/pages/anchors/anchors' },
  { label: '排行 /pages/ranking/ranking', value: '/pages/ranking/ranking' },
  { label: '乐享 /pages/enjoy/enjoy', value: '/pages/enjoy/enjoy' },
  { label: '金库 /pages/vault/vault', value: '/pages/vault/vault' },
  { label: '赛季 /pages/season/season', value: '/pages/season/season' }
]

const searchForm = reactive<{
  title: string
  jumpType: BannerJumpType | null
  status: BannerStatus | null
}>({
  title: '',
  jumpType: null,
  status: null
})

const tableData = ref<AppBanner[]>([])
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
const defaultFormData: AppBanner = {
  title: '',
  description: '',
  imageUrl: '',
  jumpType: 0,
  jumpTarget: '',
  sort: 0,
  status: 1,
  remark: ''
}
const formData = reactive<AppBanner>({ ...defaultFormData })

const formRules: FormRules = {
  imageUrl: { required: true, message: '请上传轮播图片', trigger: ['change', 'blur'] },
  jumpTarget: {
    trigger: ['change', 'blur'],
    validator() {
      if (formData.jumpType === 0) {
        return true
      }
      const target = (formData.jumpTarget || '').trim()
      if (!target) {
        return new Error('请输入跳转目标')
      }
      if (formData.jumpType === 1 && !/^\/?pages\/.+/.test(target)) {
        return new Error('小程序页面路径必须以 /pages/ 或 pages/ 开头')
      }
      if (formData.jumpType === 2 && !/^https?:\/\//.test(target)) {
        return new Error('网页 URL 必须以 http:// 或 https:// 开头')
      }
      return true
    }
  }
}

const jumpTypeMap: Record<BannerJumpType, { text: string; type: 'default' | 'info' | 'success' | 'warning' }> = {
  0: { text: '不跳转', type: 'default' },
  1: { text: '小程序页面', type: 'success' },
  2: { text: '网页 URL', type: 'info' }
}

const columns: DataTableColumns<AppBanner> = [
  { type: 'selection' },
  {
    title: '图片',
    key: 'imageUrl',
    width: 132,
    render(row) {
      return row.imageUrl
        ? h(NImage, {
            src: row.imageUrl,
            width: 126,
            height: 63,
            objectFit: 'cover',
            style: { borderRadius: '6px' }
          })
        : '-'
    }
  },
  { title: '标题', key: 'title', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '跳转类型',
    key: 'jumpType',
    width: 120,
    render(row) {
      const config = jumpTypeMap[(row.jumpType ?? 0) as BannerJumpType]
      return h(NTag, { type: config.type, size: 'small' }, { default: () => config.text })
    }
  },
  { title: '跳转目标', key: 'jumpTarget', minWidth: 220, ellipsis: { tooltip: true }, render: row => row.jumpTarget || '-' },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render(row) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? '启用' : '禁用' })
    }
  },
  { title: '更新时间', key: 'updateTime', width: 170 },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    fixed: 'right',
    render(row) {
      const actions = []
      if (hasPermission('yun:banner:edit')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, onClick: () => handleEdit(row) }, {
          default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑']
        }))
      }
      if (hasPermission('yun:banner:remove')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) }, {
          default: () => [h(NIcon, null, { default: () => h(TrashOutline) }), ' 删除']
        }))
      }
      return actions.length ? h('div', { class: 'action-buttons' }, actions) : '-'
    }
  }
]

async function loadData() {
  loading.value = true
  try {
    const res = await bannerApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined,
      jumpType: searchForm.jumpType ?? undefined,
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
  searchForm.jumpType = null
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
  modalTitle.value = '新增轮播图'
  resetForm()
  modalVisible.value = true
}

async function handleEdit(row: AppBanner) {
  modalTitle.value = '编辑轮播图'
  resetForm()
  const detail = await bannerApi.detail(row.id!)
  Object.assign(formData, detail)
  modalVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const submitData: AppBanner = {
      ...formData,
      jumpTarget: formData.jumpType === 0 ? '' : (formData.jumpTarget || '').trim()
    }
    if (submitData.id) {
      await bannerApi.update(submitData)
      message.success('修改成功')
    } else {
      await bannerApi.create(submitData)
      message.success('新增成功')
    }
    modalVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

function handleDelete(row: AppBanner) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该轮播图吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await bannerApi.delete([row.id!])
      message.success('删除成功')
      await loadData()
    }
  })
}

function handleBatchDelete() {
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedIds.value.length} 张轮播图吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await bannerApi.delete(selectedIds.value)
      message.success('删除成功')
      selectedIds.value = []
      await loadData()
    }
  })
}

watch(() => formData.jumpType, (jumpType) => {
  if (jumpType === 0) {
    formData.jumpTarget = ''
  }
})

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

.webview-tip {
  margin-top: 12px;
}
</style>
