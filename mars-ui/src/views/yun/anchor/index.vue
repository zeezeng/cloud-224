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
        <div class="table-toolbar-actions">
          <n-space class="table-toolbar-primary">
            <n-button v-if="hasPermission('yun:anchor:add')" type="primary" @click="handleAdd">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              新增主播
            </n-button>
            <n-button v-if="hasPermission('yun:anchor:add')" type="primary" secondary @click="handleBatchAdd">
              <template #icon><n-icon><CloudUploadOutline /></n-icon></template>
              批量新增
            </n-button>
            <n-button
              v-if="hasPermission('yun:anchor:sync')"
              type="info"
              :loading="syncAllLoading"
              :disabled="syncAllTask.running"
              @click="handleSyncAll"
            >
              <template #icon><n-icon><CloudDownloadOutline /></n-icon></template>
              {{ syncAllTask.running ? '同步中' : '同步全部' }}
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
            <div
              v-if="hasPermission('yun:anchor:sync') || hasPermission('yun:anchor:add')"
              class="sync-platform-meta"
            >
              <span class="sync-platform-meta-label">当前同步来源</span>
              <template v-if="syncSourceOptions.length > 1">
                <n-select
                  v-model:value="toolbarSyncDataSource"
                  :options="syncSourceOptions"
                  class="sync-platform-meta-select"
                />
              </template>
              <span v-else class="sync-platform-meta-value">{{ dataSourceLabel(toolbarSyncDataSource) }}</span>
            </div>
            <n-tooltip>
              <template #trigger>
                <n-button quaternary circle @click="handleOpenCookieConfig">
                  <template #icon><n-icon><SettingsOutline /></n-icon></template>
                </n-button>
              </template>
              在看会员Cookie配置
            </n-tooltip>
          </n-space>
        </div>
      </div>

      <n-data-table
        size="small"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        remote
        :row-key="row => row.id"
        :scroll-x="2200"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
        @update:checked-row-keys="handleCheck"
      />
    </n-card>

    <n-modal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 900px">
      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="116px">
        <n-alert v-if="!formData.id" type="info" :bordered="false" class="fetch-tip">
          输入主播ID后点击「自动获取」可按工具栏所选同步平台获取主播资料；也可以只填必填项后手动保存。
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
        每行输入一个主播ID，提交后会按工具栏所选同步平台自动获取主播资料；系统会并发处理并显示实时进度。
      </n-alert>
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

    <n-modal
      v-model:show="syncAllModalVisible"
      preset="card"
      title="同步全部主播"
      style="width: 460px"
      :mask-closable="false"
      :close-on-esc="false"
      :closable="!syncAllTask.running"
    >
      <div class="sync-all-modal-body">
        <div class="sync-all-modal-header">
          <n-spin v-if="syncAllTask.running" size="small" />
          <span class="sync-all-modal-status" :class="{ 'is-running': syncAllTask.running }">{{ syncAllModalStatus }}</span>
        </div>
        <n-progress
          type="line"
          :percentage="syncAllPercent"
          indicator-placement="inside"
          :height="22"
          :status="syncAllTask.failCount > 0 ? 'warning' : 'success'"
          :processing="syncAllTask.running"
        />
        <div class="sync-all-modal-stats">
          <div class="sync-all-modal-stat">
            <span class="sync-all-modal-stat-label">进度</span>
            <span class="sync-all-modal-stat-value">{{ syncAllTask.completedCount }} / {{ syncAllTask.totalCount }}</span>
          </div>
          <div class="sync-all-modal-stat">
            <span class="sync-all-modal-stat-label">成功</span>
            <span class="sync-all-modal-stat-value sync-all-modal-stat-success">{{ syncAllTask.successCount }}</span>
          </div>
          <div class="sync-all-modal-stat">
            <span class="sync-all-modal-stat-label">失败</span>
            <span class="sync-all-modal-stat-value sync-all-modal-stat-fail">{{ syncAllTask.failCount }}</span>
          </div>
        </div>
        <div v-if="syncAllTask.currentAnchorId && syncAllTask.running" class="sync-all-modal-current">
          正在同步：{{ syncAllTask.currentAnchorId }}
        </div>
        <div v-if="!syncAllTask.running && syncAllTask.errors.length > 0" class="sync-all-modal-errors">
          <div v-for="error in syncAllTask.errors.slice(0, 5)" :key="error">{{ error }}</div>
          <div v-if="syncAllTask.errors.length > 5" class="sync-all-modal-errors-more">
            还有 {{ syncAllTask.errors.length - 5 }} 条失败记录
          </div>
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button v-if="syncAllTask.running" type="error" @click="handleCancelSyncAll">取消同步</n-button>
          <n-button v-else type="primary" @click="syncAllModalVisible = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="cookieModalVisible" preset="card" title="在看会员Cookie配置" style="width: 600px">
      <n-alert type="info" :bordered="false" style="margin-bottom: 12px">
        在看网站本月数据需要会员权限。请登录 doseeeing.com 后，按 F12 → Network → 找任意请求 → 复制 Cookie 头的值粘贴到下方。留空则使用未登录的公开接口。
      </n-alert>
      <n-input
        v-model:value="cookieValue"
        type="textarea"
        :rows="6"
        placeholder="粘贴在看网站的Cookie值..."
        :disabled="cookieSaving"
      />
      <div style="margin-top: 12px">
        <div style="margin-bottom: 6px; font-size: 13px; color: #666">在看访问代理地址（可选）</div>
        <n-input
          v-model:value="proxyValue"
          type="text"
          placeholder="留空则直连在看；当本机/服务器 IP 被在看封禁时，填写阿里云函数代理地址（如 https://xxx.cn-shanghai.fcapp.run）"
          :disabled="cookieSaving"
        />
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="cookieModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="cookieSaving" @click="handleSaveCookie">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <div v-if="syncingRowIds.length > 0" class="fullscreen-loading">
      <n-spin size="large" />
      <p class="fullscreen-loading-text">正在同步主播数据…</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, onUnmounted, reactive, ref } from 'vue'
import {
  NAvatar,
  NButton,
  NIcon,
  NSelect,
  NSpace,
  NSwitch,
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
  SettingsOutline,
  TrashOutline
} from '@vicons/ionicons5'
import { anchorApi, type AnchorDataSource, type AnchorFlag, type AnchorStatus, type YunAnchor, type YunAnchorPageRow, type YunSyncProgress, type YunSyncResult } from '@/api/anchor'
import { configGroupApi } from '@/api/org'
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
  { label: '在看', value: 'DOSEEING' }
]

const syncSourceOptions: Array<{ label: string; value: string }> = [
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

const syncingRowIds = ref<number[]>([])
const syncAllTask = reactive<{
  taskId: string
  running: boolean
  totalCount: number
  completedCount: number
  successCount: number
  failCount: number
  currentAnchorId: string
  errors: string[]
}>({
  taskId: '',
  running: false,
  totalCount: 0,
  completedCount: 0,
  successCount: 0,
  failCount: 0,
  currentAnchorId: '',
  errors: []
})
let syncPollTimer: number | null = null

const syncAllPercent = computed(() => {
  if (!syncAllTask.totalCount) {
    return 0
  }
  return Math.min(100, Math.round((syncAllTask.completedCount / syncAllTask.totalCount) * 100))
})

const syncAllModalStatus = computed(() => {
  if (syncAllTask.running) {
    return '同步进行中…'
  }
  if (syncAllTask.failCount > 0) {
    return '同步完成（部分失败）'
  }
  return '同步完成'
})

const modalVisible = ref(false)
const modalTitle = ref('')
const batchModalVisible = ref(false)
const batchAnchorText = ref('')
const syncAllModalVisible = ref(false)
const cookieModalVisible = ref(false)
const cookieValue = ref('')
const proxyValue = ref('')
const cookieSaving = ref(false)
const toolbarSyncDataSource = ref<AnchorDataSource>('DOSEEING')
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
  dataSource: 'DOSEEING',
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
const statusUpdatingIds = ref<number[]>([])
const showRankUpdatingIds = ref<number[]>([])

const batchProgressPercentage = computed(() => {
  if (batchProgress.total <= 0) {
    return 0
  }
  return Math.min(100, Math.round((batchProgress.completed / batchProgress.total) * 100))
})

function updateRowLoading(list: { value: number[] }, id: number, loading: boolean) {
  list.value = loading
    ? Array.from(new Set([...list.value, id]))
    : list.value.filter(item => item !== id)
}

function isRowLoading(list: { value: number[] }, id?: number) {
  return !!id && list.value.includes(id)
}

function refreshAnchorRows() {
  void loadData().catch((error) => {
    console.error('主播列表刷新失败', error)
  })
}

const columns: DataTableColumns<YunAnchorPageRow> = [
  { type: 'selection' },
  {
    title: '主播',
    key: 'anchorName',
    width: 128,
    render(row) {
      return h('div', { class: 'anchor-cell' }, [
        h(NAvatar, {
          src: row.avatarUrl || undefined,
          round: true,
          size: 32,
          fallbackSrc: '',
        }),
        h('div', { class: 'anchor-cell-main' }, [
          h('div', { class: 'anchor-name' }, row.anchorName || '-'),
          h('div', { class: 'anchor-sub' }, `ID：${row.anchorId || '-'}`),
        ]),
      ])
    }
  },
  { title: '房间号', key: 'roomId', width: 68, render: row => row.roomId || '-' },
  { title: '公会', key: 'guildName', width: 72, ellipsis: { tooltip: true }, render: row => row.guildName || '-' },
  { title: '分类', key: 'categoryName', width: 72, ellipsis: { tooltip: true }, render: row => row.categoryName || '-' },
  { title: '今日SR值', key: 'todaySrValue', width: 88, render: row => formatMoney(row.todaySrValue) },
  { title: '今日SR人数', key: 'todaySrUserCount', width: 88, render: row => formatInt(row.todaySrUserCount) },
  { title: '今日LW值', key: 'todayLwValue', width: 88, render: row => formatMoney(row.todayLwValue) },
  { title: '今日LW人数', key: 'todayLwUserCount', width: 88, render: row => formatInt(row.todayLwUserCount) },
  { title: '今日开播', key: 'todayStreamHours', width: 84, render: row => formatHours(row.todayStreamHours) },
  { title: '昨日SR值', key: 'yesterdaySrValue', width: 88, render: row => formatMoney(row.yesterdaySrValue) },
  { title: '昨日SR人数', key: 'yesterdaySrUserCount', width: 88, render: row => formatInt(row.yesterdaySrUserCount) },
  { title: '昨日LW值', key: 'yesterdayLwValue', width: 88, render: row => formatMoney(row.yesterdayLwValue) },
  { title: '昨日LW人数', key: 'yesterdayLwUserCount', width: 88, render: row => formatInt(row.yesterdayLwUserCount) },
  { title: '昨日开播', key: 'yesterdayStreamHours', width: 84, render: row => formatHours(row.yesterdayStreamHours) },
  { title: '本月SR值', key: 'monthSrValue', width: 88, render: row => formatMoney(row.monthSrValue) },
  { title: '本月SR人数', key: 'monthSrUserCount', width: 88, render: row => formatInt(row.monthSrUserCount) },
  { title: '本月LW值', key: 'monthLwValue', width: 88, render: row => formatMoney(row.monthLwValue) },
  { title: '本月LW人数', key: 'monthLwUserCount', width: 88, render: row => formatInt(row.monthLwUserCount) },
  { title: '本月开播', key: 'monthStreamHours', width: 84, render: row => formatHours(row.monthStreamHours) },
  {
    title: '状态',
    key: 'status',
    width: 92,
    render(row) {
      if (!hasPermission('yun:anchor:edit')) {
        return h(NTag, { type: row.status === 1 ? 'success' : 'default', size: 'small' }, { default: () => row.status === 1 ? '启用' : '禁用' })
      }
      const loading = isRowLoading(statusUpdatingIds, row.id)
      return h(NSwitch, {
        value: row.status === 1,
        size: 'small',
        loading,
        disabled: loading,
        onUpdateValue: (value: boolean) => handleUpdateStatus(row, value),
      }, {
        checked: () => '启用',
        unchecked: () => '禁用',
      })
    }
  },
  {
    title: '榜单',
    key: 'showRank',
    width: 92,
    render(row) {
      if (!hasPermission('yun:anchor:edit')) {
        return h(NTag, { type: row.showRank === 1 ? 'info' : 'default', size: 'small' }, { default: () => row.showRank === 1 ? '展示' : '隐藏' })
      }
      const loading = isRowLoading(showRankUpdatingIds, row.id)
      return h(NSwitch, {
        value: row.showRank === 1,
        size: 'small',
        loading,
        disabled: loading,
        onUpdateValue: (value: boolean) => handleUpdateShowRank(row, value),
      }, {
        checked: () => '展示',
        unchecked: () => '隐藏',
      })
    }
  },
  { title: '最近同步', key: 'lastGiftSyncTime', width: 88, ellipsis: { tooltip: true }, render: row => row.lastGiftSyncTime || '-' },
  { title: '更新时间', key: 'updateTime', width: 88, ellipsis: { tooltip: true }, render: row => row.updateTime || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 324,
    fixed: 'right',
    render(row) {
      const actions = []
      const isRowSyncing = syncingRowIds.value.includes(row.id!)
      if (hasPermission('yun:anchor:sync')) {
        if (isRowSyncing) {
          actions.push(h(NButton, {
            size: 'small',
            quaternary: true,
            type: 'info',
            disabled: true,
            class: 'action-sync-btn'
          }, {
            default: () => ' 同步中'
          }))
        } else {
          actions.push(h(NButton, {
            size: 'small',
            quaternary: true,
            type: 'info',
            disabled: syncAllTask.running,
            class: 'action-sync-btn',
            onClick: () => handleSync(row)
          }, {
            default: () => [h(NIcon, null, { default: () => h(CloudDownloadOutline) }), ' 同步']
          }))
        }
      }
      if (hasPermission('yun:anchor:edit')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, disabled: isRowSyncing, onClick: () => handleEdit(row) }, {
          default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑']
        }))
      }
      if (hasPermission('yun:anchor:remove')) {
        actions.push(h(NButton, { size: 'small', quaternary: true, type: 'error', disabled: isRowSyncing, onClick: () => handleDelete(row) }, {
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

function formatInt(value: unknown) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? String(Math.trunc(num)) : '-'
}

function formatHours(value: unknown) {
  const num = Number(value || 0)
  return Number.isFinite(num) ? `${num.toFixed(1)} 小时` : '-'
}

function dataSourceLabel(value?: string) {
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
  formRef.value?.restoreValidation()
}

function handleAdd() {
  modalTitle.value = '新增主播'
  resetForm()
  modalVisible.value = true
}

function handleBatchAdd() {
  batchAnchorText.value = ''
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

async function handleUpdateStatus(row: YunAnchorPageRow, checked: boolean) {
  const id = row.id
  if (!id) {
    return
  }
  const nextStatus: AnchorStatus = checked ? 1 : 0
  const prevStatus = row.status ?? 0
  if (prevStatus === nextStatus || isRowLoading(statusUpdatingIds, id)) {
    return
  }
  updateRowLoading(statusUpdatingIds, id, true)
  row.status = nextStatus
  try {
    await anchorApi.updateStatus(id, nextStatus)
    message.success(nextStatus === 1 ? '状态已启用' : '状态已禁用')
    refreshAnchorRows()
  } catch (error) {
    row.status = prevStatus
    console.error('主播状态更新失败', error)
    message.error('状态更新失败')
  } finally {
    updateRowLoading(statusUpdatingIds, id, false)
  }
}

async function handleUpdateShowRank(row: YunAnchorPageRow, checked: boolean) {
  const id = row.id
  if (!id) {
    return
  }
  const nextShowRank: AnchorFlag = checked ? 1 : 0
  const prevShowRank = row.showRank ?? 0
  if (prevShowRank === nextShowRank || isRowLoading(showRankUpdatingIds, id)) {
    return
  }
  updateRowLoading(showRankUpdatingIds, id, true)
  row.showRank = nextShowRank
  try {
    await anchorApi.updateShowRank(id, nextShowRank)
    message.success(nextShowRank === 1 ? '已展示到榜单' : '已隐藏榜单')
    refreshAnchorRows()
  } catch (error) {
    row.showRank = prevShowRank
    console.error('主播榜单展示更新失败', error)
    message.error('榜单展示更新失败')
  } finally {
    updateRowLoading(showRankUpdatingIds, id, false)
  }
}

async function handleFetchPreview() {
  const anchorId = (formData.anchorId || '').trim()
  if (!anchorId) {
    message.warning('请先输入主播ID')
    return
  }
  previewLoading.value = true
  try {
    const preview = await anchorApi.fetchPreview(anchorId, toolbarSyncDataSource.value || 'DOSEEING')
    Object.assign(formData, {
      ...formData,
      ...preview,
      anchorId,
      status: formData.status ?? 1,
      showRank: formData.showRank ?? 1,
      sort: formData.sort ?? 0,
      autoUpdateProfile: formData.autoUpdateProfile ?? 1,
    })
    message.success(`已获取${dataSourceLabel(toolbarSyncDataSource.value || 'DOSEEING')}资料`)

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
      dataSource: toolbarSyncDataSource.value || 'DOSEEING',
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
          const result = await anchorApi.batchCreate([anchorId], toolbarSyncDataSource.value || 'DOSEEING')

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

function getErrorMessage(error: unknown, fallback: string): string {
  return error instanceof Error ? error.message : fallback
}

function showSyncErrors(result: YunSyncResult) {
  const errors = result.errors || []
  dialog.warning({
    title: '同步完成（部分失败）',
    content: () => h('div', { class: 'batch-result' }, [
      h('div', `成功：${result.successCount}/${result.totalCount}，失败：${result.failCount}`),
      errors.length > 0
        ? h('div', { class: 'batch-result-errors' }, errors.slice(0, 10).map(error => h('div', error)))
        : h('div', { class: 'batch-result-errors' }, '未返回具体失败原因'),
      errors.length > 10 ? h('div', { class: 'batch-result-more' }, `还有 ${errors.length - 10} 条失败记录未展示`) : null,
    ]),
    positiveText: '知道了'
  })
}

function renderSyncMessage(result: YunSyncResult) {
  if (result.failCount > 0) {
    showSyncErrors(result)
    return
  }
  message.success(`同步成功：${result.successCount}/${result.totalCount}`)
}

async function handleSync(row: YunAnchorPageRow) {
  if (syncingRowIds.value.includes(row.id!)) {
    return
  }
  syncingRowIds.value = [...syncingRowIds.value, row.id!]
  try {
    const result = await anchorApi.sync(row.id!, toolbarSyncDataSource.value || 'DOSEEING')
    renderSyncMessage(result)
    await loadData()
  } catch (error) {
    message.error(`同步失败：${getErrorMessage(error, '未知错误')}`)
  } finally {
    syncingRowIds.value = syncingRowIds.value.filter(id => id !== row.id)
  }
}

function handleSyncAll() {
  if (syncAllTask.running) {
    return
  }
  dialog.warning({
    title: '同步全部主播',
    content: `将按当前同步平台（${dataSourceLabel(toolbarSyncDataSource.value)}）同步所有启用主播的今日、昨日和本月礼物数据，确认继续吗？`,
    positiveText: '开始同步',
    negativeText: '取消',
    onPositiveClick: () => {
      void startSyncAll()
    }
  })
}

async function startSyncAll() {
  syncAllModalVisible.value = true
  syncAllLoading.value = true
  syncAllTask.running = true
  syncAllTask.completedCount = 0
  syncAllTask.successCount = 0
  syncAllTask.failCount = 0
  syncAllTask.currentAnchorId = ''
  syncAllTask.errors = []
  try {
    const progress: YunSyncProgress = await anchorApi.syncAll(toolbarSyncDataSource.value || 'DOSEEING')
    applySyncAllProgress(progress)
    startSyncPolling()
  } catch (error) {
    syncAllLoading.value = false
    syncAllTask.running = false
    message.error(`启动同步失败：${getErrorMessage(error, '请稍后重试')}`)
  }
}

function applySyncAllProgress(progress: YunSyncProgress) {
  syncAllTask.taskId = progress.taskId || ''
  syncAllTask.running = !!progress.running
  syncAllTask.totalCount = progress.totalCount ?? syncAllTask.totalCount
  syncAllTask.completedCount = progress.completedCount || 0
  syncAllTask.successCount = progress.successCount || 0
  syncAllTask.failCount = progress.failCount || 0
  syncAllTask.currentAnchorId = progress.currentAnchorId || ''
  syncAllTask.errors = progress.errors || []
}

function startSyncPolling() {
  stopSyncPolling()
  syncPollTimer = window.setInterval(async () => {
    try {
      const progress = await anchorApi.syncAllProgress(syncAllTask.taskId)
      applySyncAllProgress(progress)
      if (!syncAllTask.running) {
        stopSyncPolling()
        syncAllLoading.value = false
        await loadData()
      }
    } catch (error) {
      stopSyncPolling()
      syncAllLoading.value = false
      message.error(`同步进度查询失败：${getErrorMessage(error, '请刷新页面查看结果')}`)
    }
  }, 1500)
}

function stopSyncPolling() {
  if (syncPollTimer !== null) {
    window.clearInterval(syncPollTimer)
    syncPollTimer = null
  }
}

function handleCancelSyncAll() {
  stopSyncPolling()
  syncAllTask.running = false
  syncAllLoading.value = false
  message.info('已取消同步，后台任务可能仍在进行')
  refreshAnchorRows()
}

async function handleOpenCookieConfig() {
  cookieModalVisible.value = true
  cookieValue.value = ''
  proxyValue.value = ''
  try {
    const group = await configGroupApi.getByCode('yunDataSource')
    if (group?.configValue) {
      const cfg = JSON.parse(group.configValue)
      cookieValue.value = cfg.doseeingCookie || ''
      proxyValue.value = cfg.doseeingProxy || ''
    }
  } catch {
    // 配置组不存在时忽略，使用空值
  }
}

async function handleSaveCookie() {
  cookieSaving.value = true
  try {
    await configGroupApi.save('yunDataSource', {
      doseeingCookie: cookieValue.value.trim(),
      doseeingProxy: proxyValue.value.trim(),
    })
    message.success('Cookie 保存成功')
    cookieModalVisible.value = false
  } catch {
    message.error('Cookie 保存失败，请检查系统配置中是否存在 yunDataSource 配置组')
  } finally {
    cookieSaving.value = false
  }
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
onUnmounted(() => {
  stopSyncPolling()
})
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

.table-toolbar-actions {
  width: 100%;
}

.table-toolbar-primary {
  align-items: center;
  flex-wrap: wrap;
}

.sync-platform-meta {
  display: inline-flex;
  align-items: center;
  flex: 0 0 auto;
  gap: 8px;
  min-height: 32px;
  white-space: nowrap;
}

.sync-platform-meta-label {
  color: #98a2b3;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.sync-platform-meta-value {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 14px;
  border-radius: 999px;
  background: #f3f6fb;
  color: #344256;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.01em;
  box-shadow: inset 0 0 0 1px #e1e8f0;
}

.sync-platform-meta-select {
  width: 148px;
}

.sync-platform-meta-select :deep(.n-base-selection) {
  min-height: 32px;
  border: 1px solid #e1e8f0;
  border-radius: 12px;
  background: #fff;
  box-shadow: none;
}

.sync-platform-meta-select :deep(.n-base-selection-label) {
  font-size: 13px;
  font-weight: 600;
  color: #344256;
}

.sync-platform-meta-select :deep(.n-base-selection-placeholder) {
  color: #98a2b3;
}

.sync-platform-meta-select :deep(.n-base-selection-arrow) {
  color: #b4bdc9;
}

.sync-all-modal-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sync-all-modal-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sync-all-modal-status {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.sync-all-modal-status.is-running {
  color: #2080f0;
}

.sync-all-modal-stats {
  display: flex;
  gap: 24px;
}

.sync-all-modal-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sync-all-modal-stat-label {
  font-size: 12px;
  color: #8a8f99;
}

.sync-all-modal-stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.sync-all-modal-stat-success {
  color: #18a058;
}

.sync-all-modal-stat-fail {
  color: #d03050;
}

.sync-all-modal-current {
  padding: 8px 12px;
  border-radius: 6px;
  background: #f5f5f5;
  color: #2080f0;
  font-size: 13px;
}

.sync-all-modal-errors {
  padding: 10px 12px;
  border-radius: 6px;
  background: #fff2f0;
  color: #d03050;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-all;
}

.sync-all-modal-errors-more {
  margin-top: 4px;
  color: #8a8f99;
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
  gap: 8px;
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
  font-size: 11px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: flex-end;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.action-sync-btn {
  min-width: 0;
}

.action-buttons :deep(.n-button) {
  flex: 0 0 auto;
  padding-right: 4px;
  padding-left: 4px;
}

.action-buttons :deep(.n-button__content) {
  white-space: nowrap;
}

.fullscreen-loading {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  background: rgba(0, 0, 0, 0.45);
}

.fullscreen-loading-text {
  margin: 0;
  color: #fff;
  font-size: 14px;
}
</style>
