<template>
  <div class="page-container">
    <n-card>
      <div class="season-page">
        <div class="season-toolbar">
          <div class="season-toolbar-left">
            <n-input v-model:value="seasonSearch.seasonCode" placeholder="赛季编号" clearable style="width: 180px" />
            <n-input v-model:value="seasonSearch.seasonName" placeholder="赛季名称" clearable style="width: 180px" />
            <n-select
              v-model:value="seasonSearch.status"
              :options="statusOptions"
              clearable
              placeholder="状态"
              style="width: 120px"
            />
            <n-button type="primary" @click="loadSeasonPage">
              <template #icon><n-icon><SearchOutline /></n-icon></template>
              搜索
            </n-button>
            <n-button @click="resetSeasonSearch">
              <template #icon><n-icon><RefreshOutline /></n-icon></template>
              重置
            </n-button>
          </div>
          <n-space>
            <n-button type="primary" @click="openSeasonModal()">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              新增赛季
            </n-button>
            <n-button secondary @click="loadSeasonPage">
              <template #icon><n-icon><ReloadOutline /></n-icon></template>
              刷新
            </n-button>
          </n-space>
        </div>

        <n-data-table
          :columns="seasonColumns"
          :data="seasonTableData"
          :loading="seasonLoading"
          :pagination="seasonPagination"
          :row-key="row => row.id"
          :row-props="row => ({ onClick: () => selectSeason(row) })"
          remote
          size="small"
          @update:page="handleSeasonPageChange"
          @update:page-size="handleSeasonPageSizeChange"
        />

        <div class="member-head">
          <div>
            <div class="member-title">
              {{ currentSeason?.seasonCode || '未选择赛季' }}
              <span v-if="currentSeason?.seasonName" class="member-title-sub">· {{ currentSeason.seasonName }}</span>
            </div>
            <div class="member-subtitle">
              {{ currentSeason ? `共 ${currentSeason.memberCount || 0} 位成员 · ${currentSeason.eliminatedCount || 0} 人已淘汰` : '请选择一个赛季管理成员' }}
            </div>
          </div>
          <n-space>
            <n-button v-if="currentSeason" type="primary" @click="openMemberModal()">
              <template #icon><n-icon><AddOutline /></n-icon></template>
              批量加入
            </n-button>
            <n-button v-if="currentSeason" secondary @click="openCopyModal(currentSeason)">
              <template #icon><n-icon><CopyOutline /></n-icon></template>
              复制赛季
            </n-button>
            <n-button v-if="currentSeason" tertiary @click="openResetConfirm">
              <template #icon><n-icon><ReloadOutline /></n-icon></template>
              重置成员
            </n-button>
            <n-button v-if="currentSeason" type="error" secondary @click="handleDeleteSeason(currentSeason)">
              <template #icon><n-icon><TrashOutline /></n-icon></template>
              删除赛季
            </n-button>
          </n-space>
        </div>

        <div v-if="currentSeason" class="member-toolbar">
          <div class="member-toolbar-left">
            <n-input v-model:value="memberSearch.keyword" placeholder="按主播名/ID/队伍搜索" clearable style="width: 220px" />
            <n-select
              v-model:value="memberSearch.eliminated"
              :options="eliminatedOptions"
              clearable
              placeholder="淘汰状态"
              style="width: 130px"
            />
            <n-select
              v-model:value="memberSearch.captainFlag"
              :options="captainOptions"
              clearable
              placeholder="队长"
              style="width: 120px"
            />
            <n-button type="primary" @click="loadMemberPage(true)">
              <template #icon><n-icon><SearchOutline /></n-icon></template>
              搜索成员
            </n-button>
            <n-button @click="resetMemberSearch">
              <template #icon><n-icon><RefreshOutline /></n-icon></template>
              重置
            </n-button>
          </div>
          <n-space>
            <n-button secondary :disabled="!memberSelectedIds.length" @click="handleDeleteMembers">
              <template #icon><n-icon><TrashOutline /></n-icon></template>
              删除{{ memberSelectedIds.length ? `(${memberSelectedIds.length})` : '' }}
            </n-button>
          </n-space>
        </div>

        <n-data-table
          v-if="currentSeason"
          :columns="memberColumns"
          :data="memberTableData"
          :loading="memberLoading"
          :pagination="memberPagination"
          :row-key="row => row.id"
          remote
          size="small"
          @update:page="handleMemberPageChange"
          @update:page-size="handleMemberPageSizeChange"
          @update:checked-row-keys="handleMemberCheck"
        />
        <n-empty v-else description="先选择或新建一个赛季">
          <template #icon>
            <n-icon size="40"><TrophyOutline /></n-icon>
          </template>
        </n-empty>
      </div>
    </n-card>

    <n-modal v-model:show="seasonModalVisible" preset="card" :title="seasonModalTitle" style="width: 760px">
      <n-form ref="seasonFormRef" :model="seasonFormData" :rules="seasonFormRules" label-placement="left" label-width="100px">
        <n-grid :cols="2" :x-gap="20">
          <n-form-item-gi label="赛季编号" path="seasonCode">
            <n-input v-model:value="seasonFormData.seasonCode" placeholder="如：S10.5" maxlength="32" />
          </n-form-item-gi>
          <n-form-item-gi label="赛季名称" path="seasonName">
            <n-input v-model:value="seasonFormData.seasonName" placeholder="如：S10.5 赛季" maxlength="100" />
          </n-form-item-gi>
          <n-form-item-gi label="状态" path="status">
            <n-switch v-model:value="seasonFormData.status" :checked-value="1" :unchecked-value="0">
              <template #checked>启用</template>
              <template #unchecked>停用</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="客户端显示" path="appDisplay">
            <n-switch v-model:value="seasonFormData.appDisplay" :checked-value="1" :unchecked-value="0">
              <template #checked>显示</template>
              <template #unchecked>隐藏</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="排序" path="sort">
            <n-input-number v-model:value="seasonFormData.sort" :min="0" :step="1" style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="开始时间" path="startTime">
            <n-date-picker v-model:value="seasonFormDate.startTime" type="datetime" clearable style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="结束时间" path="endTime">
            <n-date-picker v-model:value="seasonFormDate.endTime" type="datetime" clearable style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="封面图" path="coverImageUrl" :span="2">
            <ImageUpload v-model="seasonFormData.coverImageUrl" />
          </n-form-item-gi>
          <n-form-item-gi label="备注" path="remark" :span="2">
            <n-input v-model:value="seasonFormData.remark" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
          </n-form-item-gi>
        </n-grid>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="seasonModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="seasonSubmitLoading" @click="handleSeasonSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="copyModalVisible" preset="card" title="复制赛季" style="width: 640px">
      <n-form ref="copyFormRef" :model="copyFormData" :rules="copyFormRules" label-placement="left" label-width="108px">
        <n-form-item label="新赛季编号" path="seasonCode">
          <n-input v-model:value="copyFormData.seasonCode" placeholder="如：S11" maxlength="32" />
        </n-form-item>
        <n-form-item label="新赛季名称" path="seasonName">
          <n-input v-model:value="copyFormData.seasonName" placeholder="复制后展示名称" maxlength="100" />
        </n-form-item>
        <n-form-item label="封面图" path="coverImageUrl">
          <ImageUpload v-model="copyFormData.coverImageUrl" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-switch v-model:value="copyFormData.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>停用</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="复制成员">
          <n-switch v-model:value="copyFormData.copyMembers">
            <template #checked>复制</template>
            <template #unchecked>不复制</template>
          </n-switch>
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="copyFormData.remark" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="copyModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="copySubmitLoading" @click="handleCopySubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="memberModalVisible" preset="card" :title="memberModalTitle" style="width: 980px">
      <n-form ref="memberFormRef" :model="memberFormData" :rules="memberFormRules" label-placement="left" label-width="110px">
        <n-grid :cols="2" :x-gap="20">
          <n-form-item-gi label="队伍名称" path="teamName">
            <n-input v-model:value="memberFormData.teamName" placeholder="留空则不显示队伍" maxlength="100" />
          </n-form-item-gi>
          <n-form-item-gi label="队长" path="captainFlag">
            <n-switch v-model:value="memberFormData.captainFlag" :checked-value="1" :unchecked-value="0">
              <template #checked>是</template>
              <template #unchecked>否</template>
            </n-switch>
          </n-form-item-gi>
        </n-grid>
      </n-form>

      <div class="anchor-picker">
        <div class="anchor-import-mode">
          <n-radio-group v-model:value="memberImportMode" size="small">
            <n-radio-button value="room">房间号导入</n-radio-button>
            <n-radio-button value="select">选择已有主播</n-radio-button>
          </n-radio-group>
        </div>

        <div v-show="memberImportMode === 'room'" class="anchor-import">
          <n-alert type="info" :bordered="false" class="anchor-import-tip">
            可直接粘贴房间号批量加入。每行一个；hy: 表示虎牙，dy: 表示斗鱼，不带前缀默认斗鱼。已有主播直接加入，没有则自动创建主播后加入。
          </n-alert>
          <n-input
            v-model:value="memberFormData.anchorText"
            type="textarea"
            placeholder="182102&#10;hy:156324&#10;dy:999999"
            :autosize="{ minRows: 4, maxRows: 8 }"
            :disabled="memberSubmitLoading"
          />
          <div class="anchor-import-footer">
            <span class="anchor-import-count">已输入 {{ memberImportCount }} 个房间号</span>
            <n-button type="primary" :disabled="!memberCanSubmit" :loading="memberSubmitLoading" @click="handleMemberSubmit">
              导入房间号
            </n-button>
          </div>
        </div>

        <div v-show="memberImportMode === 'select'" class="anchor-picker-select">
          <div class="anchor-picker-head">
            <div>
              <div class="anchor-picker-title">
                选择已有主播
              </div>
              <div class="anchor-picker-subtitle">
                已选择 {{ anchorSelectedRowKeys.length }} 位主播
              </div>
            </div>
          <n-space>
            <n-button tertiary :disabled="!anchorSelectedRowKeys.length" @click="clearAnchorSelection">
              清空选择
            </n-button>
            <n-button secondary @click="loadAnchorPage(true)">
              <template #icon><n-icon><ReloadOutline /></n-icon></template>
              刷新
            </n-button>
          </n-space>
        </div>

        <div class="anchor-picker-toolbar">
          <n-input v-model:value="anchorSearch.anchorName" placeholder="主播名称" clearable style="width: 160px" @keyup.enter="loadAnchorPage(true)" />
          <n-input v-model:value="anchorSearch.anchorId" placeholder="主播ID" clearable style="width: 150px" @keyup.enter="loadAnchorPage(true)" />
          <n-input v-model:value="anchorSearch.roomId" placeholder="房间号" clearable style="width: 150px" @keyup.enter="loadAnchorPage(true)" />
          <n-input v-model:value="anchorSearch.guildName" placeholder="公会/队伍" clearable style="width: 160px" @keyup.enter="loadAnchorPage(true)" />
          <n-select
            v-model:value="anchorSearch.status"
            :options="anchorStatusOptions"
            clearable
            placeholder="主播状态"
            style="width: 120px"
          />
          <n-button type="primary" @click="loadAnchorPage(true)">
            <template #icon><n-icon><SearchOutline /></n-icon></template>
            搜索
          </n-button>
          <n-button @click="resetAnchorSearch">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
            重置
          </n-button>
        </div>

        <n-data-table
          :checked-row-keys="anchorSelectedRowKeys"
          :columns="anchorColumns"
          :data="anchorTableData"
          :loading="anchorLoading"
          :pagination="anchorPagination"
          :row-key="getAnchorRowKey"
          remote
          size="small"
          @update:checked-row-keys="handleAnchorCheck"
          @update:page="handleAnchorPageChange"
          @update:page-size="handleAnchorPageSizeChange"
        />

        <div class="anchor-picker-select-footer">
          <span class="anchor-import-count">已选择 {{ anchorSelectedRowKeys.length }} 位主播</span>
          <n-button type="primary" :disabled="!memberCanSubmit" :loading="memberSubmitLoading" @click="handleMemberSubmit">
            加入所选主播
          </n-button>
        </div>
        </div>
      </div>

      <template #footer>
        <n-space justify="end">
          <n-button @click="memberModalVisible = false">取消</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="memberEditModalVisible" preset="card" title="编辑赛季成员" style="width: 760px">
      <n-form ref="memberEditFormRef" :model="memberEditFormData" :rules="memberEditFormRules" label-placement="left" label-width="110px">
        <n-grid :cols="2" :x-gap="20">
          <n-form-item-gi label="主播名称" path="anchorName">
            <n-input v-model:value="memberEditFormData.anchorName" placeholder="主播名称" maxlength="100" />
          </n-form-item-gi>
          <n-form-item-gi label="队伍名称" path="teamName">
            <n-input v-model:value="memberEditFormData.teamName" placeholder="队伍名称" maxlength="100" />
          </n-form-item-gi>
          <n-form-item-gi label="队长" path="captainFlag">
            <n-switch v-model:value="memberEditFormData.captainFlag" :checked-value="1" :unchecked-value="0">
              <template #checked>是</template>
              <template #unchecked>否</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="是否淘汰" path="eliminated">
            <n-switch v-model:value="memberEditFormData.eliminated" :checked-value="1" :unchecked-value="0">
              <template #checked>已淘汰</template>
              <template #unchecked>未淘汰</template>
            </n-switch>
          </n-form-item-gi>
          <n-form-item-gi label="开条次数" path="failTimes">
            <n-input-number v-model:value="memberEditFormData.failTimes" :min="0" :step="1" style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="下次金额" path="nextEliminationAmount">
            <n-input-number v-model:value="memberEditFormData.nextEliminationAmount" :min="0" :step="1" style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="排序" path="sort">
            <n-input-number v-model:value="memberEditFormData.sort" :min="0" :step="1" style="width: 100%" />
          </n-form-item-gi>
          <n-form-item-gi label="头像" path="avatarUrl">
            <ImageUpload v-model="memberEditFormData.avatarUrl" />
          </n-form-item-gi>
          <n-form-item-gi label="大图" path="bigImageUrl">
            <ImageUpload v-model="memberEditFormData.bigImageUrl" />
          </n-form-item-gi>
          <n-form-item-gi label="备注" path="remark" :span="2">
            <n-input v-model:value="memberEditFormData.remark" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
          </n-form-item-gi>
        </n-grid>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="memberEditModalVisible = false">取消</n-button>
          <n-button type="primary" :loading="memberEditSubmitLoading" @click="handleMemberEditSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import {
  NButton,
  NAlert,
  NCard,
  NDatePicker,
  NEmpty,
  NForm,
  NFormItemGi,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NRadioButton,
  NRadioGroup,
  NSelect,
  NSpace,
  NSwitch,
  NDataTable,
  useDialog,
  useMessage,
  type DataTableColumns,
  type FormInst,
  type FormRules,
} from 'naive-ui'
import {
  AddOutline,
  CopyOutline,
  ReloadOutline,
  RefreshOutline,
  SearchOutline,
  TrashOutline,
  TrophyOutline,
} from '@vicons/ionicons5'
import ImageUpload from '@/components/ImageUpload.vue'
import type { AnchorStatus, YunAnchorPageRow } from '@/api/anchor'
import { seasonApi, type CaptainFlag, type EliminatedFlag, type SeasonStatus, type YunSeason, type YunSeasonAnchor, type YunSeasonAnchorPageRow, type YunSeasonMemberBatchRequest, type YunSeasonPageRow } from '@/api/season'
import { useUserStore } from '@/stores/user'

defineOptions({
  name: 'YunSeason',
})

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const hasPermission = (permission: string) => userStore.hasPermission(permission)

const statusOptions: Array<{ label: string; value: SeasonStatus }> = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

const eliminatedOptions: Array<{ label: string; value: EliminatedFlag }> = [
  { label: '未淘汰', value: 0 },
  { label: '已淘汰', value: 1 },
]

const captainOptions: Array<{ label: string; value: CaptainFlag }> = [
  { label: '普通成员', value: 0 },
  { label: '队长', value: 1 },
]

const anchorStatusOptions: Array<{ label: string; value: AnchorStatus }> = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

const seasonSearch = reactive<{ seasonCode: string; seasonName: string; status: SeasonStatus | null }>({
  seasonCode: '',
  seasonName: '',
  status: null,
})

const seasonLoading = ref(false)
const seasonTableData = ref<YunSeasonPageRow[]>([])
const seasonPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})
const currentSeasonId = ref<number | null>(null)
const currentSeason = computed(() => seasonTableData.value.find(item => item.id === currentSeasonId.value) || null)

const memberSearch = reactive<{ keyword: string; eliminated: EliminatedFlag | null; captainFlag: CaptainFlag | null }>({
  keyword: '',
  eliminated: null,
  captainFlag: null,
})
const memberLoading = ref(false)
const memberTableData = ref<YunSeasonAnchorPageRow[]>([])
const memberSelectedIds = ref<number[]>([])
const memberPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const seasonModalVisible = ref(false)
const seasonModalTitle = ref('新增赛季')
const seasonSubmitLoading = ref(false)
const seasonFormRef = ref<FormInst | null>(null)
const seasonFormData = reactive<YunSeason>({
  seasonCode: '',
  seasonName: '',
  coverImageUrl: '',
  status: 1,
  appDisplay: 0,
  sort: 0,
  remark: '',
})
const seasonFormDate = reactive<{ startTime: number | null; endTime: number | null }>({
  startTime: null,
  endTime: null,
})
const seasonFormRules: FormRules = {
  seasonCode: { required: true, message: '请输入赛季编号', trigger: ['blur', 'input'] },
}

const copyModalVisible = ref(false)
const copySubmitLoading = ref(false)
const copySourceSeasonId = ref<number | null>(null)
const copyFormRef = ref<FormInst | null>(null)
const copyFormData = reactive<YunSeason & { copyMembers?: boolean }>({
  seasonCode: '',
  seasonName: '',
  coverImageUrl: '',
  status: 1,
  sort: 0,
  remark: '',
  copyMembers: true,
})
const copyFormRules: FormRules = {
  seasonCode: { required: true, message: '请输入新赛季编号', trigger: ['blur', 'input'] },
}

const memberModalVisible = ref(false)
const memberModalTitle = ref('批量加入成员')
const memberSubmitLoading = ref(false)
const memberImportMode = ref<'room' | 'select'>('room')
const memberFormRef = ref<FormInst | null>(null)
const memberFormData = reactive<{ teamName: string; captainFlag: CaptainFlag; anchorText: string }>({
  teamName: '',
  captainFlag: 0,
  anchorText: '',
})
const memberFormRules: FormRules = {
  teamName: [],
}

const anchorSearch = reactive<{ anchorId: string; anchorName: string; roomId: string; guildName: string; status: AnchorStatus | null }>({
  anchorId: '',
  anchorName: '',
  roomId: '',
  guildName: '',
  status: 1,
})
const anchorLoading = ref(false)
const anchorTableData = ref<YunAnchorPageRow[]>([])
const anchorSelectedRowKeys = ref<number[]>([])
const anchorPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
})

const memberImportAnchorIds = computed(() => parseMemberImportAnchorIds())
const memberImportCount = computed(() => memberImportAnchorIds.value.length)
const memberCanSubmit = computed(() =>
  memberImportMode.value === 'select'
    ? anchorSelectedRowKeys.value.length > 0
    : memberImportCount.value > 0,
)

const memberEditModalVisible = ref(false)
const memberEditSubmitLoading = ref(false)
const memberEditFormRef = ref<FormInst | null>(null)
const memberEditFormData = reactive<YunSeasonAnchor>({
  id: undefined,
  seasonId: undefined,
  anchorRefId: undefined,
  anchorId: '',
  platform: '',
  roomId: '',
  anchorName: '',
  avatarUrl: '',
  bigImageUrl: '',
  teamName: '',
  captainFlag: 0,
  eliminated: 0,
  failTimes: 0,
  nextEliminationAmount: 0,
  sort: 0,
  remark: '',
})
const memberEditFormRules: FormRules = {
  anchorName: [],
}

const seasonColumns: DataTableColumns<YunSeasonPageRow> = [
  {
    title: '赛季',
    key: 'seasonCode',
    width: 160,
    render(row) {
      return h('div', { class: 'season-cell' }, [
        h('div', { class: 'season-code' }, row.seasonCode),
        h('div', { class: 'season-name' }, row.seasonName || '-'),
      ])
    },
  },
  {
    title: '封面',
    key: 'coverImageUrl',
    width: 120,
    render(row) {
      return row.coverImageUrl
        ? h('img', { src: row.coverImageUrl, class: 'season-cover' })
        : h('div', { class: 'season-cover-empty' }, '暂无')
    },
  },
  { title: '成员', key: 'memberCount', width: 90 },
  { title: '队长', key: 'captainCount', width: 90 },
  { title: '淘汰', key: 'eliminatedCount', width: 90 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      return h('span', { class: ['season-status', row.status === 1 ? 'is-on' : 'is-off'] }, row.status === 1 ? '启用' : '停用')
    },
  },
  {
    title: '客户端',
    key: 'appDisplay',
    width: 110,
    render(row) {
      return h('span', { class: ['season-status', row.appDisplay === 1 ? 'is-client' : 'is-off'] }, row.appDisplay === 1 ? '当前显示' : '不显示')
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 260,
    fixed: 'right',
    render(row) {
      const actions = [
        h(NButton, { size: 'small', secondary: true, onClick: () => openSeasonModal(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', secondary: true, onClick: () => openCopyModal(row) }, { default: () => '复制' }),
      ]
      if (hasPermission('yun:season:remove')) {
        actions.push(h(NButton, { size: 'small', type: 'error', secondary: true, onClick: () => handleDeleteSeason(row) }, { default: () => '删除' }))
      }
      return h(NSpace, { size: 8 }, { default: () => actions })
    },
  },
]

const memberColumns: DataTableColumns<YunSeasonAnchorPageRow> = [
  { type: 'selection' },
  {
    title: '主播',
    key: 'anchorName',
    width: 220,
    render(row) {
      const avatar = row.avatarUrl || ''
      const avatarStyle = {
        display: 'block',
        width: '40px',
        minWidth: '40px',
        maxWidth: '40px',
        height: '40px',
        minHeight: '40px',
        maxHeight: '40px',
        borderRadius: '8px',
        objectFit: 'cover',
      }
      const captainStyle = {
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '22px',
        padding: '0 8px',
        borderRadius: '999px',
        background: '#fef3c7',
        color: '#92400e',
        fontSize: '12px',
        lineHeight: '22px',
      }
      return h('div', { class: 'member-cell' }, [
        avatar
          ? h('img', { src: avatar, class: 'member-cover', style: avatarStyle })
          : h('div', { class: 'member-cover-empty', style: avatarStyle }, '无图'),
        h('div', { class: 'member-cell-main' }, [
          h('div', { class: 'member-name-line' }, [
            h('span', { class: 'member-name' }, row.anchorName || '-'),
            row.captainFlag === 1 ? h('span', { style: captainStyle }, '队长') : null,
          ]),
          h('div', { class: 'member-sub' }, `ID：${row.anchorId || '-'} · 房间：${row.roomId || '-'}`),
        ]),
      ])
    },
  },
  { title: '队伍', key: 'teamName', width: 120 },
  {
    title: '状态',
    key: 'eliminated',
    width: 90,
    render(row) {
      const isEliminated = row.eliminated === 1
      return h('span', {
        style: {
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          height: '24px',
          minWidth: '58px',
          padding: '0 10px',
          borderRadius: '999px',
          background: isEliminated ? '#fee2e2' : '#dbeafe',
          color: isEliminated ? '#b91c1c' : '#1d4ed8',
          fontSize: '12px',
          lineHeight: '24px',
        },
      }, isEliminated ? '已淘汰' : '未淘汰')
    },
  },
  { title: '开条次数', key: 'failTimes', width: 100 },
  { title: '下次金额', key: 'nextEliminationAmount', width: 120 },
  {
    title: '操作',
    key: 'actions',
    width: 160,
    fixed: 'right',
    render(row) {
      const actions = [
        h(NButton, { size: 'small', secondary: true, onClick: () => openMemberEditModal(row) }, { default: () => '编辑' }),
      ]
      if (hasPermission('yun:season:edit')) {
        actions.push(h(NButton, { size: 'small', type: 'error', secondary: true, onClick: () => handleDeleteMembers([row.id!]) }, { default: () => '删除' }))
      }
      return h(NSpace, { size: 8 }, { default: () => actions })
    },
  },
]

const anchorColumns: DataTableColumns<YunAnchorPageRow> = [
  { type: 'selection' },
  {
    title: '主播',
    key: 'anchorName',
    width: 220,
    render(row) {
      const cover = row.avatarUrl || row.bigImageUrl
      const coverStyle = {
        display: 'block',
        width: '36px',
        minWidth: '36px',
        maxWidth: '36px',
        height: '36px',
        minHeight: '36px',
        maxHeight: '36px',
        borderRadius: '6px',
        objectFit: 'cover',
      }
      return h('div', { class: 'anchor-cell' }, [
        cover
          ? h('img', { src: cover, class: 'anchor-cover', style: coverStyle })
          : h('div', { class: 'anchor-cover-empty', style: coverStyle }, '无图'),
        h('div', { class: 'anchor-cell-main' }, [
          h('div', { class: 'anchor-name' }, row.anchorName || '-'),
          h('div', { class: 'anchor-sub' }, `ID：${row.anchorId || '-'} · 房间：${row.roomId || '-'}`),
        ]),
      ])
    },
  },
  { title: '平台', key: 'platform', width: 90 },
  { title: '公会/队伍', key: 'guildName', width: 150, render: row => row.guildName || '-' },
  { title: '直播分类', key: 'categoryName', width: 130, render: row => row.categoryName || '-' },
  { title: '排序', key: 'sort', width: 80 },
]

function normalizeDate(value: number | null) {
  if (!value) {
    return undefined
  }
  const date = new Date(value)
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function selectSeason(row: YunSeasonPageRow) {
  currentSeasonId.value = row.id || null
}

function openSeasonModal(row?: YunSeasonPageRow) {
  seasonModalTitle.value = row ? '编辑赛季' : '新增赛季'
  seasonFormRef.value?.restoreValidation()
  Object.assign(seasonFormData, {
    id: row?.id,
    seasonCode: row?.seasonCode || '',
    seasonName: row?.seasonName || '',
    coverImageUrl: row?.coverImageUrl || '',
    status: row?.status ?? 1,
    appDisplay: row?.appDisplay ?? 0,
    sort: row?.sort ?? 0,
    remark: row?.remark || '',
  })
  seasonFormDate.startTime = row?.startTime ? new Date(row.startTime).getTime() : null
  seasonFormDate.endTime = row?.endTime ? new Date(row.endTime).getTime() : null
  seasonModalVisible.value = true
}

function resetSeasonForm() {
  Object.assign(seasonFormData, {
    id: undefined,
    seasonCode: '',
    seasonName: '',
    coverImageUrl: '',
    status: 1,
    appDisplay: 0,
    sort: 0,
    remark: '',
  })
  seasonFormDate.startTime = null
  seasonFormDate.endTime = null
  seasonFormRef.value?.restoreValidation()
}

function openCopyModal(row: YunSeasonPageRow) {
  copySourceSeasonId.value = row.id || null
  Object.assign(copyFormData, {
    seasonCode: '',
    seasonName: row.seasonName ? `${row.seasonName}（复制）` : '',
    coverImageUrl: row.coverImageUrl || '',
    status: row.status ?? 1,
    sort: row.sort ?? 0,
    remark: row.remark || '',
    copyMembers: true,
  })
  copyModalVisible.value = true
}

function openMemberModal() {
  memberModalTitle.value = '批量加入成员'
  memberFormData.teamName = ''
  memberFormData.captainFlag = 0
  memberFormData.anchorText = ''
  memberSubmitLoading.value = false
  memberImportMode.value = 'room'
  anchorSearch.anchorId = ''
  anchorSearch.anchorName = ''
  anchorSearch.roomId = ''
  anchorSearch.guildName = ''
  anchorSearch.status = 1
  anchorSelectedRowKeys.value = []
  anchorPagination.page = 1
  memberModalVisible.value = true
  void loadAnchorPage(true)
}

function openMemberEditModal(row: YunSeasonAnchorPageRow) {
  Object.assign(memberEditFormData, {
    ...row,
    nextEliminationAmount: Number(row.nextEliminationAmount || 0),
  })
  memberEditModalVisible.value = true
}

function resetMemberEditForm() {
  Object.assign(memberEditFormData, {
    id: undefined,
    seasonId: undefined,
    anchorRefId: undefined,
    anchorId: '',
    platform: '',
    roomId: '',
    anchorName: '',
    avatarUrl: '',
    bigImageUrl: '',
    teamName: '',
    captainFlag: 0,
    eliminated: 0,
    failTimes: 0,
    nextEliminationAmount: 0,
    sort: 0,
    remark: '',
  })
}

function handleSeasonPageChange(page: number) {
  seasonPagination.page = page
  loadSeasonPage()
}

function handleSeasonPageSizeChange(pageSize: number) {
  seasonPagination.pageSize = pageSize
  seasonPagination.page = 1
  loadSeasonPage()
}

function handleMemberPageChange(page: number) {
  memberPagination.page = page
  loadMemberPage()
}

function handleMemberPageSizeChange(pageSize: number) {
  memberPagination.pageSize = pageSize
  memberPagination.page = 1
  loadMemberPage()
}

function handleMemberCheck(keys: Array<string | number>) {
  memberSelectedIds.value = keys as number[]
}

function getAnchorRowKey(row: YunAnchorPageRow) {
  return row.id || 0
}

function handleAnchorCheck(keys: Array<string | number>) {
  anchorSelectedRowKeys.value = keys
    .map(key => Number(key))
    .filter(key => Number.isFinite(key) && key > 0)
}

async function loadSeasonPage() {
  seasonLoading.value = true
  try {
    const result = await seasonApi.page({
      page: seasonPagination.page,
      pageSize: seasonPagination.pageSize,
      seasonCode: seasonSearch.seasonCode || undefined,
      seasonName: seasonSearch.seasonName || undefined,
      status: seasonSearch.status ?? undefined,
    })
    seasonTableData.value = result.list || []
    seasonPagination.itemCount = result.total
    if (!currentSeasonId.value && seasonTableData.value.length > 0) {
      currentSeasonId.value = seasonTableData.value[0].id || null
    }
    if (currentSeasonId.value && !seasonTableData.value.some(item => item.id === currentSeasonId.value)) {
      currentSeasonId.value = seasonTableData.value[0]?.id || null
    }
    if (currentSeasonId.value) {
      await loadMemberPage(true)
    }
  }
  finally {
    seasonLoading.value = false
  }
}

async function loadMemberPage(reset = false) {
  if (!currentSeasonId.value) {
    memberTableData.value = []
    memberPagination.itemCount = 0
    return
  }
  if (reset) {
    memberPagination.page = 1
  }
  memberLoading.value = true
  try {
    const result = await seasonApi.memberPage({
      seasonId: currentSeasonId.value,
      page: memberPagination.page,
      pageSize: memberPagination.pageSize,
      keyword: memberSearch.keyword || undefined,
      eliminated: memberSearch.eliminated ?? undefined,
      captainFlag: memberSearch.captainFlag ?? undefined,
    })
    memberTableData.value = result.list || []
    memberPagination.itemCount = result.total
  }
  finally {
    memberLoading.value = false
  }
}

async function loadAnchorPage(reset = false) {
  if (!currentSeasonId.value) {
    anchorTableData.value = []
    anchorPagination.itemCount = 0
    return
  }
  if (reset) {
    anchorPagination.page = 1
  }
  anchorLoading.value = true
  try {
    const result = await seasonApi.candidateAnchors(currentSeasonId.value, {
      page: anchorPagination.page,
      pageSize: anchorPagination.pageSize,
      anchorId: anchorSearch.anchorId || undefined,
      anchorName: anchorSearch.anchorName || undefined,
      roomId: anchorSearch.roomId || undefined,
      guildName: anchorSearch.guildName || undefined,
      status: anchorSearch.status ?? undefined,
    })
    anchorTableData.value = result.list || []
    anchorPagination.itemCount = result.total
  }
  finally {
    anchorLoading.value = false
  }
}

function resetSeasonSearch() {
  seasonSearch.seasonCode = ''
  seasonSearch.seasonName = ''
  seasonSearch.status = null
  seasonPagination.page = 1
  loadSeasonPage()
}

function resetMemberSearch() {
  memberSearch.keyword = ''
  memberSearch.eliminated = null
  memberSearch.captainFlag = null
  loadMemberPage(true)
}

function resetAnchorSearch() {
  anchorSearch.anchorId = ''
  anchorSearch.anchorName = ''
  anchorSearch.roomId = ''
  anchorSearch.guildName = ''
  anchorSearch.status = 1
  loadAnchorPage(true)
}

function clearAnchorSelection() {
  anchorSelectedRowKeys.value = []
}

function parseMemberImportAnchorIds() {
  const map = new Map<string, string>()
  const tokens = memberFormData.anchorText.split(/[\s,，;；]+/)
  for (const token of tokens) {
    const trimmed = token.trim()
    if (!trimmed) {
      continue
    }
    const normalized = trimmed.replace(/：/g, ':')
    const lower = normalized.toLowerCase()
    let anchorId = normalized
    let output = normalized
    if (lower.startsWith('dy:')) {
      anchorId = normalized.slice(3).trim()
      output = `dy:${anchorId}`
    }
    else if (lower.startsWith('hy:')) {
      anchorId = normalized.slice(3).trim()
      output = `hy:${anchorId}`
    }
    else {
      output = anchorId.trim()
    }
    if (!anchorId.trim()) {
      continue
    }
    const key = output.toLowerCase()
    if (!map.has(key)) {
      map.set(key, output)
    }
  }
  return Array.from(map.values())
}

function handleAnchorPageChange(page: number) {
  anchorPagination.page = page
  loadAnchorPage()
}

function handleAnchorPageSizeChange(pageSize: number) {
  anchorPagination.pageSize = pageSize
  anchorPagination.page = 1
  loadAnchorPage()
}

async function handleSeasonSubmit() {
  await seasonFormRef.value?.validate()
  seasonSubmitLoading.value = true
  try {
    const payload: YunSeason = {
      ...seasonFormData,
      seasonCode: seasonFormData.seasonCode.trim(),
      seasonName: seasonFormData.seasonName?.trim() || '',
      coverImageUrl: seasonFormData.coverImageUrl?.trim() || '',
      remark: seasonFormData.remark?.trim() || '',
      startTime: normalizeDate(seasonFormDate.startTime),
      endTime: normalizeDate(seasonFormDate.endTime),
    }
    if (payload.id) {
      await seasonApi.update(payload)
      message.success('赛季修改成功')
    }
    else {
      await seasonApi.create(payload)
      message.success('赛季新增成功')
    }
    seasonModalVisible.value = false
    resetSeasonForm()
    await loadSeasonPage()
  }
  finally {
    seasonSubmitLoading.value = false
  }
}

async function handleCopySubmit() {
  await copyFormRef.value?.validate()
  if (!copySourceSeasonId.value) {
    return
  }
  copySubmitLoading.value = true
  try {
    await seasonApi.copy(copySourceSeasonId.value, {
      seasonCode: copyFormData.seasonCode.trim(),
      seasonName: copyFormData.seasonName?.trim(),
      coverImageUrl: copyFormData.coverImageUrl?.trim(),
      status: copyFormData.status,
      copyMembers: copyFormData.copyMembers,
      remark: copyFormData.remark?.trim(),
    })
    message.success('复制成功')
    copyModalVisible.value = false
    await loadSeasonPage()
  }
  finally {
    copySubmitLoading.value = false
  }
}

async function handleMemberSubmit() {
  if (!currentSeasonId.value) {
    return
  }
  if (memberImportMode.value === 'select') {
    if (!anchorSelectedRowKeys.value.length) {
      message.warning('请先选择主播')
      return
    }
    await addMembers({
      anchorRefIds: anchorSelectedRowKeys.value,
    })
  }
  else {
    if (!memberImportCount.value) {
      message.warning('请输入房间号')
      return
    }
    await addMembers({
      anchorIds: memberImportAnchorIds.value,
    })
  }
}

async function addMembers(payload: Partial<YunSeasonMemberBatchRequest>) {
  if (!currentSeasonId.value) {
    return
  }
  memberSubmitLoading.value = true
  try {
    const result = await seasonApi.addMembers(currentSeasonId.value, {
      ...payload,
      teamName: memberFormData.teamName.trim() || undefined,
      captainFlag: memberFormData.captainFlag,
    })
    if (result.failCount > 0) {
      dialog.warning({
        title: '加入完成（部分失败）',
        content: () => h('div', { class: 'batch-result' }, [
          h('div', `成功：${result.successCount}/${result.totalCount}，失败：${result.failCount}`),
          h('div', { class: 'batch-result-errors' }, result.errors.slice(0, 10).map(error => h('div', error))),
        ]),
        positiveText: '知道了',
      })
    }
    else {
      message.success(`加入成功：${result.successCount}/${result.totalCount}`)
    }
    memberModalVisible.value = false
    await loadSeasonPage()
  }
  finally {
    memberSubmitLoading.value = false
  }
}

async function handleMemberEditSubmit() {
  await memberEditFormRef.value?.validate()
  memberEditSubmitLoading.value = true
  try {
    await seasonApi.updateMember({
      ...memberEditFormData,
      anchorName: memberEditFormData.anchorName?.trim(),
      teamName: memberEditFormData.teamName?.trim(),
      remark: memberEditFormData.remark?.trim(),
    })
    message.success('成员修改成功')
    memberEditModalVisible.value = false
    resetMemberEditForm()
    await loadMemberPage()
  }
  finally {
    memberEditSubmitLoading.value = false
  }
}

function handleDeleteSeason(row: YunSeasonPageRow) {
  dialog.warning({
    title: '提示',
    content: `确定要删除赛季 ${row.seasonCode} 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await seasonApi.delete([row.id!])
      message.success('删除成功')
      if (currentSeasonId.value === row.id) {
        currentSeasonId.value = null
      }
      await loadSeasonPage()
    },
  })
}

function handleDeleteMembers(memberIds: number[] = memberSelectedIds.value) {
  if (!currentSeasonId.value || !memberIds.length) {
    return
  }
  dialog.warning({
    title: '提示',
    content: `确定删除选中的 ${memberIds.length} 位成员吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await seasonApi.deleteMembers(currentSeasonId.value!, memberIds)
      message.success('删除成功')
      memberSelectedIds.value = []
      await loadSeasonPage()
    },
  })
}

function openResetConfirm() {
  if (!currentSeasonId.value) {
    return
  }
  dialog.warning({
    title: '重置成员状态',
    content: '将把当前赛季所有成员重置为未淘汰状态，开条次数和下次金额清零，确定继续吗？',
    positiveText: '重置',
    negativeText: '取消',
    onPositiveClick: async () => {
      await seasonApi.resetMembers(currentSeasonId.value!)
      message.success('重置成功')
      await loadSeasonPage()
    },
  })
}

watch(currentSeasonId, () => {
  memberSelectedIds.value = []
  memberPagination.page = 1
  if (currentSeasonId.value) {
    void loadMemberPage(true)
  }
})

onMounted(() => {
  void loadSeasonPage()
})
</script>

<style scoped>
.season-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.season-toolbar,
.member-toolbar,
.member-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.season-toolbar-left,
.member-toolbar-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.member-head {
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.member-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.member-title-sub {
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
}

.member-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.season-cell {
  min-width: 0;
}

.season-code {
  font-weight: 700;
  color: #111827;
}

.season-name {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.season-cover {
  display: block;
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
}

.season-cover-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background: #f3f4f6;
  color: #9ca3af;
  font-size: 12px;
}

.member-cover {
  display: block;
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 8px;
}

.member-cover-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f3f4f6;
  color: #9ca3af;
  font-size: 11px;
}

.season-status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 56px;
  height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.season-status.is-on {
  background: #dcfce7;
  color: #166534;
}

.season-status.is-client {
  background: #dbeafe;
  color: #1d4ed8;
}

.season-status.is-off {
  background: #f3f4f6;
  color: #6b7280;
}

.member-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.member-cell-main {
  min-width: 0;
}

.member-name-line {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex-wrap: wrap;
}

.member-name {
  color: #111827;
  font-weight: 700;
}

.member-sub {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.member-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 22px;
  padding: 0 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 22px;
}

.member-badge-captain {
  background: #fef3c7;
  color: #92400e;
}

.member-badge-eliminated {
  background: #fee2e2;
  color: #b91c1c;
}

.member-badge-live {
  background: #dbeafe;
  color: #1d4ed8;
}

.anchor-picker {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 4px;
}

.anchor-import-mode {
  display: flex;
  align-items: center;
  gap: 10px;
}

.anchor-import {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fafafa;
}

.anchor-import-tip {
  font-size: 12px;
}

.anchor-import-footer,
.anchor-picker-select-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.anchor-picker-select-footer {
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.anchor-import-count {
  color: #6b7280;
  font-size: 12px;
}

.anchor-picker-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
}

.anchor-picker-title {
  color: #111827;
  font-size: 15px;
  font-weight: 700;
}

.anchor-picker-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
}

.anchor-picker-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.anchor-picker :deep(.n-data-table-th),
.anchor-picker :deep(.n-data-table-td) {
  padding: 6px 8px;
}

.anchor-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.anchor-cover,
.anchor-cover-empty {
  width: 36px;
  height: 36px;
  border-radius: 6px;
}

.anchor-cover {
  display: block;
  object-fit: cover;
}

.anchor-cover-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  color: #9ca3af;
  font-size: 12px;
}

.anchor-cell-main {
  min-width: 0;
}

.anchor-name {
  color: #111827;
  font-size: 13px;
  font-weight: 700;
}

.anchor-sub {
  margin-top: 2px;
  color: #6b7280;
  font-size: 11px;
}

</style>
