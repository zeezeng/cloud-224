<template>
  <div class="page-container">
    <n-card>
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="主播ID">
            <n-input v-model:value="searchForm.anchorId" placeholder="请输入主播ID" clearable />
          </n-form-item>
          <n-form-item label="主播名称">
            <n-input v-model:value="searchForm.anchorName" placeholder="请输入主播名称" clearable />
          </n-form-item>
          <n-form-item label="房间号">
            <n-input v-model:value="searchForm.roomId" placeholder="请输入房间号" clearable />
          </n-form-item>
          <n-form-item label="公会">
            <n-input v-model:value="searchForm.guildName" placeholder="请输入公会名称" clearable />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              clearable
              style="width: 130px"
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
          <n-button v-if="hasPermission('yun:anchor:add')" type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增主播
          </n-button>
          <n-button v-if="hasPermission('yun:anchor:add')" type="primary" secondary @click="handleBatchAdd">
            <template #icon><n-icon><CloudUploadOutline /></n-icon></template>
            批量新增
          </n-button>
          <n-select
            v-if="hasPermission('yun:anchor:sync')"
            v-model:value="toolbarSyncDataSource"
            :options="syncSourceOptions"
            style="width: 150px"
          />
          <n-button
            v-if="hasPermission('yun:anchor:sync')"
            type="info"
            :loading="syncAllLoading"
            @click="handleSyncAll"
          >
            <template #icon><n-icon><CloudDownloadOutline /></n-icon></template>
            同步全部
          </n-button>
          <n-button
            v-if="hasPermission('yun:anchor:remove')"
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
        remote
        :row-key="row => row.id"
        :scroll-x="1810"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        @update:checked-row-keys="handleCheck"
      />
    </n-card>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 900px">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="116px">
        <n-alert v-if="!formData.id" type="info" :bordered="false" class="fetch-tip">
          输入主播ID后可选择播酱或在看自动获取主播资料；也可以只填必填项后手动保存。
        </n-alert>
        <n-grid :cols="2" :x-gap="24">
          <n-form-item-gi label="主播ID" path="anchorId">
            <n-input-group>
              <n-input
                v-model:value="formData.anchorId"
                placeholder="必填，唯一；当前等同斗鱼房间号 rid"
                :disabled="!!formData.id"
              />
              <n-button
                v-if="!formData.id"
                type="primary"
                ghost
                :loading="previewLoading"
                @click="handleFetchPreview"
              >
                自动获取
              </n-button>
            </n-input-group>
          </n-form-item-gi>
          <n-form-item-gi label="数据源" path="dataSource">
            <n-select v-model:value="formData.dataSource" :options="dataSourceOptions" />
          </n-form-item-gi>
          <n-form-item-gi label="房间号" path="roomId">
            <n-input v-model:value="formData.roomId" placeholder="默认等于主播ID" />
          </n-form-item-gi>
          <n-form-item-gi label="主播名称" path="anchorName">
            <n-input v-model:value="formData.anchorName" placeholder="请输入主播名称" maxlength="100" />
          </n-form-item-gi>
          <n-form-item-gi label="头像地址" path="avatarUrl">
            <div class="avatar-form-field">
              <n-input v-model:value="formData.avatarUrl" placeholder="请输入头像 URL" />
              <div class="avatar-preview">
                <n-avatar
                  v-if="formData.avatarUrl"
                  :src="formData.avatarUrl"
                  round
                  :size="64"
                  fallback-src=""
                  class="avatar-preview-image"
                />
                <div v-else class="avatar-preview-empty">暂无头像</div>
                <div class="avatar-preview-text">头像预览</div>
              </div>
            </div>
          </n-form-item-gi>
          <n-form-item-gi label="公会编号" path="guildNo">
            <n-input v-model:value="formData.guildNo" placeholder="请输入公会编号" />
          </n-form-item-gi>
          <n-form-item-gi label="公会名称" path="guildName">
            <n-input v-model:value="formData.guildName" placeholder="请输入公会名称" />
          </n-form-item-gi>
          <n-form-item-gi label="分类ID" path="categoryId">
            <n-input v-model:value="formData.categoryId" placeholder="请输入分类ID" />
          </n-form-item-gi>
          <n-form-item-gi label="分类名称" path="categoryName">
            <n-input v-model:value="formData.categoryName" placeholder="请输入分类名称" />
          </n-form-item-gi>
          <n-form-item-gi label="状态" path="status">
            <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
              <template #checked>启用</template>
              <template #unchecked>禁用</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="榜单展示" path="showRank">
            <n-switch v-model:value="formData.showRank" :checked-value="1" :unchecked-value="0">
              <template #checked>展示</template>
              <template #unchecked>隐藏</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="自动更新资料" path="autoUpdateProfile">
            <n-switch v-model:value="formData.autoUpdateProfile" :checked-value="1" :unchecked-value="0">
              <template #checked>开启</template>
              <template #unchecked>关闭</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="排序" path="sort">
            <n-input-number v-model:value="formData.sort" :min="0" :step="1" style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="直播标题" path="roomTitle" :span="2">
            <n-input v-model:value="formData.roomTitle" placeholder="请输入直播间标题" maxlength="255" show-count />
          </n-form-item-gi>
          <n-form-item-gi label="主播简介" path="bio" :span="2">
            <n-input
              v-model:value="formData.bio"
              type="textarea"
              placeholder="请输入主播简介"
              :autosize="{ minRows: 2, maxRows: 4 }"
            />
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
      <template #footer>
        <n-space justify="end">
          <n-button @click="modalVisible = false">取消</n-button>
          <n-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal
      v-model:show="batchModalVisible"
      preset="card"
      title="批量新增主播"
      style="width: 640px"
      :mask-closable="!batchSubmitLoading"
      :close-on-esc="!batchSubmitLoading"
      :closable="!batchSubmitLoading"
    >
      <n-alert type="info" :bordered="false" class="batch-tip">
        每行输入一个主播ID，提交后会按所选数据源自动获取主播资料；系统会并发处理并显示实时进度。
      </n-alert>
      <n-form label-placement="left" label-width="96px" class="batch-source-form">
        <n-form-item label="创建数据源">
          <n-select v-model:value="batchDataSource" :options="createDataSourceOptions" :disabled="batchSubmitLoading" />
        </n-form-item>
      </n-form>
      <n-input
        v-model:value="batchAnchorText"
        type="textarea"
        placeholder="182102&#10;999999&#10;..."
        :autosize="{ minRows: 8, maxRows: 14 }"
        :disabled="batchSubmitLoading"
      />
      <div v-if="batchProgress.total > 0" class="batch-progress">
        <n-progress
          type="line"
          indicator-placement="inside"
          :percentage="batchProgressPercentage"
          :processing="batchSubmitLoading"
          :status="batchProgress.fail > 0 ? 'warning' : 'success'"
        />
        <div class="batch-progress-meta">
          <span>总数：{{ batchProgress.total }}</span>
          <span>完成：{{ batchProgress.completed }}</span>
          <span>成功：{{ batchProgress.success }}</span>
          <span>失败：{{ batchProgress.fail }}</span>
        </div>
        <div v-if="batchProcessingIds.length > 0" class="batch-current">
          正在处理：{{ batchProcessingIds.join('、') }}
        </div>
        <div v-if="batchProgress.errors.length > 0" class="batch-progress-errors">
          <div v-for="error in batchProgress.errors.slice(0, 5)" :key="error">{{ error }}</div>
          <div v-if="batchProgress.errors.length > 5" class="batch-result-more">
            还有 {{ batchProgress.errors.length - 5 }} 条失败记录未展示
          </div>
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button :disabled="batchSubmitLoading" @click="batchModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="batchSubmitLoading" @click="handleBatchSubmit">
            {{ batchSubmitLoading ? '添加中' : '开始添加' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NAvatar,
  NButton,
  NIcon,
  NSelect,
  NSpace,
  NTag,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules
} from 'naive-ui'
import {
  AddOutline,
  CloudDownloadOutline,
  CloudUploadOutline,
  CreateOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline
} from '@vicons/ionicons5'
import { anchorApi, type AnchorDataSource, type AnchorStatus, type YunAnchor, type YunAnchorPageRow, type YunSyncResult } from '@/api/anchor'
import { useUserStore } from '@/stores/user'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const hasPermission = (permission: string) => userStore.hasPermission(permission)
const BATCH_CREATE_CONCURRENCY = 3

const statusOptions: Array<{ label: string; value: AnchorStatus }> = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const dataSourceOptions: Array<{ label: string; value: AnchorDataSource }> = [
  { label: '手动维护', value: 'MANUAL' },
  { label: '播酱', value: 'BOJIANG' },
  { label: '在看', value: 'DOSEEING' }
]

const createDataSourceOptions = dataSourceOptions.filter(option => option.value !== 'MANUAL')

const syncSourceOptions: Array<{ label: string; value: string }> = [
  { label: '按主播数据源', value: '' },
  { label: '播酱', value: 'BOJIANG' },
  { label: '在看', value: 'DOSEEING' }
]

const searchForm = reactive<{
  anchorId: string
  anchorName: string
  roomId: string
  guildName: string
  status: AnchorStatus | null
}>({
  anchorId: '',
  anchorName: '',
  roomId: '',
  guildName: '',
  status: null
})

const tableData = ref<YunAnchorPageRow[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const previewLoading = ref(false)
const syncAllLoading = ref(false)
const batchSubmitLoading = ref(false)
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
const batchModalVisible = ref(false)
const batchAnchorText = ref('')
const batchDataSource = ref<AnchorDataSource>('BOJIANG')
const toolbarSyncDataSource = ref('')
const batchProcessingIds = ref<string[]>([])
const formRef = ref<FormInst | null>(null)
const defaultFormData: YunAnchor = {
  anchorId: '',
  roomId: '',
  anchorName: '',
  avatarUrl: '',
  roomTitle: '',
  categoryId: '',
  categoryName: '',
  guildNo: '',
  guildName: '',
  bio: '',
  status: 1,
  showRank: 1,
  sort: 0,
  dataSource: 'BOJIANG',
  autoUpdateProfile: 1,
  remark: ''
}
const formData = reactive<YunAnchor>({ ...defaultFormData })

const formRules: FormRules = {
  anchorId: { required: true, message: '请输入主播ID', trigger: ['blur', 'input'] },
}

const batchProgress = reactive({
  total: 0,
  completed: 0,
  success: 0,
  fail: 0,
  errors: [] as string[]
})

const batchProgressPercentage = computed(() => {
  if (batchProgress.total <= 0) {
    return 0
  }
  return Math.min(100, Math.round((batchProgress.completed / batchProgress.total) * 100))
})

const columns: DataTableColumns<YunAnchorPageRow> = [
  { type: 'selection' },
  {
    title: '主播',
    key: 'anchorName',
    minWidth: 210,
    render(row) {
      return h('div', { class: 'anchor-cell' }, [
        h(NAvatar, {
          src: row.avatarUrl || undefined,
          round: true,
          size: 40,
          fallbackSrc: '',
        }),
        h('div', { class: 'anchor-cell-main' }, [
          h('div', { class: 'anchor-name' }, row.anchorName || '-'),
          h('div', { class: 'anchor-sub' }, `ID：${row.anchorId || '-'}`),
        ]),
      ])
    }
  },
  { title: '房间号', key: 'roomId', width: 120, render: row => row.roomId || '-' },
  {
    title: '数据源',
    key: 'dataSource',
    width: 100,
    render(row) {
      return h(NTag, { size: 'small', type: row.dataSource === 'DOSEEING' ? 'success' : row.dataSource === 'BOJIANG' ? 'info' : 'default' }, { default: () => dataSourceLabel(row.dataSource) })
    }
  },
  { title: '公会', key: 'guildName', minWidth: 150, ellipsis: { tooltip: true }, render: row => row.guildName || '-' },
  { title: '分类', key: 'categoryName', width: 130, ellipsis: { tooltip: true }, render: row => row.categoryName || '-' },
  {
    title: '今日礼物',
    key: 'todayGiftValue',
    width: 130,
    render(row) {
      return formatMoney(row.todayGiftValue)
    }
  },
  {
    title: '昨日礼物',
    key: 'yesterdayGiftValue',
    width: 130,
    render(row) {
      return formatMoney(row.yesterdayGiftValue)
    }
  },
  {
    title: '本月礼物',
    key: 'monthGiftValue',
    width: 130,
    render(row) {
      return formatMoney(row.monthGiftValue)
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render(row) {
      return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? '启用' : '禁用' })
    }
  },
  {
    title: '榜单',
    key: 'showRank',
    width: 90,
    render(row) {
      return h(NTag, { type: row.showRank === 1 ? 'info' : 'default', size: 'small' }, { default: () => row.showRank === 1 ? '展示' : '隐藏' })
    }
  },
  { title: '最近同步', key: 'lastGiftSyncTime', width: 170, render: row => row.lastGiftSyncTime || '-' },
  { title: '更新时间', key: 'updateTime', width: 170, render: row => row.updateTime || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 220,
    fixed: 'right',
    render(row) {
      const actions = []
      if (hasPermission('yun:anchor:sync')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, type: 'info', onClick: () => handleSync(row) }, {
          default: () => [h(NIcon, null, { default: () => h(CloudDownloadOutline) }), ' 同步']
        }))
      }
      if (hasPermission('yun:anchor:edit')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, onClick: () => handleEdit(row) }, {
          default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑']
        }))
      }
      if (hasPermission('yun:anchor:remove')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) }, {
          default: () => [h(NIcon, null, { default: () => h(TrashOutline) }), ' 删除']
        }))
      }
      return actions.length ? h('div', { class: 'action-buttons' }, actions) : '-'
    }
  }
]

function formatMoney(value: unknown) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'
}

function dataSourceLabel(value?: string) {
  if (value === '') {
    return '按主播数据源'
  }
  return dataSourceOptions.find(option => option.value === value)?.label || value || '手动维护'
}

async function loadData() {
  loading.value = true
  try {
    const res = await anchorApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      anchorId: searchForm.anchorId || undefined,
      anchorName: searchForm.anchorName || undefined,
      roomId: searchForm.roomId || undefined,
      guildName: searchForm.guildName || undefined,
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
  searchForm.anchorId = ''
  searchForm.anchorName = ''
  searchForm.roomId = ''
  searchForm.guildName = ''
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
  if (!formData.dataSource || formData.dataSource === 'MANUAL') {
    formData.dataSource = 'BOJIANG'
  }
  formRef.value?.restoreValidation()
}

function handleAdd() {
  modalTitle.value = '新增主播'
  resetForm()
  modalVisible.value = true
}

function handleBatchAdd() {
  batchAnchorText.value = ''
  batchDataSource.value = 'BOJIANG'
  resetBatchProgress()
  batchModalVisible.value = true
}

async function handleEdit(row: YunAnchorPageRow) {
  modalTitle.value = '编辑主播'
  resetForm()
  const detail = await anchorApi.detail(row.id!)
  Object.assign(formData, detail)
  modalVisible.value = true
}

async function handleFetchPreview() {
  const anchorId = (formData.anchorId || '').trim()
  if (!anchorId) {
    message.warning('请先输入主播ID')
    return
  }
  previewLoading.value = true
  try {
    const preview = await anchorApi.fetchPreview(anchorId, formData.dataSource === 'MANUAL' ? 'BOJIANG' : formData.dataSource)
    Object.assign(formData, {
      ...formData,
      ...preview,
      anchorId,
      status: formData.status ?? 1,
      showRank: formData.showRank ?? 1,
      sort: formData.sort ?? 0,
      autoUpdateProfile: formData.autoUpdateProfile ?? 1,
    })
    message.success(`已获取${dataSourceLabel(formData.dataSource)}资料`)

  } finally {
    previewLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    const submitData: YunAnchor = {
      ...formData,
      anchorId: formData.anchorId?.trim(),
      roomId: formData.roomId?.trim() || formData.anchorId?.trim(),
    }
    if (submitData.id) {
      await anchorApi.update(submitData)
      message.success('修改成功')
    } else {
      await anchorApi.create(submitData)
      message.success('新增成功')
    }
    modalVisible.value = false
    await loadData()
  } finally {
    submitLoading.value = false
  }
}

function parseBatchAnchorIds() {
  return Array.from(new Set(
    batchAnchorText.value
      .split(/\r?\n/)
      .map(item => item.trim())
      .filter(Boolean)
  ))
}

function resetBatchProgress(total = 0) {
  batchProgress.total = total
  batchProgress.completed = 0
  batchProgress.success = 0
  batchProgress.fail = 0
  batchProgress.errors = []
  batchProcessingIds.value = []
}

function pushBatchError(anchorId: string, error: unknown) {
  const message = error instanceof Error ? error.message : '请求失败'
  batchProgress.errors.push(`${anchorId}：${message}`)
}

async function handleBatchSubmit() {
  const anchorIds = parseBatchAnchorIds()
  if (anchorIds.length === 0) {
    message.warning('请输入主播ID')
    return
  }
  resetBatchProgress(anchorIds.length)
  batchSubmitLoading.value = true
  try {
    let nextIndex = 0
    const workerCount = Math.min(BATCH_CREATE_CONCURRENCY, anchorIds.length)
    const runWorker = async () => {
      while (nextIndex < anchorIds.length) {
        const anchorId = anchorIds[nextIndex]
        nextIndex += 1
        batchProcessingIds.value = [...batchProcessingIds.value, anchorId]
        try {
          const result = await anchorApi.batchCreate([anchorId], batchDataSource.value)

          batchProgress.success += result.successCount || 0
          batchProgress.fail += result.failCount || 0
          batchProgress.errors.push(...(result.errors || []))
        } catch (error) {
          batchProgress.fail += 1
          pushBatchError(anchorId, error)
        } finally {
          batchProgress.completed += 1
          batchProcessingIds.value = batchProcessingIds.value.filter(id => id !== anchorId)
        }
      }
    }

    await Promise.all(Array.from({ length: workerCount }, () => runWorker()))

    if (batchProgress.fail > 0) {
      message.warning(`批量新增完成：成功 ${batchProgress.success}，失败 ${batchProgress.fail}`)
      dialog.warning({
        title: '批量新增结果',
        content: () => h('div', { class: 'batch-result' }, [
          h('div', `成功：${batchProgress.success}/${batchProgress.total}`),
          h('div', { class: 'batch-result-errors' }, batchProgress.errors.slice(0, 10).map(error => h('div', error))),
          batchProgress.errors.length > 10 ? h('div', { class: 'batch-result-more' }, `还有 ${batchProgress.errors.length - 10} 条失败记录未展示`) : null,
        ]),
        positiveText: '知道了'
      })
    } else {
      message.success(`批量新增成功：${batchProgress.success}/${batchProgress.total}`)
      batchModalVisible.value = false
    }
    await loadData()
  } finally {
    batchSubmitLoading.value = false
  }
}

function renderSyncMessage(result: YunSyncResult) {
  if (result.failCount > 0) {
    message.warning(`同步完成：成功 ${result.successCount}，失败 ${result.failCount}`)
    return
  }
  message.success(`同步成功：${result.successCount}/${result.totalCount}`)
}

async function handleSync(row: YunAnchorPageRow) {
  const result = await anchorApi.sync(row.id!)
  renderSyncMessage(result)
  await loadData()
}

function handleSyncAll() {
  dialog.warning({
    title: '同步全部主播',
    content: `将同步所有启用主播的今日、昨日和本月礼物数据（数据源：${dataSourceLabel(toolbarSyncDataSource.value)}），确认继续吗？`,
    positiveText: '开始同步',
    negativeText: '取消',
    onPositiveClick: async () => {
      syncAllLoading.value = true
      try {
        const result = await anchorApi.syncAll(toolbarSyncDataSource.value || undefined)
        renderSyncMessage(result)
        await loadData()
      } finally {
        syncAllLoading.value = false
      }
    }
  })
}

function handleDelete(row: YunAnchorPageRow) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该主播吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await anchorApi.delete([row.id!])
      message.success('删除成功')
      await loadData()
    }
  })
}

function handleBatchDelete() {
  dialog.warning({
    title: '提示',
    content: `确定要删除选中的 ${selectedIds.value.length} 位主播吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await anchorApi.delete(selectedIds.value)
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

.fetch-tip {
  margin-bottom: 16px;
}

.batch-tip {
  margin-bottom: 12px;
}

.batch-source-form {
  margin-bottom: 12px;
}

.sync-dialog-desc {
  margin-bottom: 12px;
}

.batch-result {
  line-height: 1.7;
}

.batch-progress {
  margin-top: 16px;
}

.batch-progress-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  margin-top: 10px;
  color: #606266;
  font-size: 13px;
}

.batch-current {
  margin-top: 8px;
  color: #2080f0;
  font-size: 13px;
  word-break: break-all;
}

.batch-progress-errors {
  margin-top: 10px;
  padding: 8px 10px;
  border-radius: 4px;
  background: #fff2f0;
  color: #d03050;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
}

.batch-result-errors {
  margin-top: 8px;
  color: #d03050;
  word-break: break-all;
}

.batch-result-more {
  margin-top: 8px;
  color: #8a8f99;
}

.avatar-form-field {
  width: 100%;
}

.avatar-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 10px;
  min-height: 64px;
}

.avatar-preview-image {
  flex: 0 0 auto;
  border: 1px solid #e5e7eb;
}

.avatar-preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  flex: 0 0 auto;
  border: 1px dashed #d4d7de;
  border-radius: 50%;
  color: #8a8f99;
  font-size: 12px;
  background: #fafafa;
}

.avatar-preview-text {
  color: #8a8f99;
  font-size: 12px;
}

.anchor-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.anchor-cell-main {
  min-width: 0;
}

.anchor-name {
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.anchor-sub {
  margin-top: 4px;
  color: #8a8f99;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
}
</style>
