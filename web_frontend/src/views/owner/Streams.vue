<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api'
import { userStore } from '../../store/user'
import { PRECISION_OPTIONS, fmtTime, fmtGranularity } from '../../utils/time'

const list = ref([])
const loading = ref(false)
const selectedStreams = ref([])
const deleting = ref(false)
const uploadingId = ref('')
const uploadDialogVisible = ref(false)
const uploadTarget = ref(null)
const uploadFile = ref(null)
const uploadFiles = ref([])
const lastUploadSummary = ref(null)
const MAX_CSV_UPLOAD_MB = 100
const MAX_CSV_UPLOAD_BYTES = MAX_CSV_UPLOAD_MB * 1024 * 1024

function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const equipments = ref([])

const flowOverview = computed(() => {
  const total = list.value.length
  const uploaded = list.value.filter((x) => x.uploaded).length
  const authorized = list.value.filter((x) => x.authorized).length
  const ready = list.value.filter((x) => x.readyForQuery).length
  return [
    { key: 'created', label: '已创建交通流', value: total, done: total > 0 },
    { key: 'uploaded', label: 'CSV/模拟数据已接入', value: uploaded, done: uploaded > 0 },
    { key: 'authorized', label: '已制定访问策略', value: authorized, done: authorized > 0 },
    { key: 'ready', label: 'Consumer 可查询', value: ready, done: ready > 0 },
  ]
})

const form = reactive({
  name: '',
  description: '城市道路汽车流量数据',
  startTime: null,
  endTime: null,
  minGranularityMillis: 1000,
  granularityMillis: 1000,
  producerEqId: null,
})
const rules = {
  name: [{ required: true, message: '请输入流名称', trigger: 'blur' }],
  producerEqId: [{ required: true, message: '请选择生产者设备', trigger: 'change' }],
}

async function fetchList() {
  loading.value = true
  try {
    list.value = await api.listStreams(userStore.state.number)
  } catch (e) {
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(rows) {
  selectedStreams.value = rows
}

async function handleDeleteSelected() {
  if (!selectedStreams.value.length) {
    ElMessage.warning('请先勾选要删除的数据流')
    return
  }
  const targets = [...selectedStreams.value]
  const names = targets.map((item) => item.name).join('、')
  await ElMessageBox.confirm(
    `确认永久删除 ${targets.length} 条数据流（${names}）？关联的普通策略、联邦策略和已上传 CSV 也会被清理，此操作不可恢复。`,
    '删除交通流',
    {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
    },
  )
  deleting.value = true
  try {
    const results = await Promise.allSettled(targets.map((stream) => api.deleteStream(stream.id)))
    const succeeded = results.filter((result) => result.status === 'fulfilled').length
    const failed = results.length - succeeded
    if (succeeded > 0) {
      ElMessage.success(`已删除 ${succeeded} 条交通流`)
    }
    if (failed > 0) {
      ElMessage.warning(`${failed} 条交通流删除失败，请检查服务状态后重试`)
    }
    selectedStreams.value = []
    await fetchList()
  } finally {
    deleting.value = false
  }
}

async function openDialog() {
  form.name = ''
  form.description = '城市道路汽车流量数据'
  form.startTime = null
  form.endTime = null
  form.minGranularityMillis = 1000
  form.granularityMillis = 1000
  form.producerEqId = null
  formRef.value?.clearValidate()
  dialogVisible.value = true
  try {
    equipments.value = await api.listEquipments(userStore.state.number)
  } catch (e) {
  }
}

function openUploadDialog(row) {
  uploadTarget.value = row
  uploadFile.value = null
  uploadFiles.value = []
  lastUploadSummary.value = null
  uploadDialogVisible.value = true
}

function handleFileChange(file) {
  const raw = file.raw
  if (!raw) return
  if (!raw.name.toLowerCase().endsWith('.csv')) {
    ElMessage.warning('请选择 .csv 文件')
    uploadFiles.value = []
    uploadFile.value = null
    return
  }
  if (raw.size > MAX_CSV_UPLOAD_BYTES) {
    ElMessage.warning(`当前文件 ${formatFileSize(raw.size)}，超过 ${MAX_CSV_UPLOAD_MB}MB 上传限制；请先切分或抽样后再上传`)
    uploadFiles.value = []
    uploadFile.value = null
    return
  }
  uploadFile.value = raw
  uploadFiles.value = [file]
}

function handleFileRemove() {
  uploadFile.value = null
  uploadFiles.value = []
}

async function handleUpload() {
  if (!uploadTarget.value) return
  uploadingId.value = uploadTarget.value.id
  try {
    const result = await api.uploadStream(uploadTarget.value.id, uploadFile.value)
    lastUploadSummary.value = result
    if (result?.source === 'csv') {
      const timeModeText = result.timeMode === 'rebased' ? '，已按数据流起始时间重映射' : ''
      const sampledText = result.sampled
        ? `，原始 ${result.totalRows || 0} 行已抽样为 ${result.validRows || 0} 个上传点`
        : `：有效 ${result.validRows || 0} 行，忽略 ${result.ignoredRows || 0} 行`
      ElMessage.success(`CSV 已解析并写入${sampledText}${timeModeText}`)
    } else {
      ElMessage.success('模拟数据上传成功')
    }
    uploadDialogVisible.value = false
    await fetchList()
  } catch (e) {
  } finally {
    uploadingId.value = ''
  }
}

function flowStage(row) {
  if (row.readyForQuery) return { label: '可查询', type: 'success', percent: 100, text: '已上传并完成授权，Consumer 可以查询' }
  if (row.authorized && !row.uploaded) return { label: '待上传', type: 'warning', percent: 66, text: '已有授权策略，还需要上传 CSV 或模拟数据' }
  if (row.uploaded && !row.authorized) return { label: '待授权', type: 'warning', percent: 66, text: '数据已接入，还需要制定隐私策略' }
  return { label: '初始化', type: 'info', percent: 33, text: '请先上传数据，再制定授权策略' }
}

function uploadModeText(row) {
  if (!row?.uploaded) return '暂无上传记录'
  if (row.uploadSource !== 'csv') return '模拟数据已写入'
  const modeMap = {
    original: '保留原始时间',
    generated: '按流起点生成时间',
    rebased: '按流起点重映射',
    sampled: '已抽样写入',
  }
  return `${row.csvValidRows || 0} 行有效 / ${row.csvIgnoredRows || 0} 行忽略 · ${modeMap[row.csvTimeMode] || '已解析'}`
}

async function handleSubmit() {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (!form.startTime || !form.endTime) {
    ElMessage.warning('请选择起止时间')
    return
  }
  if (form.startTime.getTime() >= form.endTime.getTime()) {
    ElMessage.warning('起始时间必须早于结束时间')
    return
  }
  const eq = equipments.value.find((e) => e.eqId === form.producerEqId)
  if (!eq) {
    ElMessage.warning('请选择生产者设备')
    return
  }
  submitting.value = true
  try {
    await api.createStream({
      name: form.name,
      description: form.description,
      startTime: form.startTime.getTime(),
      endTime: form.endTime.getTime(),
      minGranularityMillis: form.minGranularityMillis,
      granularityMillis: form.granularityMillis,
      producerId: Number(eq.eqId),
      producerName: eq.name,
    })
    ElMessage.success('数据流创建成功')
    dialogVisible.value = false
    await fetchList()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

onMounted(fetchList)
</script>

<template>
  <div class="streams-view">
    <section class="traffic-hero">
      <div>
        <span class="section-code">FLOW MATRIX / LIVE</span>
        <h2>汽车流量数据中枢</h2>
        <p>接入道路检测器、卡口与历史数据集，构建可授权、可查询、可仿真的城市交通数据流。</p>
      </div>
      <div class="traffic-kpis">
        <div><small>交通数据流</small><strong>{{ list.length }}</strong></div>
        <div><small>已接入数据</small><strong>{{ list.filter((x) => x.uploaded).length }}</strong></div>
        <div><small>可仿真节点</small><strong>{{ list.filter((x) => x.readyForQuery).length }}</strong></div>
      </div>
      <div class="lane-visual"><i></i><i></i><i></i><span>LIVE</span></div>
    </section>

  <div class="page-card">
    <div class="page-toolbar">
      <h2 class="page-title">道路流量数据</h2>
      <div class="toolbar-actions">
        <el-button
          type="danger"
          :disabled="selectedStreams.length === 0"
          :loading="deleting"
          @click="handleDeleteSelected"
        >
          <el-icon><Delete /></el-icon>删除所选
        </el-button>
        <el-button type="primary" @click="openDialog">
          <el-icon><Plus /></el-icon>新建交通流
        </el-button>
      </div>
    </div>

    <el-alert
      class="flow-guide"
      type="info"
      :closable="false"
      show-icon
      title="运行链路：接入路侧设备 → 上传汽车流量 CSV → 配置访问授权 → 仿真终端查询与分析。"
    />

    <div class="flow-status-board">
      <div
        v-for="step in flowOverview"
        :key="step.key"
        class="flow-step-card"
        :class="{ done: step.done }"
      >
        <span>{{ step.label }}</span>
        <strong>{{ step.value }}</strong>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="list"
      stripe
      class="traffic-table"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" />
      <el-table-column prop="name" label="路段流标识" min-width="140" />
      <el-table-column prop="description" label="交通数据说明" min-width="170" />
      <el-table-column label="起始时间" min-width="170">
        <template #default="{ row }">{{ fmtTime(row.startTime) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" min-width="170">
        <template #default="{ row }">{{ fmtTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="最小粒度" min-width="110">
        <template #default="{ row }">{{ fmtGranularity(row.minGranularity) }}</template>
      </el-table-column>
      <el-table-column label="数据接入" min-width="150">
        <template #default="{ row }">
          <el-tag v-if="row.uploaded" type="success" effect="light">
            {{ row.uploadSource === 'csv' ? `CSV ${row.csvValidRows || 0} 行` : '模拟数据' }}
          </el-tag>
          <el-tag v-else type="info" effect="light">未上传</el-tag>
          <div class="row-sub">{{ uploadModeText(row) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="授权状态" min-width="120">
        <template #default="{ row }">
          <el-tag v-if="row.authorized" type="success" effect="light">已授权 {{ row.authorizationCount }}</el-tag>
          <el-tag v-else type="warning" effect="light">未授权</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="仿真就绪" min-width="105">
        <template #default="{ row }">
          <el-tag :type="row.readyForQuery ? 'success' : 'danger'" effect="light">
            {{ row.readyForQuery ? 'READY' : 'STANDBY' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="演示链路" min-width="210">
        <template #default="{ row }">
          <div class="stage-line">
            <el-tag :type="flowStage(row).type" effect="dark">{{ flowStage(row).label }}</el-tag>
            <el-progress :percentage="flowStage(row).percent" :show-text="false" />
          </div>
          <div class="row-sub">{{ flowStage(row).text }}</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            :loading="uploadingId === row.id"
            @click="openUploadDialog(row)"
          >
            <el-icon><Upload /></el-icon>上传数据
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" width="660px" class="traffic-dialog">
      <template #header>
        <div class="dialog-title">
          <span>FLOW CONFIGURATION</span>
          <strong>创建汽车流量数据流</strong>
        </div>
      </template>
      <div class="flow-dialog-shell">
        <aside class="dialog-steps">
          <div><b>01</b><span>路段标识</span></div>
          <div><b>02</b><span>时间窗口</span></div>
          <div><b>03</b><span>粒度与设备</span></div>
        </aside>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="flow-form">
          <el-form-item label="路段标识" prop="name">
            <el-input v-model="form.name" placeholder="例如：G107-NORTH-01 / RING-EAST-03" clearable />
          </el-form-item>
          <el-form-item label="数据说明">
            <el-input v-model="form.description" placeholder="例如：早高峰北向车流量检测" clearable />
          </el-form-item>
          <div class="form-split">
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
          </div>
          <div class="form-split">
            <el-form-item label="最小粒度">
              <el-select v-model="form.minGranularityMillis" style="width: 100%">
                <el-option
                  v-for="o in PRECISION_OPTIONS"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="展示粒度">
              <el-select v-model="form.granularityMillis" style="width: 100%">
                <el-option
                  v-for="o in PRECISION_OPTIONS"
                  :key="o.value"
                  :label="o.label"
                  :value="o.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="生产者设备" prop="producerEqId">
            <el-select v-model="form.producerEqId" placeholder="选择已注册路侧设备" style="width: 100%">
              <el-option
                v-for="eq in equipments"
                :key="eq.eqId"
                :label="`${eq.name}｜${eq.ip}:${eq.port}`"
                :value="eq.eqId"
              />
            </el-select>
          </el-form-item>
          <div class="dialog-note">
            创建后请继续执行“上传 CSV 数据”和“配置授权”，consumer 才能看到查询结果。
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">创建交通流</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" width="600px" class="traffic-dialog">
      <template #header>
        <div class="dialog-title">
          <span>CSV INGESTION</span>
          <strong>接入汽车流量数据</strong>
        </div>
      </template>
      <div class="upload-target" v-if="uploadTarget">
        <div>
          <small>当前交通流</small>
          <strong>{{ uploadTarget.name }}</strong>
        </div>
        <el-tag :type="uploadTarget.readyForQuery ? 'success' : 'warning'" effect="light">
          {{ uploadTarget.readyForQuery ? '可查询' : '待完成链路' }}
        </el-tag>
      </div>
      <el-alert
        class="dialog-hint"
        type="info"
        :closable="false"
        show-icon
        :title="`支持 timestamp,value 或单列 value；单文件建议不超过 ${MAX_CSV_UPLOAD_MB}MB，超大文件请先切分或抽样。`"
      />
      <el-alert
        v-if="lastUploadSummary"
        class="dialog-hint"
        type="success"
        :closable="false"
        show-icon
        :title="`最近上传：${lastUploadSummary.source === 'csv' ? 'CSV 解析成功' : '模拟数据写入成功'}；有效 ${lastUploadSummary.validRows || 0} 行，忽略 ${lastUploadSummary.ignoredRows || 0} 行，总计 ${lastUploadSummary.totalRows || 0} 行。`"
      />
      <el-upload
        class="csv-upload"
        drag
        accept=".csv"
        :auto-upload="false"
        :limit="1"
        :file-list="uploadFiles"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽交通流量 CSV 到这里，或 <em>点击选择本地文件</em></div>
        <template #tip>
          <div class="el-upload__tip">
            <span>推荐列：timestamp,value / vehicle_count / traffic_volume。</span>
            <span v-if="uploadFile" class="file-pill">已选择：{{ uploadFile.name }}（{{ formatFileSize(uploadFile.size) }}）</span>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploadingId === uploadTarget?.id" @click="handleUpload">
          {{ uploadFile ? '上传 CSV 数据' : '使用模拟数据上传' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
  </div>
</template>

<style scoped>
.streams-view {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.traffic-hero {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 170px;
  padding: 28px 34px;
  border: 1px solid var(--line);
  border-radius: 3px 22px 3px 22px;
  background:
    linear-gradient(100deg, rgba(12, 39, 48, .98), rgba(5, 21, 28, .92)),
    var(--bg-panel);
}
.traffic-hero::after {
  content: '';
  position: absolute;
  width: 400px; height: 400px;
  right: -180px; top: -260px;
  border: 1px solid rgba(20, 241, 217, .16);
  border-radius: 50%;
  box-shadow: 0 0 0 50px rgba(20, 241, 217, .02), 0 0 0 100px rgba(20, 241, 217, .015);
}
.section-code {
  color: var(--brand);
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: .2em;
}
.traffic-hero h2 {
  margin: 8px 0;
  font: 600 30px 'Chakra Petch', 'Noto Sans SC', sans-serif;
}
.traffic-hero p { margin: 0; color: var(--text-sub); max-width: 620px; }
.traffic-kpis {
  display: flex;
  gap: 12px;
  margin-right: 90px;
  position: relative;
  z-index: 1;
}
.traffic-kpis div {
  min-width: 112px;
  padding: 15px;
  border: 1px solid var(--line);
  background: rgba(2, 15, 20, .48);
}
.traffic-kpis small { display: block; color: var(--text-sub); font-size: 11px; }
.traffic-kpis strong { color: var(--brand); font: 700 28px 'Chakra Petch', sans-serif; }
.lane-visual { position: absolute; right: 28px; bottom: 20px; width: 185px; height: 45px; opacity: .65; }
.lane-visual i { display:block; height:1px; margin:11px 0; background:repeating-linear-gradient(90deg,var(--brand) 0 16px,transparent 16px 28px); }
.lane-visual span { position:absolute; right:0; top:-15px; color:var(--signal); font:9px 'Chakra Petch',sans-serif; }
.csv-upload {
  margin-top: 16px;
}
.flow-guide {
  margin-bottom: 16px;
}
.flow-status-board {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.flow-step-card {
  position: relative;
  overflow: hidden;
  padding: 14px 16px;
  border: 1px solid rgba(118, 228, 221, .16);
  border-radius: 2px 12px 2px 12px;
  background: rgba(3, 18, 24, .58);
}
.flow-step-card::after {
  content: '';
  position: absolute;
  inset: auto 0 0 0;
  height: 2px;
  background: rgba(118, 228, 221, .18);
}
.flow-step-card.done::after {
  background: linear-gradient(90deg, var(--brand), var(--signal));
  box-shadow: 0 0 14px rgba(20, 241, 217, .35);
}
.flow-step-card span {
  color: var(--text-sub);
  font-size: 12px;
}
.flow-step-card strong {
  display: block;
  margin-top: 6px;
  color: var(--brand);
  font: 700 24px 'Chakra Petch', sans-serif;
}
.row-sub {
  margin-top: 5px;
  color: var(--text-sub);
  font-size: 12px;
}
.stage-line {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 10px;
  align-items: center;
}
.traffic-table {
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
.traffic-table::before,
.traffic-table :deep(.el-table__inner-wrapper::before) {
  background-color: rgba(118, 228, 221, .14);
}
.traffic-table :deep(.el-table__inner-wrapper),
.traffic-table :deep(.el-table__header-wrapper),
.traffic-table :deep(.el-table__body-wrapper),
.traffic-table :deep(.el-scrollbar__view) {
  background: #091b23;
}
.traffic-table :deep(th.el-table__cell) {
  height: 54px;
  color: #85ded7;
  background: linear-gradient(180deg, #112c36, #0d252e) !important;
  border-bottom-color: rgba(20, 241, 217, .2);
}
.traffic-table :deep(td.el-table__cell) {
  color: #c8d8dc;
  background: rgba(8, 27, 35, .96) !important;
  border-bottom-color: rgba(118, 228, 221, .1);
}
.traffic-table :deep(.el-table__row--striped td.el-table__cell) {
  background: rgba(13, 37, 46, .96) !important;
}
.traffic-table :deep(.el-table__row:hover td.el-table__cell) {
  background: #123640 !important;
}
.traffic-table :deep(.el-table-fixed-column--right) {
  box-shadow: -10px 0 18px rgba(0, 0, 0, .16);
}
.traffic-table :deep(.el-checkbox__inner) {
  border-color: rgba(132, 207, 204, .5);
  background: rgba(3, 15, 21, .7);
}
.traffic-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner),
.traffic-table :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
  border-color: var(--brand);
  background: var(--brand);
}
.traffic-table :deep(.el-tag--light) {
  border-color: currentColor;
  background: rgba(8, 26, 33, .82);
}
.traffic-table :deep(.el-button.is-link) {
  padding-inline: 8px;
  color: #43dfd1;
  background: rgba(20, 241, 217, .055);
}
.traffic-table :deep(.el-button.is-link:hover) {
  color: #8afff4;
  background: rgba(20, 241, 217, .11);
}
.dialog-title span {
  display: block;
  color: var(--brand);
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: .22em;
}
.dialog-title strong {
  display: block;
  margin-top: 4px;
  font-size: 18px;
}
.flow-dialog-shell {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 18px;
}
.dialog-steps {
  position: relative;
  padding: 12px;
  border: 1px solid var(--line);
  background:
    linear-gradient(180deg, rgba(20,241,217,.08), rgba(20,241,217,.02));
}
.dialog-steps::before {
  content: '';
  position: absolute;
  left: 26px;
  top: 32px;
  bottom: 32px;
  width: 1px;
  background: rgba(20,241,217,.28);
}
.dialog-steps div {
  position: relative;
  display: flex;
  gap: 10px;
  align-items: center;
  margin: 0 0 22px;
}
.dialog-steps b {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border: 1px solid var(--brand);
  border-radius: 50%;
  color: var(--brand);
  background: #07171e;
  font: 700 11px 'Chakra Petch', sans-serif;
}
.dialog-steps span {
  color: var(--text-sub);
  font-size: 12px;
}
.flow-form :deep(.el-form-item) {
  margin-bottom: 16px;
}
.form-split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.dialog-note {
  margin-top: 4px;
  padding: 11px 13px;
  border-left: 2px solid var(--brand);
  color: var(--text-sub);
  background: rgba(20, 241, 217, .05);
  font-size: 12px;
}
.dialog-hint {
  margin-bottom: 14px;
}
.upload-target {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  padding: 14px 16px;
  border: 1px solid var(--line);
  background:
    radial-gradient(circle at 12% 20%, rgba(20,241,217,.13), transparent 24%),
    rgba(3, 18, 25, .72);
}
.upload-target small {
  display: block;
  color: var(--text-sub);
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: .16em;
}
.upload-target strong {
  display: block;
  margin-top: 4px;
  color: var(--text-main);
}
.csv-upload :deep(.el-upload-dragger) {
  border-color: rgba(20, 241, 217, .35);
  background:
    linear-gradient(135deg, rgba(20,241,217,.07), rgba(255,176,32,.03)),
    rgba(4, 18, 25, .72);
}
.csv-upload :deep(.el-upload-dragger:hover) {
  border-color: var(--brand);
  box-shadow: 0 0 24px rgba(20, 241, 217, .12);
}
.file-pill {
  display: inline-flex;
  margin-left: 8px;
  padding: 3px 8px;
  border: 1px solid rgba(20,241,217,.25);
  color: var(--brand);
  background: rgba(20,241,217,.06);
}
@media (max-width: 1050px) {
  .traffic-kpis { display: none; }
  .flow-status-board { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 760px) {
  .flow-dialog-shell,
  .form-split {
    grid-template-columns: 1fr;
  }
  .flow-status-board { grid-template-columns: 1fr; }
  .dialog-steps {
    display: none;
  }
}
</style>
