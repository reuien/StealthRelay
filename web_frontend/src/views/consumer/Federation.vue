<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import { fmtTime } from '../../utils/time'

const policies = ref([])
const selected = ref([])
const loading = ref(false)
const queried = ref(false)
const form = reactive({
  type: '',
  startTime: null,
  endTime: null,
})
const result = reactive({
  rows: [],
  statistics: null,
  tokenIssued: false,
  credential: null,
})

const commonRange = computed(() => {
  const rows = policies.value.filter((item) => selected.value.includes(item.policyId))
  if (rows.length < 2) return null
  const start = Math.max(...rows.map((item) => Number(item.startTime)))
  const end = Math.min(...rows.map((item) => Number(item.endTime)))
  return start < end ? { start, end } : null
})

const cards = computed(() => {
  const s = result.statistics
  if (!s) return []
  return [
    { label: '总车辆数', value: formatInt(s.sum), icon: 'Van' },
    { label: '总计数', value: formatInt(s.count), icon: 'Coin' },
    { label: '总平方和', value: formatInt(s.squareSum), icon: 'DataAnalysis' },
    { label: '总平均值', value: Number(s.mean || 0).toFixed(2), icon: 'TrendCharts' },
    { label: '总标准差', value: Number(s.std || 0).toFixed(2), icon: 'Histogram' },
    { label: '总方差', value: Number(s.variance || 0).toFixed(2), icon: 'Operation' },
  ]
})

async function loadPolicies() {
  policies.value = await api.listFederationPolicies(form.type || undefined)
}

function applyCommonRange() {
  if (!commonRange.value) {
    ElMessage.warning('至少选择两个存在公共时间范围的联邦策略')
    return
  }
  form.startTime = new Date(commonRange.value.start)
  form.endTime = new Date(commonRange.value.end)
}

async function handleQuery() {
  if (selected.value.length < 2) {
    ElMessage.warning('请选择两个或以上联邦策略')
    return
  }
  if (!form.startTime || !form.endTime) applyCommonRange()
  if (!form.startTime || !form.endTime) return
  loading.value = true
  try {
    const data = await api.federationQuery({
      policyIds: selected.value,
      startTime: form.startTime.getTime(),
      endTime: form.endTime.getTime(),
    })
    result.rows = data.rows || []
    result.statistics = data.statistics || null
    result.tokenIssued = Boolean(data.tokenIssued)
    result.credential = data.credential || null
    queried.value = true
  } finally {
    loading.value = false
  }
}

function onSelectionChange(rows) {
  selected.value = rows.map((item) => item.policyId)
  if (commonRange.value) applyCommonRange()
}

function formatInt(value) {
  return Number(value || 0).toLocaleString('en-US')
}

onMounted(loadPolicies)
</script>

<template>
  <div class="federation-page">
    <div class="page-card">
      <div class="page-toolbar">
        <h2 class="page-title">联邦查询</h2>
        <div class="toolbar-actions">
          <el-input v-model="form.type" placeholder="流类型筛选，如 车辆流量" clearable @keyup.enter="loadPolicies" />
          <el-button class="query-policy-button" @click="loadPolicies">查询策略</el-button>
          <el-button type="primary" :loading="loading" @click="handleQuery">联邦查询</el-button>
        </div>
      </div>
      <el-table class="federation-table" :data="policies" height="340" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="ownerName" label="执行者" width="120" />
        <el-table-column prop="policyName" label="策略" min-width="180" />
        <el-table-column prop="streamName" label="流ID/名称" min-width="220">
          <template #default="{ row }">{{ row.streamName || '交通流' }} · {{ row.streamId }}</template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ fmtTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="{ row }">{{ fmtTime(row.endTime) }}</template>
        </el-table-column>
      </el-table>
      <div class="query-row">
        <el-alert
          class="common-range-alert"
          :closable="false"
          type="info"
          :title="commonRange ? `公共授权范围：${fmtTime(commonRange.start)} ~ ${fmtTime(commonRange.end)}` : '请选择至少两个联邦策略以计算公共授权范围'"
        />
        <el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间" />
        <el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间" />
        <el-tag :type="result.tokenIssued ? 'success' : 'warning'" effect="dark">
          {{ result.tokenIssued ? '控制器已颁发联邦令牌' : 'CSV 统计兜底模式' }}
        </el-tag>
      </div>
      <div v-if="result.credential" class="credential-strip">
        <div>
          <small>FEDERATION QUERY CREDENTIAL</small>
          <strong>联邦查询执行凭证已生成，正在提交链上确认</strong>
        </div>
        <code>{{ result.credential.traceId }}</code>
        <el-tag type="warning" effect="dark">{{ result.credential.status }}</el-tag>
      </div>
    </div>

    <div class="page-card result-card" v-loading="loading">
      <template v-if="queried">
        <div class="stat-grid">
          <div v-for="card in cards" :key="card.label" class="stat-card">
            <el-icon><component :is="card.icon" /></el-icon>
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
          </div>
        </div>
        <el-table class="federation-table" :data="result.rows">
          <el-table-column prop="ownerName" label="拥有者" width="120" />
          <el-table-column prop="streamName" label="交通流" min-width="160" />
          <el-table-column prop="policyId" label="策略ID" min-width="150" />
          <el-table-column prop="count" label="样本量" width="110" />
          <el-table-column label="平均值" width="120">
            <template #default="{ row }">{{ Number(row.average || 0).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </template>
      <el-empty v-else description="选择多个授权交通流后执行联邦查询" />
    </div>
  </div>
</template>

<style scoped>
.federation-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.query-row {
  display: grid;
  grid-template-columns: 1fr 210px 210px 170px;
  gap: 12px;
  align-items: center;
  margin-top: 16px;
}
.federation-table {
  --el-table-bg-color: #091b23;
  --el-table-tr-bg-color: #091b23;
  --el-table-header-bg-color: #102a34;
  --el-table-row-hover-bg-color: #12323c;
  --el-table-current-row-bg-color: #12323c;
  --el-table-border-color: rgba(118, 228, 221, .12);
  border: 1px solid rgba(118, 228, 221, .16);
  border-radius: 2px 14px 2px 14px;
  background: #091b23;
}
.federation-table::before,
.federation-table :deep(.el-table__inner-wrapper::before) {
  background-color: rgba(118, 228, 221, .14);
}
.federation-table :deep(.el-table__inner-wrapper),
.federation-table :deep(.el-table__header-wrapper),
.federation-table :deep(.el-table__body-wrapper),
.federation-table :deep(.el-scrollbar__view) {
  background: #091b23;
}
.federation-table :deep(th.el-table__cell) {
  height: 54px;
  color: #85ded7;
  background: linear-gradient(180deg, #112c36, #0d252e) !important;
  border-bottom-color: rgba(20, 241, 217, .2);
}
.federation-table :deep(td.el-table__cell) {
  color: #c8d8dc;
  background: rgba(8, 27, 35, .96) !important;
  border-bottom-color: rgba(118, 228, 221, .1);
  transition: background-color .18s ease, box-shadow .18s ease;
}
.federation-table :deep(.el-table__row:nth-child(even) td.el-table__cell) {
  background: rgba(13, 37, 46, .96) !important;
}
.federation-table :deep(.el-table__row:hover td.el-table__cell) {
  background: #123640 !important;
}
.federation-table :deep(.el-table__row:has(.el-checkbox__input.is-checked) td.el-table__cell) {
  background: rgba(15, 61, 68, .96) !important;
  box-shadow: inset 0 1px rgba(20, 241, 217, .08), inset 0 -1px rgba(20, 241, 217, .08);
}
.federation-table :deep(.el-table__row:has(.el-checkbox__input.is-checked) td.el-table__cell:first-child) {
  box-shadow: inset 3px 0 var(--brand);
}
.federation-table :deep(.el-checkbox__inner) {
  border-color: rgba(132, 207, 204, .5);
  background: rgba(3, 15, 21, .72);
}
.federation-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner),
.federation-table :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  border-color: var(--brand);
  background: var(--brand);
}
.common-range-alert {
  border: 1px solid rgba(83, 205, 198, .2);
  background:
    linear-gradient(90deg, rgba(20, 241, 217, .075), rgba(20, 241, 217, .025)),
    rgba(5, 22, 29, .88);
}
.common-range-alert :deep(.el-alert__title) {
  color: #94b9bc;
}
.query-policy-button {
  color: #9ed7d5;
  border-color: rgba(83, 205, 198, .38);
  background: rgba(9, 32, 40, .88);
}
.query-policy-button:hover,
.query-policy-button:focus {
  color: #76f0e5;
  border-color: rgba(20, 241, 217, .72);
  background: rgba(20, 241, 217, .09);
}
.credential-strip {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(260px, auto) auto;
  gap: 16px;
  align-items: center;
  margin-top: 14px;
  padding: 13px 16px;
  border: 1px solid rgba(20, 241, 217, .28);
  border-left: 3px solid var(--brand);
  background: linear-gradient(90deg, rgba(20, 241, 217, .09), rgba(7, 28, 36, .75));
}
.credential-strip small {
  display: block;
  color: var(--brand);
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: .15em;
}
.credential-strip strong { display: block; margin-top: 4px; color: var(--text-main); }
.credential-strip code { color: #87b8bb; font-size: 12px; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-bottom: 18px;
}
.stat-card {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 2px 14px 2px 14px;
  background: linear-gradient(160deg, rgba(20, 241, 217, .08), rgba(3, 18, 24, .76));
}
.stat-card .el-icon {
  color: var(--brand);
  font-size: 24px;
}
.stat-card span {
  display: block;
  color: var(--text-sub);
  margin-top: 8px;
}
.stat-card strong {
  display: block;
  margin-top: 4px;
  color: var(--brand);
  font: 700 22px 'Chakra Petch', sans-serif;
}
.result-card {
  min-height: 340px;
}
@media (max-width: 1200px) {
  .query-row,
  .stat-grid {
    grid-template-columns: 1fr;
  }
  .toolbar-actions {
    flex-wrap: wrap;
  }
}
</style>
