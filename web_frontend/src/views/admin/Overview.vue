<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api'

const active = ref('streams')
const loading = ref(false)
const summary = ref({})
const users = ref([])
const streams = ref([])
const policies = ref([])
const policyDetail = ref(null)
const policyDrawer = ref(false)
const policyLoading = ref(false)
const logs = ref([])
const trace = ref(null)
const selectedStream = ref(null)
const auditFilter = ref('')
const anchors = ref([])
const anchorStatus = ref('')
const anchorDetail = ref(null)
const anchorDrawer = ref(false)
const anchorLoading = ref(false)
let anchorTimer = null

const metricCards = computed(() => [
  { label: '平台用户', value: summary.value.users || 0, code: 'IDENTITIES' },
  { label: '数据流', value: summary.value.streams || 0, code: 'STREAMS' },
  { label: '访问策略', value: summary.value.policies || 0, code: 'POLICIES' },
  { label: '审计事件', value: summary.value.auditEvents || 0, code: 'AUDIT EVENTS' },
  { label: '已禁用账号', value: summary.value.disabledUsers || 0, code: 'LOCKED', danger: true },
  { label: '链上已确认', value: `${summary.value.confirmedAnchors || 0}/${summary.value.anchors || 0}`, code: 'ANCHORS', danger: Boolean(summary.value.failedAnchors) },
])

const latestAnchor = computed(() => anchors.value[0] || null)
const credentialSteps = computed(() => {
  const item = latestAnchor.value
  const hashed = Boolean(item?.payload_sha256)
  const broadcast = Boolean(item?.transaction_hash)
  const confirmed = item?.status === 'CONFIRMED' && Boolean(item?.block_number)
  return [
    { code: '01', title: '授权任务', note: item ? businessLabel(item.business_type) : '等待策略授权', done: Boolean(item) },
    { code: '02', title: '生成指纹', note: hashed ? shortHash(item.payload_sha256) : '规范化载荷与 SHA-256', done: hashed },
    { code: '03', title: '广播交易', note: broadcast ? shortHash(item.transaction_hash) : '等待签名并提交', done: broadcast, live: item?.status === 'PROCESSING' },
    { code: '04', title: '区块确认', note: confirmed ? `BLOCK ${item.block_number}` : anchorLabel(item?.status, item?.last_error), done: confirmed },
    { code: '05', title: '凭证生成', note: confirmed ? '交易哈希 + 区块号' : '等待链上确认', done: confirmed },
  ]
})
const recentBlocks = computed(() => anchors.value.filter((a) => a.status === 'CONFIRMED' && a.block_number).slice(0, 6))

function fmt(time) {
  return time ? new Date(Number(time)).toLocaleString('zh-CN', { hour12: false }) : '—'
}
function num(value) { return Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 2 }) }
function identity(row) { return row.identity === '超级管理员' ? '超级管理员' : row.identity }
function shortHash(value) { return value ? `${value.slice(0, 9)}…${value.slice(-7)}` : '—' }
function businessLabel(type) {
  if (type === 'POLICY') return 'CSV + 普通策略授权'
  if (type === 'FEDERATION_POLICY') return 'CSV + 联邦策略授权'
  if (type === 'FEDERATION_QUERY_EXECUTION') return '联邦查询执行凭证'
  if (type === 'COMPUTATION_TRACE') return '聚合计算溯源'
  return type || '等待业务事件'
}

async function loadAll() {
  loading.value = true
  try {
    const [s, u, st, p, a, bc] = await Promise.all([
      api.adminSummary(), api.adminUsers(), api.adminStreams(), api.adminPolicies(), api.adminAudit({ limit: 300 }), api.adminAnchors({ limit: 500 }),
    ])
    summary.value = s; users.value = u; streams.value = st; policies.value = p; logs.value = a; anchors.value = bc
  } finally { loading.value = false }
}

async function openTrace(row) {
  selectedStream.value = row; active.value = 'trace'; trace.value = null; loading.value = true
  try { trace.value = await api.adminTrace(row.id) } finally { loading.value = false }
}

async function toggleUser(row) {
  const disabled = !Boolean(row.disabled)
  const action = disabled ? '禁用' : '启用'
  await ElMessageBox.confirm(`${action}账号 ${row.usr_name}（${row.number}）？此操作会写入审计日志。`, `${action}用户`, { type: 'warning' })
  await api.adminSetUserStatus(row.number, disabled, `${action} by super admin`)
  ElMessage.success(`账号已${action}`); await loadAll()
}

async function removeUser(row) {
  await ElMessageBox.confirm(`永久删除用户 ${row.usr_name} 及其关联数据流和策略？审计记录仍会保留。`, '高风险操作', { type: 'error', confirmButtonText: '确认永久删除' })
  await api.adminDeleteUser(row.number); ElMessage.success('用户及关联资源已删除'); await loadAll()
}

async function removeStream(row) {
  await ElMessageBox.confirm(`删除数据流「${row.name}」及其策略、历史和绑定？底层远程密文存储需单独核验。`, '删除数据流', { type: 'error', confirmButtonText: '确认删除' })
  await api.adminDeleteStream(row.id); ElMessage.success('数据流记录已删除'); trace.value = null; await loadAll()
}

async function removePolicy(row) {
  await ElMessageBox.confirm(`删除策略 ${row.policy_id}？`, '删除策略', { type: 'warning' })
  await api.adminDeletePolicy(row.policy_type, row.policy_id); ElMessage.success('策略已删除'); await loadAll()
}

async function openPolicy(row) {
  policyDrawer.value = true
  policyDetail.value = null
  policyLoading.value = true
  try { policyDetail.value = await api.adminPolicyDetail(row.policy_type, row.policy_id) }
  finally { policyLoading.value = false }
}

async function refreshLogs() { logs.value = await api.adminAudit({ limit: 500, action: auditFilter.value || undefined }) }
async function refreshAnchors() { anchors.value = await api.adminAnchors({ limit: 500, status: anchorStatus.value || undefined }) }
async function openAnchor(row) {
  anchorDrawer.value = true; anchorDetail.value = null; anchorLoading.value = true
  try { anchorDetail.value = await api.adminAnchorDetail(row.id) } finally { anchorLoading.value = false }
}
async function retryAnchor(row) {
  await api.adminRetryAnchor(row.id); ElMessage.success('已重新加入存证队列'); await refreshAnchors(); summary.value = await api.adminSummary()
}
function anchorLabel(status, error) {
  if (status === 'CONFIRMED') return '已确认'
  if (status === 'PROCESSING') return '处理中'
  if (status === 'FAILED') return '失败'
  if (status === 'WAITING_CONFIG') return '待配置'
  return error?.includes('未配置') ? '待配置' : '待上链'
}
function anchorTag(status) { return status === 'CONFIRMED' ? 'success' : status === 'FAILED' ? 'danger' : status === 'PROCESSING' ? 'warning' : 'info' }
onMounted(() => {
  loadAll()
  anchorTimer = window.setInterval(async () => {
    if (active.value !== 'anchors') return
    try {
      await refreshAnchors()
      summary.value = await api.adminSummary()
    } catch (e) {
    }
  }, 2500)
})
onBeforeUnmount(() => anchorTimer && window.clearInterval(anchorTimer))
</script>

<template>
  <div class="admin-shell" v-loading="loading">
    <section class="command-head">
      <div>
        <span class="kicker"><i></i> ROOT ACCESS / TRACEABLE OPERATIONS</span>
        <h1>全域审计与计算溯源</h1>
        <p>从异常流量点反向追踪生产节点、上传源、授权策略与密文聚合路径。</p>
      </div>
      <div class="integrity"><span></span><div><b>AUDIT CHAIN ACTIVE</b><small>审计日志无删除接口</small></div></div>
    </section>

    <section class="metrics">
      <article v-for="card in metricCards" :key="card.code" :class="{ danger: card.danger }">
        <small>{{ card.code }}</small><strong>{{ card.value }}</strong><span>{{ card.label }}</span>
      </article>
    </section>

    <nav class="rail">
      <button v-for="item in [{k:'streams',t:'数据流与溯源'},{k:'users',t:'用户控制'},{k:'policies',t:'全局策略'},{k:'anchors',t:'区块链存证'},{k:'audit',t:'详细日志'},{k:'trace',t:'计算透视'}]"
              :key="item.k" :class="{ active: active === item.k }" @click="active = item.k">{{ item.t }}</button>
      <el-button text type="primary" @click="loadAll"><el-icon><Refresh /></el-icon>刷新全域状态</el-button>
    </nav>

    <section v-if="active === 'streams'" class="console-card">
      <header><div><small>GLOBAL STREAM REGISTRY</small><h2>任意用户创建的数据流</h2></div><span>{{ streams.length }} STREAMS</span></header>
      <el-table :data="streams" height="520">
        <el-table-column prop="name" label="数据流" min-width="150" />
        <el-table-column label="归属 / 生产者" min-width="190"><template #default="{row}"><b>{{ row.owner_name || '未知' }}</b><small class="sub">{{ row.owner_id }} · {{ row.producer_name || '未绑定' }}</small></template></el-table-column>
        <el-table-column label="时间范围" min-width="260"><template #default="{row}">{{ fmt(row.starttime) }}<small class="sub">→ {{ fmt(row.endtime) }}</small></template></el-table-column>
        <el-table-column label="策略" width="100"><template #default="{row}">{{ row.policy_count }} + {{ row.federation_count }}</template></el-table-column>
        <el-table-column label="操作" width="220" fixed="right"><template #default="{row}"><el-button size="small" type="primary" @click="openTrace(row)">异常与计算</el-button><el-button size="small" type="danger" text @click="removeStream(row)">删除</el-button></template></el-table-column>
      </el-table>
    </section>

    <section v-else-if="active === 'users'" class="console-card">
      <header><div><small>IDENTITY CONTROL</small><h2>用户与权限状态</h2></div></header>
      <el-table :data="users" height="520">
        <el-table-column prop="number" label="账号" width="130" /><el-table-column prop="usr_name" label="用户名" />
        <el-table-column label="角色"><template #default="{row}"><el-tag :type="row.identity === '超级管理员' ? 'danger' : 'info'">{{ identity(row) }}</el-tag></template></el-table-column>
        <el-table-column prop="stream_count" label="数据流" width="100" />
        <el-table-column label="状态" width="110"><template #default="{row}"><span :class="row.disabled ? 'status-off' : 'status-on'">{{ row.disabled ? '已禁用' : '正常' }}</span></template></el-table-column>
        <el-table-column label="操作" width="230"><template #default="{row}"><template v-if="row.identity !== '超级管理员'"><el-button size="small" @click="toggleUser(row)">{{ row.disabled ? '启用' : '禁用' }}</el-button><el-button size="small" type="danger" text @click="removeUser(row)">删除</el-button></template><span v-else class="root-lock">唯一账号 · 受保护</span></template></el-table-column>
      </el-table>
    </section>

    <section v-else-if="active === 'policies'" class="console-card">
      <header><div><small>POLICY MATRIX</small><h2>普通策略与联邦策略</h2></div></header>
      <el-table :data="policies" height="520">
        <el-table-column label="类型" width="120"><template #default="{row}"><el-tag>{{ row.policy_type === 'normal' ? '普通' : '联邦' }}</el-tag></template></el-table-column>
        <el-table-column prop="policy_id" label="策略 ID" min-width="180" /><el-table-column prop="stream_id" label="流 ID" min-width="180" />
        <el-table-column label="授权关系" min-width="180"><template #default="{row}">{{ row.owner_name }} → {{ row.consumer_name }}</template></el-table-column>
        <el-table-column label="授权时间" min-width="240"><template #default="{row}">{{ fmt(row.p_starttime) }}<small class="sub">→ {{ fmt(row.p_endtime) }}</small></template></el-table-column>
        <el-table-column label="操作" width="190"><template #default="{row}"><el-button type="primary" size="small" @click="openPolicy(row)">查看详情</el-button><el-button type="danger" text @click="removePolicy(row)">删除</el-button></template></el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="policyDrawer" size="min(760px, 92vw)" class="policy-drawer" destroy-on-close>
      <template #header>
        <div class="drawer-title"><small>POLICY DEFINITION / AUTHORIZATION GRAPH</small><h2>{{ policyDetail?.policy_type === 'federation' ? '联邦策略详情' : '普通策略详情' }}</h2></div>
      </template>
      <div v-loading="policyLoading" class="policy-detail">
        <template v-if="policyDetail">
          <div class="policy-id"><el-tag :type="policyDetail.policy_type === 'federation' ? 'warning' : 'primary'">{{ policyDetail.policy_type === 'federation' ? 'FEDERATION' : 'NORMAL' }}</el-tag><code>{{ policyDetail.policy_id }}</code></div>
          <section class="detail-grid">
            <article><small>OWNER</small><strong>{{ policyDetail.owner_name }}</strong><span>账号 {{ policyDetail.owner_id || '—' }}</span></article>
            <article><small>CONSUMER</small><strong>{{ policyDetail.consumer_name }}</strong><span>授权接收方</span></article>
            <article><small>STREAM</small><strong>{{ policyDetail.stream_name || '未知数据流' }}</strong><span>{{ policyDetail.stream_id }}</span></article>
            <article><small>GRANULARITY</small><strong>{{ policyDetail.granularity }}</strong><span>{{ policyDetail.policy_type === 'federation' ? '最小粒度' : '最小粒度倍数' }}</span></article>
          </section>
          <section class="time-boundary">
            <div><small>POLICY WINDOW</small><b>{{ fmt(policyDetail.p_starttime) }}</b><i></i><b>{{ fmt(policyDetail.p_endtime) }}</b></div>
            <div><small>EFFECTIVE WINDOW</small><b>{{ fmt(policyDetail.effective_starttime) }}</b><i></i><b>{{ fmt(policyDetail.effective_endtime) }}</b></div>
            <p>数据流自身范围：{{ fmt(policyDetail.stream_starttime) }} → {{ fmt(policyDetail.stream_endtime) }}</p>
          </section>
          <div class="detail-columns">
            <section class="rule-panel"><small>AUTHORIZATION RULES</small><h3>授权约束</h3><ol><li v-for="(rule, i) in policyDetail.authorization_rules" :key="rule"><i>{{ i + 1 }}</i><span>{{ rule }}</span></li></ol></section>
            <section class="rule-panel path"><small>CALCULATION PATH</small><h3>执行过程</h3><ol><li v-for="(step, i) in policyDetail.calculation_path" :key="step"><i>{{ i + 1 }}</i><span>{{ step }}</span></li></ol></section>
          </div>
          <section v-if="policyDetail.policy_type === 'federation'" class="federation-group">
            <header><div><small>FEDERATION SET</small><h3>同一消费者的联邦策略组</h3></div><el-tag :type="policyDetail.common_range_valid ? 'success' : 'danger'">{{ policyDetail.common_range_valid ? '存在公共授权区间' : '无有效公共区间' }}</el-tag></header>
            <div class="common-window">公共区间：{{ fmt(policyDetail.common_starttime) }} → {{ fmt(policyDetail.common_endtime) }}</div>
            <el-table :data="policyDetail.federation_group" max-height="280">
              <el-table-column prop="stream_name" label="数据流" min-width="130" /><el-table-column prop="owner_name" label="Owner" width="110" />
              <el-table-column label="授权时间" min-width="260"><template #default="{row}">{{ fmt(row.p_starttime) }}<small class="sub">→ {{ fmt(row.p_endtime) }}</small></template></el-table-column>
              <el-table-column prop="granularity" label="粒度" width="80" />
            </el-table>
          </section>
          <section class="producer-line"><span>生产节点</span><b>{{ policyDetail.producer_name || '未绑定' }}</b><code>{{ policyDetail.producer_id || 'N/A' }}</code><span>数据类型</span><b>{{ policyDetail.stream_description || '未描述' }}</b></section>
          <section class="fingerprint-inline">
            <div><small>CSV DATA FINGERPRINT</small><h3>数据与授权绑定</h3></div>
            <template v-if="policyDetail.csv_fingerprint">
              <el-tag type="success">已绑定上传批次</el-tag>
              <code>{{ policyDetail.csv_fingerprint.file_sha256 }}</code>
              <span>{{ policyDetail.csv_fingerprint.file_name }} · {{ policyDetail.csv_fingerprint.valid_rows }} 行</span>
            </template>
            <template v-else><el-tag type="info">历史策略</el-tag><span>创建该策略时尚无CSV指纹记录</span></template>
          </section>
          <section class="anchor-inline">
            <small>BLOCKCHAIN ANCHOR</small>
            <template v-if="policyDetail.blockchain_anchor"><el-tag :type="anchorTag(policyDetail.blockchain_anchor.status)">{{ anchorLabel(policyDetail.blockchain_anchor.status, policyDetail.blockchain_anchor.last_error) }}</el-tag><code>{{ policyDetail.blockchain_anchor.payload_sha256 || '摘要生成中' }}</code><el-button size="small" @click="openAnchor(policyDetail.blockchain_anchor)">查看存证</el-button></template>
            <span v-else>尚未建立存证任务</span>
          </section>
        </template>
      </div>
    </el-drawer>

    <section v-if="active === 'anchors'" class="console-card">
      <header><div><small>ETHEREUM PROVENANCE LEDGER</small><h2>区块链存证与摘要校验</h2></div><div class="filter"><el-select v-model="anchorStatus" clearable placeholder="全部状态"><el-option v-for="s in ['PENDING','PROCESSING','WAITING_CONFIG','CONFIRMED','FAILED']" :key="s" :label="anchorLabel(s)" :value="s" /></el-select><el-button @click="refreshAnchors">筛选</el-button></div></header>
      <section class="chain-visual" :class="{ confirmed: latestAnchor?.status === 'CONFIRMED' }">
        <div class="chain-scan"></div>
        <div class="chain-brief">
          <small>LIVE AUTHORIZATION ANCHOR</small>
          <strong>{{ latestAnchor ? businessLabel(latestAnchor.business_type) : '等待新的授权事件' }}</strong>
          <span v-if="latestAnchor">凭证 #{{ latestAnchor.id }} · {{ anchorLabel(latestAnchor.status, latestAnchor.last_error) }}</span>
          <span v-else>上传CSV并创建访问策略后，这里将显示真实交易过程</span>
        </div>
        <div class="credential-flow">
          <template v-for="(step, index) in credentialSteps" :key="step.code">
            <article :class="{ done: step.done, live: step.live }">
              <i>{{ step.done ? '✓' : step.code }}</i><div><b>{{ step.title }}</b><small>{{ step.note }}</small></div>
            </article>
            <span v-if="index < credentialSteps.length - 1" class="flow-link" :class="{ done: credentialSteps[index + 1].done }"><em></em></span>
          </template>
        </div>
        <div class="block-ribbon">
          <div class="genesis"><small>LOCAL CHAIN</small><b>31337</b></div>
          <div v-for="block in recentBlocks" :key="block.id" class="mini-block" @click="openAnchor(block)">
            <small>BLOCK</small><b>{{ block.block_number }}</b><code>{{ shortHash(block.transaction_hash) }}</code>
          </div>
          <div v-if="!recentBlocks.length" class="block-empty">等待首个确认区块</div>
        </div>
        <p class="chain-truth"><i></i> 动画仅依据真实存证字段推进：SHA-256、交易哈希、区块号和确认状态。</p>
      </section>
      <el-table :data="anchors" height="520">
        <el-table-column label="状态" width="105"><template #default="{row}"><el-tag :type="anchorTag(row.status)">{{ anchorLabel(row.status, row.last_error) }}</el-tag></template></el-table-column>
        <el-table-column label="业务对象" min-width="210"><template #default="{row}"><b>{{ businessLabel(row.business_type) }}</b><small class="sub">{{ row.business_type }} · {{ row.business_id }}</small></template></el-table-column>
        <el-table-column label="SHA-256" min-width="250"><template #default="{row}"><code>{{ row.payload_sha256 || '等待生成' }}</code></template></el-table-column>
        <el-table-column label="链上信息" min-width="210"><template #default="{row}"><code>{{ row.transaction_hash || '—' }}</code><small class="sub">{{ row.block_number ? `Block ${row.block_number}` : (row.last_error || '等待处理') }}</small></template></el-table-column>
        <el-table-column label="重试" width="90"><template #default="{row}">{{ row.retry_count }}/{{ row.max_retries }}</template></el-table-column>
        <el-table-column label="操作" width="180" fixed="right"><template #default="{row}"><el-button size="small" type="primary" @click="openAnchor(row)">详情</el-button><el-button v-if="row.status !== 'PROCESSING' && row.status !== 'CONFIRMED'" size="small" text @click="retryAnchor(row)">重试</el-button></template></el-table-column>
      </el-table>
    </section>

    <el-drawer v-model="anchorDrawer" size="min(720px, 92vw)" class="policy-drawer" destroy-on-close>
      <template #header><div class="drawer-title"><small>ANCHOR PAYLOAD / PUBLIC VERIFICATION</small><h2>区块链存证详情</h2></div></template>
      <div v-loading="anchorLoading" class="policy-detail" v-if="anchorDetail">
        <div class="policy-id"><el-tag :type="anchorTag(anchorDetail.status)">{{ anchorLabel(anchorDetail.status, anchorDetail.last_error) }}</el-tag><code>#{{ anchorDetail.id }} · {{ anchorDetail.trace_id }}</code></div>
        <section class="detail-grid"><article><small>BUSINESS</small><strong>{{ anchorDetail.business_type }}</strong><span>{{ anchorDetail.business_id }}</span></article><article><small>CHAIN</small><strong>{{ anchorDetail.chain_id || '未配置' }}</strong><span>Block {{ anchorDetail.block_number || '—' }}</span></article></section>
        <section class="hash-verify"><small>STORED SHA-256</small><code>{{ anchorDetail.payload_sha256 || '—' }}</code><small>RECOMPUTED SHA-256</small><code>{{ anchorDetail.recomputed_sha256 || '—' }}</code><el-tag :type="anchorDetail.verified ? 'success' : 'danger'">{{ anchorDetail.verified ? '数据库载荷校验一致' : '尚未生成或校验不一致' }}</el-tag><el-tag v-if="anchorDetail.status === 'CONFIRMED'" :type="anchorDetail.on_chain_verified ? 'success' : 'danger'">{{ anchorDetail.on_chain_verified ? '链上交易输入一致' : '链上校验失败' }}</el-tag></section>
        <section class="payload"><small>CANONICAL JSON</small><pre>{{ anchorDetail.payload_json || '等待生成规范化载荷' }}</pre></section>
        <section class="detail-grid"><article><small>TRANSACTION</small><code>{{ anchorDetail.transaction_hash || '—' }}</code></article><article><small>FROM</small><code>{{ anchorDetail.from_address || '—' }}</code></article></section>
        <p v-if="anchorDetail.last_error" class="anchor-error">{{ anchorDetail.last_error }}</p>
        <el-button v-if="anchorDetail.explorer_url" tag="a" :href="anchorDetail.explorer_url" target="_blank" type="primary">在区块浏览器查看</el-button>
        <el-button v-if="anchorDetail.status !== 'PROCESSING' && anchorDetail.status !== 'CONFIRMED'" @click="retryAnchor(anchorDetail)">重新上链</el-button>
      </div>
    </el-drawer>

    <section v-if="active === 'audit'" class="console-card">
      <header><div><small>IMMUTABLE EVENT LEDGER</small><h2>详细操作日志</h2></div><div class="filter"><el-select v-model="auditFilter" clearable placeholder="筛选动作"><el-option v-for="a in ['FEDERATION_QUERY_DENIED','FEDERATION_QUERY_AUTHORIZED','LOGIN','VIEW','CHANGE','DELETE','QUERY','STREAM_UPLOAD','STREAM_TRACE','USER_STATUS']" :key="a" :label="a" :value="a" /></el-select><el-button @click="refreshLogs">筛选</el-button></div></header>
      <el-table :data="logs" height="520">
        <el-table-column label="时间" width="180"><template #default="{row}">{{ fmt(row.eventTime) }}</template></el-table-column><el-table-column prop="action" label="动作" width="130" />
        <el-table-column label="操作者" width="160"><template #default="{row}">{{ row.actorName || '匿名' }}<small class="sub">{{ row.actorRole }}</small></template></el-table-column>
        <el-table-column prop="path" label="接口 / 资源" min-width="250" /><el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
        <el-table-column label="结果" width="90"><template #default="{row}"><span :class="row.success ? 'status-on' : 'status-off'">{{ row.success ? '成功' : '失败' }}</span></template></el-table-column>
        <el-table-column label="追踪号" min-width="180"><template #default="{row}"><code>{{ row.traceId }}</code></template></el-table-column>
      </el-table>
    </section>

    <section v-if="active === 'trace'" class="trace-zone">
      <div v-if="!trace" class="empty-trace"><el-icon><Aim /></el-icon><h3>请选择一条数据流</h3><p>从“数据流与溯源”进入，系统会重新分析异常点和聚合过程。</p></div>
      <template v-else>
        <div class="trace-banner"><div><small>TRACE TARGET</small><h2>{{ trace.stream.name }}</h2><p>{{ trace.stream.id }} · {{ trace.provenance.ownerName }} · {{ trace.provenance.producerName || '未绑定生产者' }}</p></div><el-button @click="openTrace(selectedStream)"><el-icon><Refresh /></el-icon>重新计算</el-button></div>
        <div class="evidence-grid">
          <article><small>DATA SOURCE</small><strong>{{ trace.provenance.source }}</strong><span>{{ trace.provenance.pointCount }} points</span></article>
          <article><small>ANOMALIES</small><strong class="alert-number">{{ trace.anomalies.length }}</strong><span>Z-score + change rate</span></article>
          <article><small>TIME SLICE</small><strong>{{ trace.stream.minGranularity }} ms</strong><span>{{ trace.blocks.length }} aggregation blocks</span></article>
        </div>
        <div class="trace-grid">
          <section class="console-card anomaly"><header><div><small>ANOMALY TIMELINE</small><h2>异常流量时间点</h2></div></header><el-table :data="trace.anomalies" height="340"><el-table-column label="发生时间" min-width="170"><template #default="{row}">{{ fmt(row.time) }}</template></el-table-column><el-table-column prop="value" label="流量" width="90" /><el-table-column label="偏差" width="110"><template #default="{row}">z={{ row.zScore }}</template></el-table-column><el-table-column label="突变" width="100"><template #default="{row}">{{ row.changeRate }}%</template></el-table-column><el-table-column prop="reason" label="判定原因" min-width="160" /></el-table></section>
          <section class="console-card process"><header><div><small>CRYPTO AGGREGATION PIPELINE</small><h2>密文聚合计算过程</h2></div></header><ol><li v-for="step in trace.process" :key="step.order"><i>{{ step.order }}</i><div><b>{{ step.name }}</b><p>{{ step.detail }}</p><span :class="step.evidence">{{ step.evidence === 'observed' ? '真实观测' : '算法重建' }}</span></div></li></ol></section>
        </div>
        <section class="console-card blocks"><header><div><small>CIPHER BLOCK INSPECTION</small><h2>数据块与密文符号</h2></div><el-tag type="warning">不展示真实密钥</el-tag></header><el-table :data="trace.blocks" height="330"><el-table-column prop="blockIndex" label="块" width="70" /><el-table-column label="时间片" min-width="240"><template #default="{row}">{{ fmt(row.startTime) }} → {{ fmt(row.endTime) }}</template></el-table-column><el-table-column prop="count" label="n" width="70" /><el-table-column label="Σx" width="110"><template #default="{row}">{{ num(row.sum) }}</template></el-table-column><el-table-column label="Σx²" width="130"><template #default="{row}">{{ num(row.squareSum) }}</template></el-table-column><el-table-column prop="mean" label="均值" width="100" /><el-table-column prop="cipherSymbol" label="密文聚合表达" min-width="230"><template #default="{row}"><code>{{ row.cipherSymbol }}</code></template></el-table-column></el-table><p class="disclosure">{{ trace.disclosure }}</p></section>
      </template>
    </section>
  </div>
</template>

<style scoped>
.admin-shell{max-width:1680px;margin:auto}.command-head{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:22px}.kicker{font:10px 'Chakra Petch';letter-spacing:.18em;color:var(--brand)}.kicker i{display:inline-block;width:28px;height:1px;background:var(--brand);vertical-align:middle;margin-right:9px}.command-head h1{margin:10px 0 6px;font:600 30px 'Chakra Petch','Noto Sans SC'}.command-head p{margin:0;color:var(--text-sub)}.integrity{display:flex;gap:12px;align-items:center;padding:12px 16px;border:1px solid rgba(20,241,217,.2);background:rgba(20,241,217,.04)}.integrity>span{width:9px;height:9px;border-radius:50%;background:var(--brand);box-shadow:0 0 14px var(--brand)}.integrity b,.integrity small{display:block;font:10px 'Chakra Petch';letter-spacing:.1em}.integrity small{color:var(--text-sub);margin-top:4px}.metrics{display:grid;grid-template-columns:repeat(5,1fr);gap:12px;margin-bottom:16px}.metrics article,.evidence-grid article{position:relative;padding:17px 19px;background:linear-gradient(145deg,rgba(16,38,49,.95),rgba(8,23,31,.95));border:1px solid var(--line);overflow:hidden}.metrics article:after{content:'';position:absolute;right:-16px;bottom:-28px;width:72px;height:72px;border:1px solid rgba(20,241,217,.08);transform:rotate(45deg)}.metrics small,.evidence-grid small,.console-card header small,.trace-banner small{display:block;color:#568489;font:9px 'Chakra Petch';letter-spacing:.16em}.metrics strong{display:block;margin:6px 0 2px;font:600 27px 'Chakra Petch';color:var(--brand)}.metrics span,.evidence-grid span{font-size:12px;color:var(--text-sub)}.metrics .danger strong,.alert-number{color:var(--danger)!important}.rail{display:flex;align-items:center;gap:5px;padding:5px;margin-bottom:16px;border:1px solid var(--line);background:rgba(5,15,21,.7)}.rail button{border:0;background:transparent;color:#668c90;padding:11px 16px;cursor:pointer}.rail button.active{color:#051513;background:var(--brand);font-weight:700}.rail .el-button{margin-left:auto}.console-card{background:linear-gradient(145deg,rgba(16,38,49,.94),rgba(8,23,31,.94));border:1px solid var(--line);padding:20px;box-shadow:0 20px 50px rgba(0,0,0,.2)}.console-card header{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px}.console-card h2,.trace-banner h2{font:600 18px 'Chakra Petch','Noto Sans SC';margin:5px 0 0}.console-card header>span{color:var(--brand);font:10px 'Chakra Petch';letter-spacing:.13em}.sub{display:block;color:var(--text-sub);margin-top:4px}.status-on{color:var(--brand)}.status-off{color:var(--danger)}.root-lock{font-size:12px;color:#667f83}code{color:#ffca72;font:11px 'Chakra Petch';word-break:break-all}.filter{display:flex;gap:8px}.filter .el-select{width:150px}.empty-trace{height:460px;display:flex;flex-direction:column;align-items:center;justify-content:center;border:1px dashed var(--line-strong);color:var(--text-sub)}.empty-trace .el-icon{font-size:42px;color:var(--brand)}.empty-trace h3{color:var(--text-main);margin:16px 0 4px}.trace-banner{display:flex;align-items:center;justify-content:space-between;padding:18px 22px;margin-bottom:12px;border-left:3px solid var(--brand);background:linear-gradient(90deg,rgba(20,241,217,.08),rgba(20,241,217,.01))}.trace-banner p{margin:4px 0 0;color:var(--text-sub)}.evidence-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:12px}.evidence-grid strong{display:block;margin:7px 0 3px;font:600 21px 'Chakra Petch';color:var(--brand)}.trace-grid{display:grid;grid-template-columns:1.2fr 1fr;gap:12px;margin-bottom:12px}.process ol{list-style:none;padding:0;margin:0;max-height:340px;overflow:auto}.process li{display:flex;gap:13px;padding:10px 0;border-bottom:1px solid var(--line)}.process li>i{display:flex;align-items:center;justify-content:center;flex:0 0 28px;height:28px;border:1px solid var(--brand);color:var(--brand);font:normal 12px 'Chakra Petch'}.process b{font-size:13px}.process p{margin:4px 0;color:var(--text-sub);font-size:12px;line-height:1.6}.process span{font-size:9px;padding:2px 6px;border:1px solid}.process .observed{color:var(--brand)}.process .derived{color:var(--signal)}.disclosure{margin:12px 0 0;color:#6f8f92;font-size:11px}.blocks{margin-bottom:30px}@media(max-width:1100px){.metrics{grid-template-columns:repeat(2,1fr)}.trace-grid{grid-template-columns:1fr}.command-head{align-items:flex-start;gap:16px}.integrity{display:none}}@media(max-width:760px){.rail{overflow:auto}.metrics,.evidence-grid{grid-template-columns:1fr}.command-head h1{font-size:24px}}
.admin-shell :deep(.el-table),.admin-shell :deep(.el-table__inner-wrapper),.admin-shell :deep(.el-table__body-wrapper),.admin-shell :deep(.el-scrollbar__view){background:transparent!important}.admin-shell :deep(.el-table tr),.admin-shell :deep(.el-table td.el-table__cell),.admin-shell :deep(.el-table th.el-table__cell){background:#0d2029!important;color:var(--text-main)}.admin-shell :deep(.el-table th.el-table__cell){background:#102a34!important;color:#7be4da}.admin-shell :deep(.el-table__row:hover td.el-table__cell){background:#12323c!important}.admin-shell :deep(.el-table__empty-block){background:#0d2029}.admin-shell :deep(.el-table__empty-text){color:var(--text-sub)}
.drawer-title small,.policy-detail small{color:#5d8c90;font:9px 'Chakra Petch';letter-spacing:.16em}.drawer-title h2{margin:6px 0 0;font:600 20px 'Chakra Petch','Noto Sans SC'}.policy-id{display:flex;align-items:center;gap:14px;padding-bottom:16px;border-bottom:1px solid var(--line)}.policy-id code{font-size:13px}.detail-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:10px;margin:14px 0}.detail-grid article{padding:15px;border:1px solid var(--line);background:rgba(20,241,217,.035)}.detail-grid strong,.detail-grid span{display:block}.detail-grid strong{margin:6px 0 3px}.detail-grid span{color:var(--text-sub);font-size:11px}.time-boundary{padding:16px;border-left:2px solid var(--brand);background:#091920}.time-boundary>div{display:grid;grid-template-columns:110px 1fr 34px 1fr;align-items:center;margin-bottom:10px}.time-boundary b{font-size:12px}.time-boundary i{height:1px;margin:0 9px;background:var(--brand)}.time-boundary p{margin:4px 0 0;color:var(--text-sub);font-size:11px}.detail-columns{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin:14px 0}.rule-panel{padding:16px;border:1px solid var(--line)}.rule-panel h3,.federation-group h3{margin:5px 0 12px;font-size:15px}.rule-panel ol{list-style:none;margin:0;padding:0}.rule-panel li{display:flex;gap:9px;align-items:flex-start;padding:7px 0;border-bottom:1px solid rgba(118,228,221,.08);font-size:12px;color:var(--text-sub)}.rule-panel li i{display:grid;place-items:center;flex:0 0 21px;height:21px;border:1px solid var(--brand);color:var(--brand);font:normal 10px 'Chakra Petch'}.rule-panel.path li i{border-color:var(--signal);color:var(--signal)}.federation-group{margin:14px 0;padding:16px;border:1px solid rgba(255,176,32,.26);background:rgba(255,176,32,.025)}.federation-group header{display:flex;justify-content:space-between;align-items:center}.common-window{margin-bottom:12px;padding:9px 12px;background:rgba(255,176,32,.08);color:#ffd38a;font-size:12px}.producer-line{display:grid;grid-template-columns:70px 1fr 100px 70px 1fr;gap:8px;align-items:center;padding:13px;border:1px solid var(--line);font-size:12px}.producer-line span{color:var(--text-sub)}.producer-line code{text-align:right}@media(max-width:700px){.detail-grid,.detail-columns{grid-template-columns:1fr}.time-boundary>div{grid-template-columns:1fr}.time-boundary i{display:none}.producer-line{grid-template-columns:1fr 1fr}}
:global(.policy-drawer.el-drawer){background:#0b1c24!important;border-left:1px solid var(--line-strong);color:var(--text-main)}:global(.policy-drawer .el-drawer__header){border-bottom:1px solid var(--line);margin-bottom:0;padding-bottom:18px}:global(.policy-drawer .el-drawer__body){padding-top:18px}:global(.policy-drawer .el-table),:global(.policy-drawer .el-table tr),:global(.policy-drawer .el-table td.el-table__cell),:global(.policy-drawer .el-table th.el-table__cell){background:#0d2029!important;color:var(--text-main)}
.anchor-inline{display:flex;align-items:center;gap:12px;margin-top:14px;padding:15px;border:1px solid rgba(20,241,217,.22);background:rgba(20,241,217,.035)}.anchor-inline small{margin-right:auto}.anchor-inline code{max-width:360px}.hash-verify,.payload{display:grid;gap:8px;margin:14px 0;padding:16px;border:1px solid var(--line);background:#091920}.hash-verify .el-tag{justify-self:start;margin-top:5px}.payload pre{max-height:260px;overflow:auto;white-space:pre-wrap;word-break:break-all;color:#c4e4e1;font:12px/1.7 'Chakra Petch',monospace}.anchor-error{padding:12px;border-left:2px solid var(--danger);background:rgba(255,82,108,.08);color:#ff9bad;font-size:12px}
.fingerprint-inline{display:grid;grid-template-columns:auto auto 1fr;align-items:center;gap:8px 14px;margin-top:14px;padding:15px;border:1px solid rgba(255,176,32,.2);background:linear-gradient(90deg,rgba(255,176,32,.055),transparent)}.fingerprint-inline>div{grid-row:span 2;min-width:150px}.fingerprint-inline small{display:block;color:#8c7450;font:9px 'Chakra Petch';letter-spacing:.14em}.fingerprint-inline h3{margin:4px 0 0;font-size:14px}.fingerprint-inline code{min-width:0}.fingerprint-inline span{color:var(--text-sub);font-size:11px}
.chain-visual{position:relative;overflow:hidden;margin:0 0 18px;padding:22px;border:1px solid rgba(20,241,217,.2);background:radial-gradient(circle at 15% 0%,rgba(20,241,217,.13),transparent 28%),linear-gradient(135deg,#071b24,#050f15 62%,#09171e)}
.chain-visual:before{content:'';position:absolute;inset:0;background-image:linear-gradient(rgba(20,241,217,.035) 1px,transparent 1px),linear-gradient(90deg,rgba(20,241,217,.035) 1px,transparent 1px);background-size:26px 26px;mask-image:linear-gradient(to bottom,#000,transparent)}
.chain-scan{position:absolute;z-index:0;top:0;bottom:0;width:140px;background:linear-gradient(90deg,transparent,rgba(20,241,217,.075),transparent);animation:chainScan 5.5s linear infinite;transform:skewX(-18deg)}
.chain-brief,.credential-flow,.block-ribbon,.chain-truth{position:relative;z-index:1}.chain-brief{display:flex;align-items:baseline;gap:13px;margin-bottom:20px}.chain-brief small{color:var(--brand);font:9px 'Chakra Petch';letter-spacing:.18em}.chain-brief strong{font:600 17px 'Chakra Petch','Noto Sans SC'}.chain-brief span{margin-left:auto;color:#668e92;font-size:11px}
.credential-flow{display:flex;align-items:center}.credential-flow article{display:flex;align-items:center;gap:9px;min-width:128px;opacity:.42;transition:.45s ease}.credential-flow article i{display:grid;place-items:center;width:31px;height:31px;border:1px solid #38565c;border-radius:50%;color:#6f8c91;font:normal 10px 'Chakra Petch'}.credential-flow article b,.credential-flow article small{display:block}.credential-flow article b{font-size:12px}.credential-flow article small{max-width:120px;margin-top:3px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#66868a;font:9px 'Chakra Petch'}.credential-flow article.done{opacity:1}.credential-flow article.done i{border-color:var(--brand);background:rgba(20,241,217,.13);color:var(--brand);box-shadow:0 0 18px rgba(20,241,217,.28)}.credential-flow article.live i{animation:nodePulse 1.2s ease-in-out infinite}.flow-link{position:relative;flex:1;height:1px;margin:0 9px;background:#244148;overflow:hidden}.flow-link.done{background:rgba(20,241,217,.32)}.flow-link.done em{position:absolute;top:-2px;width:22px;height:5px;background:linear-gradient(90deg,transparent,var(--brand),transparent);animation:packet 1.8s linear infinite}
.block-ribbon{display:flex;align-items:stretch;gap:8px;margin-top:21px;padding-top:17px;border-top:1px solid rgba(20,241,217,.1)}.genesis,.mini-block,.block-empty{min-height:66px;padding:10px 12px;border:1px solid #24444a;background:rgba(5,19,25,.82)}.genesis{border-color:rgba(255,176,32,.35)}.genesis small,.mini-block small{display:block;color:#587c80;font:8px 'Chakra Petch';letter-spacing:.14em}.genesis b,.mini-block b{display:block;margin-top:5px;color:var(--signal);font:600 15px 'Chakra Petch'}.mini-block{position:relative;min-width:122px;cursor:pointer;transition:.25s}.mini-block:before{content:'';position:absolute;left:-9px;top:31px;width:8px;height:1px;background:var(--brand)}.mini-block:hover{border-color:var(--brand);transform:translateY(-2px)}.mini-block b{color:var(--brand)}.mini-block code{display:block;margin-top:3px;font-size:8px}.block-empty{display:grid;place-items:center;flex:1;border-style:dashed;color:#53757a;font-size:11px}.chain-truth{margin:13px 0 0;color:#597b7f;font-size:10px}.chain-truth i{display:inline-block;width:6px;height:6px;margin-right:7px;border-radius:50%;background:var(--brand);box-shadow:0 0 9px var(--brand)}
@keyframes chainScan{from{left:-18%}to{left:110%}}@keyframes packet{from{left:-25px}to{left:100%}}@keyframes nodePulse{50%{box-shadow:0 0 28px rgba(255,176,32,.7);border-color:var(--signal);color:var(--signal)}}
@media(max-width:1100px){.credential-flow{display:grid;grid-template-columns:repeat(5,1fr);gap:7px}.credential-flow article{min-width:0}.flow-link{display:none}.block-ribbon{overflow:auto}.chain-brief{align-items:flex-start;flex-direction:column}.chain-brief span{margin-left:0}}
@media(max-width:760px){.credential-flow{grid-template-columns:1fr}.fingerprint-inline{grid-template-columns:1fr}.chain-visual{padding:16px}.credential-flow article small{max-width:none}}
</style>
