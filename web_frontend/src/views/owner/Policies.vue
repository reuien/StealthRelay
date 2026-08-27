<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api'
import { userStore } from '../../store/user'
import { MULTIPLE_OPTIONS, fmtTime } from '../../utils/time'

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const policies = ref([])
const loadingPolicies = ref(false)
const editingPolicy = ref(null)

const streams = ref([])
const consumers = ref([])
const selectedStream = ref(null)

const streamMap = computed(() => {
  const map = new Map()
  streams.value.forEach((stream) => map.set(String(stream.id), stream))
  return map
})

const policyOverview = computed(() => {
  const consumersCount = new Set(policies.value.map((p) => p.consumerName).filter(Boolean)).size
  const coveredStreams = new Set(policies.value.map((p) => String(p.streamId)).filter(Boolean)).size
  const readyPolicies = policies.value.filter((p) => streamMap.value.get(String(p.streamId))?.uploaded).length
  return [
    { label: '授权策略', value: policies.value.length, tone: 'brand' },
    { label: '授权消费者', value: consumersCount, tone: 'blue' },
    { label: '覆盖交通流', value: coveredStreams, tone: 'green' },
    { label: '可查询策略', value: readyPolicies, tone: 'orange' },
  ]
})

const form = reactive({
  mode: 'privacy',
  streamId: '',
  consumerName: '',
  startTime: null,
  endTime: null,
  minGranularity: 1,
  policyName: '',
})
const rules = {
  streamId: [{ required: true, message: '请选择数据流', trigger: 'change' }],
  consumerName: [{ required: true, message: '请选择授权消费者', trigger: 'change' }],
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
}

async function fetchPolicies() {
  loadingPolicies.value = true
  try {
    const [data, federationData, streamData] = await Promise.all([
      api.listOwnerPolicies(),
      api.listOwnerFederationPolicies(),
      api.listStreams(userStore.state.number),
    ])
    const normalPolicies = (Array.isArray(data) ? data : []).map((item) => ({ ...item, policyType: 'privacy' }))
    const federationPolicies = Array.isArray(federationData) ? federationData : []
    policies.value = [...federationPolicies, ...normalPolicies]
    streams.value = Array.isArray(streamData) ? streamData : []
  } catch (e) {
  } finally {
    loadingPolicies.value = false
  }
}

async function openDialog(mode = 'privacy') {
  editingPolicy.value = null
  form.mode = mode
  form.streamId = ''
  form.consumerName = ''
  form.startTime = null
  form.endTime = null
  form.minGranularity = 1
  form.policyName = ''
  selectedStream.value = null
  formRef.value?.clearValidate()
  dialogVisible.value = true
  try {
    const [s, c] = await Promise.all([
      api.listStreams(userStore.state.number),
      api.consumers(),
    ])
    streams.value = s
    consumers.value = c
  } catch (e) {
  }
}

async function openEdit(row) {
  await openDialog('federation')
  editingPolicy.value = row
  form.streamId = String(row.streamId)
  form.consumerName = row.consumerName
  form.startTime = new Date(Number(row.startTime))
  form.endTime = new Date(Number(row.endTime))
  form.minGranularity = Number(row.minGranularity || 1)
  form.policyName = row.policyName
  selectedStream.value = streams.value.find((s) => String(s.id) === String(row.streamId)) || null
}

function onStreamChange(id) {
  selectedStream.value = streams.value.find((s) => s.id === id) || null
  if (selectedStream.value && !form.policyName) {
    form.policyName = `${selectedStream.value.name}-隐私授权`
  }
}

function policyStream(row) {
  return streamMap.value.get(String(row.streamId))
}

async function handleSubmit() {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (!form.startTime || !form.endTime) {
    ElMessage.warning('请选择策略时间范围')
    return
  }
  if (form.startTime.getTime() >= form.endTime.getTime()) {
    ElMessage.warning('起始时间必须早于结束时间')
    return
  }
  if (selectedStream.value) {
    const { startTime: s, endTime: e } = selectedStream.value
    if (form.startTime.getTime() < Number(s) || form.endTime.getTime() > Number(e)) {
      ElMessage.warning('策略时间范围超出了所选数据流的起止区间')
      return
    }
  }
  submitting.value = true
  try {
    const payload = {
      consumerName: form.consumerName,
      streamId: form.streamId,
      startTime: form.startTime.getTime(),
      endTime: form.endTime.getTime(),
      minGranularity: form.minGranularity,
      policyName: form.policyName,
    }
    if (editingPolicy.value) {
      await api.updateOwnerFederationPolicy(editingPolicy.value.policyId, payload)
    } else if (form.mode === 'federation') {
      await api.createFederationPolicy(payload)
    } else {
      await api.createPolicy(payload)
    }
    ElMessage.success(editingPolicy.value ? '联邦策略更新成功' : (form.mode === 'federation' ? '联邦策略制定成功' : '策略制定成功'))
    dialogVisible.value = false
    await fetchPolicies()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确认删除策略「${row.policyName || row.policyId}」？数据库授权记录会被移除。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  )
  if (row.policyType === 'federation') {
    await api.deleteOwnerFederationPolicy(row.policyId)
  } else {
    await api.deleteOwnerPolicy(row.policyId)
  }
  ElMessage.success(row.policyType === 'federation' ? '联邦策略已删除' : '策略已删除')
  await fetchPolicies()
}

onMounted(fetchPolicies)
</script>

<template>
  <div class="page-card">
    <div class="page-toolbar">
      <h2 class="page-title">隐私策略</h2>
      <div class="toolbar-actions">
        <el-button class="federation-button" @click="openDialog('federation')">
          <el-icon><Connection /></el-icon>制定联邦策略
        </el-button>
        <el-button type="primary" @click="openDialog('privacy')">
          <el-icon><Plus /></el-icon>制定策略
        </el-button>
      </div>
    </div>

    <el-alert
      class="policy-guide"
      type="info"
      :closable="false"
      show-icon
      title="策略说明"
      description="为某条数据流授权指定消费者：消费者查询的时间范围必须落在策略范围内；最小粒度这里传的是倍数（如 1、2、5），不是毫秒值。请先选择一条自己的数据流再制定策略。"
    />

    <div class="policy-overview">
      <div v-for="item in policyOverview" :key="item.label" class="policy-kpi" :class="item.tone">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <el-table v-loading="loadingPolicies" :data="policies" stripe class="policy-table">
      <el-table-column label="类型" width="105">
        <template #default="{ row }">
          <el-tag :type="row.policyType === 'federation' ? 'warning' : 'info'" effect="dark">
            {{ row.policyType === 'federation' ? '联邦策略' : '隐私策略' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="policyName" label="策略名称" min-width="130">
        <template #default="{ row }">{{ row.policyName || `策略 ${row.policyId}` }}</template>
      </el-table-column>
      <el-table-column prop="streamName" label="数据流" min-width="130" />
      <el-table-column prop="consumerName" label="授权消费者" min-width="120" />
      <el-table-column label="授权时间范围" min-width="260">
        <template #default="{ row }">{{ fmtTime(row.startTime) }} ~ {{ fmtTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="minGranularity" label="最小粒度倍数" width="130" />
      <el-table-column label="演示状态" min-width="150">
        <template #default="{ row }">
          <el-tag :type="policyStream(row)?.uploaded ? 'success' : 'warning'" effect="light">
            {{ policyStream(row)?.uploaded ? '可查询' : '待上传数据' }}
          </el-tag>
          <div class="policy-sub">
            {{ policyStream(row)?.uploaded ? 'Consumer 可按授权范围查询' : '先到交通流页面上传 CSV' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button v-if="row.policyType === 'federation'" text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingPolicy ? '编辑联邦策略' : (form.mode === 'federation' ? '制定联邦策略' : '制定隐私策略')"
      width="560px"
      class="traffic-dialog"
    >
      <el-alert
        v-if="form.mode === 'federation'"
        type="info"
        :closable="false"
        show-icon
        class="mode-alert"
        title="联邦策略会写入 policy_mpc，用于消费者在多个车流数据源之间做联合统计查询。"
      />
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="数据流" prop="streamId">
          <el-select
            v-model="form.streamId"
            placeholder="选择数据流"
            style="width: 100%"
            @change="onStreamChange"
          >
            <el-option v-for="s in streams" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="selectedStream" label="流可用区间">
          <span class="range-hint">
            {{ fmtTime(selectedStream.startTime) }} ~ {{ fmtTime(selectedStream.endTime) }}
          </span>
        </el-form-item>

        <el-form-item label="授权消费者" prop="consumerName">
          <el-select v-model="form.consumerName" placeholder="选择消费者" style="width: 100%">
            <el-option v-for="c in consumers" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>

        <el-form-item label="起始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择起始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="最小粒度倍数">
          <el-select v-model="form.minGranularity" style="width: 100%">
            <el-option
              v-for="o in MULTIPLE_OPTIONS"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="策略名称" prop="policyName">
          <el-input v-model="form.policyName" placeholder="请输入策略名称" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editingPolicy ? '保存修改' : (form.mode === 'federation' ? '制定联邦策略' : '制定') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.range-hint {
  color: var(--brand);
  font-size: 14px;
}
.policy-table {
  margin-top: 16px;
  --el-table-bg-color: #091b23;
  --el-table-tr-bg-color: #091b23;
  --el-table-header-bg-color: #102a34;
  --el-table-row-hover-bg-color: #12323c;
  --el-table-border-color: rgba(118, 228, 221, .12);
  border: 1px solid rgba(118, 228, 221, .16);
  border-radius: 2px 14px 2px 14px;
  background: #091b23;
}
.policy-table::before,
.policy-table :deep(.el-table__inner-wrapper::before) {
  background-color: rgba(118, 228, 221, .14);
}
.policy-table :deep(.el-table__inner-wrapper),
.policy-table :deep(.el-table__header-wrapper),
.policy-table :deep(.el-table__body-wrapper),
.policy-table :deep(.el-scrollbar__view) {
  background: #091b23;
}
.policy-table :deep(th.el-table__cell) {
  height: 54px;
  color: #85ded7;
  background: linear-gradient(180deg, #112c36, #0d252e) !important;
  border-bottom-color: rgba(20, 241, 217, .2);
}
.policy-table :deep(td.el-table__cell) {
  color: #c8d8dc;
  background: rgba(8, 27, 35, .96) !important;
  border-bottom-color: rgba(118, 228, 221, .1);
}
.policy-table :deep(.el-table__row--striped td.el-table__cell) {
  background: rgba(13, 37, 46, .96) !important;
}
.policy-table :deep(.el-table__row:hover td.el-table__cell) {
  background: #123640 !important;
}
.policy-table :deep(.el-tag--light) {
  border-color: currentColor;
  background: rgba(8, 26, 33, .82);
}
.policy-guide {
  border: 1px solid rgba(83, 205, 198, .2);
  background:
    linear-gradient(90deg, rgba(20, 241, 217, .075), rgba(20, 241, 217, .025)),
    rgba(5, 22, 29, .8);
}
.policy-guide :deep(.el-alert__icon) {
  color: #4adfd3;
}
.policy-guide :deep(.el-alert__title) {
  color: #d8eeee;
}
.policy-guide :deep(.el-alert__description) {
  color: #82aeb2;
}
.federation-button {
  color: #9ed7d5;
  border-color: rgba(83, 205, 198, .38);
  background: rgba(9, 32, 40, .88);
}
.federation-button:hover,
.federation-button:focus {
  color: #76f0e5;
  border-color: rgba(20, 241, 217, .72);
  background: rgba(20, 241, 217, .09);
}
.policy-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 16px;
}
.policy-kpi {
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 2px 12px 2px 12px;
  background: rgba(3, 18, 24, .62);
}
.policy-kpi span {
  color: var(--text-sub);
  font-size: 12px;
}
.policy-kpi strong {
  display: block;
  margin-top: 6px;
  font: 700 24px 'Chakra Petch', sans-serif;
}
.policy-kpi.brand strong { color: var(--brand); }
.policy-kpi.blue strong { color: #4cb8ff; }
.policy-kpi.green strong { color: #55e38e; }
.policy-kpi.orange strong { color: var(--signal); }
.policy-sub {
  margin-top: 5px;
  color: var(--text-sub);
  font-size: 12px;
}
.toolbar-actions {
  display: flex;
  gap: 10px;
}
.mode-alert {
  margin-bottom: 16px;
}
@media (max-width: 1000px) {
  .policy-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .policy-overview {
    grid-template-columns: 1fr;
  }
}
</style>
