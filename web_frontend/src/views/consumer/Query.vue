<script setup>
import { reactive, ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { api } from '../../api'
import { userStore } from '../../store/user'
import { fmtTime } from '../../utils/time'

const policies = ref([])
const loadingQuery = ref(false)
const queried = ref(false)

const form = reactive({
  ownerName: '',
  policyId: '',
  startTime: null,
  endTime: null,
  multiple: 1,
})

const result = reactive({
  lineChart: null,
  barChart: null,
  statistics: null,
})

const ownerOptions = computed(() => {
  const set = new Set()
  policies.value.forEach((p) => set.add(p.ownerName))
  return [...set]
})

const streamOptions = computed(() =>
  policies.value.filter((p) => p.ownerName === form.ownerName)
)

const currentPolicy = computed(() =>
  policies.value.find((p) => p.policyId === form.policyId)
)
const queryReadiness = computed(() => [
  { label: '获得授权', done: policies.value.length > 0, hint: `${policies.value.length} 条策略` },
  { label: '选择数据流', done: Boolean(currentPolicy.value), hint: currentPolicy.value?.streamName || '未选择' },
  { label: '时间在授权内', done: timeInsidePolicy(), hint: currentPolicy.value ? `${fmtTime(currentPolicy.value.startTime)} ~ ${fmtTime(currentPolicy.value.endTime)}` : '等待选择' },
  { label: '粒度满足策略', done: Number(form.multiple) >= minAllowedMultiple.value, hint: `≥ ${minAllowedMultiple.value}` },
])
const minAllowedMultiple = computed(() => {
  const value = Number(currentPolicy.value?.minGranularity || 1)
  return Number.isFinite(value) && value > 0 ? value : 1
})

function timeInsidePolicy() {
  const p = currentPolicy.value
  if (!p || !form.startTime || !form.endTime) return false
  const startMs = form.startTime.getTime()
  const endMs = form.endTime.getTime()
  return startMs >= Number(p.startTime) && endMs <= Number(p.endTime) && startMs < endMs
}

async function loadPolicies() {
  try {
    const data = await api.listPolicies(userStore.state.usrName)
    policies.value = Array.isArray(data) ? data : []
  } catch (e) {
  }
}

function onOwnerChange() {
  form.policyId = ''
  form.startTime = null
  form.endTime = null
  form.multiple = 1
}

function onStreamChange() {
  const p = currentPolicy.value
  if (p) {
    form.startTime = new Date(Number(p.startTime))
    form.endTime = new Date(Number(p.endTime))
    form.multiple = minAllowedMultiple.value
  }
}

const statCards = computed(() => {
  const s = result.statistics
  if (!s) return []
  return [
    { key: 'mean', label: '平均流量参数', value: Number(s.mean).toFixed(2), icon: 'TrendCharts', color: '#14f1d9' },
    { key: 'std', label: '波动强度', value: Number(s.std).toFixed(2), icon: 'Histogram', color: '#4cb8ff' },
    { key: 'variance', label: '离散系数', value: Number(s.variance).toFixed(2), icon: 'DataLine', color: '#ffb020' },
    { key: 'sum', label: '累计观测值', value: formatInt(s.sum), icon: 'Sort', color: '#55e38e' },
    { key: 'count', label: '车辆样本量', value: formatInt(s.count), icon: 'Coin', color: '#9d7bff' },
    { key: 'squareSum', label: '能量特征值', value: formatInt(s.squareSum), icon: 'Grid', color: '#ff6f91' },
  ]
})

function formatInt(n) {
  if (n === null || n === undefined) return '-'
  return Number(n).toLocaleString('en-US')
}

const lineRef = ref(null)
const barRef = ref(null)
let lineChart = null
let barChart = null

function fmtClock(ms) {
  const d = new Date(Number(ms))
  const p = (n) => String(n).padStart(2, '0')
  return `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

function initCharts() {
  if (lineRef.value && !lineChart) lineChart = echarts.init(lineRef.value)
  if (barRef.value && !barChart) barChart = echarts.init(barRef.value)
}

function renderLine() {
  const d = result.lineChart
  if (!lineChart || !d) return
  const xData = d.time.map(fmtClock)
  lineChart.setOption({
    backgroundColor: 'transparent',
    title: { text: 'TRAFFIC FLOW / 时序波形', left: 16, top: 12, textStyle: { fontSize: 13, fontWeight: 600, color: '#bfe9e5', fontFamily: 'Chakra Petch' } },
    tooltip: { trigger: 'axis', backgroundColor: '#07171e', borderColor: '#14f1d9', textStyle: { color: '#dffaf7' } },
    grid: { left: 50, right: 24, top: 56, bottom: 48 },
    xAxis: {
      type: 'category',
      data: xData,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#294a50' } },
      axisLabel: { color: '#668e92', hideOverlap: true },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#668e92' },
      splitLine: { lineStyle: { color: 'rgba(118,228,221,.08)' } },
    },
    series: [
      {
        name: '平均值',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: d.avg,
        lineStyle: { width: 2.5, color: '#14f1d9', shadowBlur: 12, shadowColor: 'rgba(20,241,217,.45)' },
        itemStyle: { color: '#14f1d9' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(20,241,217,0.28)' },
            { offset: 1, color: 'rgba(20,241,217,0.01)' },
          ]),
        },
        markLine: {
          symbol: 'none',
          lineStyle: { type: 'dashed', color: '#ffb020', width: 1.5 },
          label: { formatter: '路网基准 {c}', color: '#ffb020', position: 'insideEndTop' },
          data: [{ yAxis: Number(d.globalAvg) }],
        },
      },
    ],
  })
}

function renderBar() {
  const d = result.barChart
  if (!barChart || !d) return
  barChart.setOption({
    title: { text: 'VEHICLE DENSITY / 区间分布', left: 16, top: 12, textStyle: { fontSize: 13, fontWeight: 600, color: '#bfe9e5', fontFamily: 'Chakra Petch' } },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#07171e', borderColor: '#14f1d9', textStyle: { color: '#dffaf7' } },
    grid: { left: 50, right: 24, top: 56, bottom: 48 },
    xAxis: {
      type: 'category',
      data: d.categories,
      axisLine: { lineStyle: { color: '#294a50' } },
      axisLabel: { color: '#668e92' },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#668e92' },
      splitLine: { lineStyle: { color: 'rgba(118,228,221,.08)' } },
    },
    series: [
      {
        name: '计数',
        type: 'bar',
        data: d.counts,
        barWidth: '52%',
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ffb020' },
            { offset: 1, color: '#14f1d9' },
          ]),
        },
        label: { show: true, position: 'top', color: '#83a8aa', fontSize: 11 },
      },
    ],
  })
}

function handleResize() {
  lineChart && lineChart.resize()
  barChart && barChart.resize()
}

async function handleQuery() {
  const p = currentPolicy.value
  if (!p) {
    ElMessage.warning('请选择拥有者与数据流')
    return
  }
  if (!form.startTime || !form.endTime) {
    ElMessage.warning('请选择查询时间范围')
    return
  }

  loadingQuery.value = true
  const startMs = form.startTime.getTime()
  const endMs = form.endTime.getTime()
  if (startMs >= endMs) {
    ElMessage.warning('起始时间必须早于结束时间')
    loadingQuery.value = false
    return
  }
  if (startMs < Number(p.startTime) || endMs > Number(p.endTime)) {
    ElMessage.warning(`查询时间需在策略允许范围内：${fmtTime(p.startTime)} ~ ${fmtTime(p.endTime)}`)
    loadingQuery.value = false
    return
  }
  if (Number(form.multiple) < minAllowedMultiple.value) {
    ElMessage.warning(`查询粒度不能小于策略授权下限：${minAllowedMultiple.value}`)
    form.multiple = minAllowedMultiple.value
    loadingQuery.value = false
    return
  }
  try {
    const data = await api.query({
      ownerName: form.ownerName,
      policyId: p.policyId,
      streamId: p.streamId,
      startTime: startMs,
      endTime: endMs,
      multiple: form.multiple,
    })
    result.lineChart = data.lineChart
    result.barChart = data.barChart
    result.statistics = data.statistics
    queried.value = true
    await nextTick()
    initCharts()
    renderLine()
    renderBar()
  } catch (e) {
  } finally {
    loadingQuery.value = false
  }
}

onMounted(() => {
  loadPolicies()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart && lineChart.dispose()
  barChart && barChart.dispose()
  lineChart = null
  barChart = null
})
</script>

<template>
  <div class="query-view">
    <section class="simulation-banner">
      <div>
        <span>URBAN TRAFFIC SIMULATION ENGINE</span>
        <h2>汽车流量仿真舱</h2>
        <p>在授权时间窗内读取交通数据流，生成路网趋势、密度分布与统计特征。</p>
      </div>
      <div class="radar">
        <i></i><i></i><i></i><b></b>
      </div>
      <div class="sim-state"><small>SIMULATION CORE</small><strong>READY</strong></div>
    </section>
    <div class="page-card">
      <div class="page-toolbar">
        <h2 class="page-title">仿真参数配置</h2>
      </div>

      <el-form :model="form" class="query-form" @submit.prevent>
        <div class="query-readiness">
          <div
            v-for="item in queryReadiness"
            :key="item.label"
            class="ready-node"
            :class="{ online: item.done }"
          >
            <i></i>
            <span>{{ item.label }}</span>
            <small>{{ item.hint }}</small>
          </div>
        </div>

        <div class="form-row">
          <el-form-item label="交通数据节点" class="fi">
            <el-select
              v-model="form.ownerName"
              placeholder="请选择拥有者"
              filterable
              clearable
              @change="onOwnerChange"
            >
              <el-option v-for="o in ownerOptions" :key="o" :label="o" :value="o" />
            </el-select>
          </el-form-item>

          <el-form-item label="道路流量数据" class="fi">
            <el-select
              v-model="form.policyId"
              placeholder="请选择数据流"
              filterable
              clearable
              :disabled="!form.ownerName"
              @change="onStreamChange"
            >
              <el-option
                v-for="s in streamOptions"
                :key="s.policyId"
                :label="s.streamName"
                :value="s.policyId"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="起始时间" class="fi">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              placeholder="选择起始时间"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="结束时间" class="fi">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="粒度倍数" class="fi fi-narrow">
            <el-input-number
              v-model="form.multiple"
              :min="minAllowedMultiple"
              :step="1"
              controls-position="right"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label=" " class="fi fi-narrow">
            <el-button type="primary" :loading="loadingQuery" style="width: 100%" @click="handleQuery">
              <el-icon style="margin-right: 4px"><Search /></el-icon>启动仿真
            </el-button>
          </el-form-item>
        </div>

        <el-alert
          v-if="currentPolicy"
          type="info"
          :closable="false"
          show-icon
          class="range-alert"
          :title="`隐私授权已装载：只返回聚合统计与趋势图；查询时间需在 ${fmtTime(currentPolicy.startTime)} ~ ${fmtTime(currentPolicy.endTime)} 内，最小粒度倍数 ${minAllowedMultiple}。`"
        />
      </el-form>
    </div>

    <div class="page-card result-card" v-loading="loadingQuery" element-loading-text="正在构建交通仿真结果...">
      <template v-if="queried">
        <div class="stat-grid">
          <div v-for="c in statCards" :key="c.key" class="stat-card">
            <div class="stat-icon" :style="{ background: c.color + '1a', color: c.color }">
              <el-icon :size="22"><component :is="c.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ c.label }}</div>
              <div class="stat-value">{{ c.value }}</div>
            </div>
          </div>
        </div>

        <div class="chart-grid">
          <div class="chart-box">
            <div ref="lineRef" class="chart"></div>
          </div>
          <div class="chart-box">
            <div ref="barRef" class="chart"></div>
          </div>
        </div>
      </template>

      <el-empty v-else description="选择道路流量数据并启动仿真" :image-size="120" />
    </div>
  </div>
</template>

<style scoped>
.query-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.simulation-banner {
  position: relative;
  overflow: hidden;
  min-height: 150px;
  padding: 26px 32px;
  border: 1px solid var(--line);
  border-radius: 3px 22px 3px 22px;
  background: linear-gradient(110deg, rgba(14, 43, 52, .98), rgba(5, 20, 27, .94));
}
.simulation-banner span {
  color: var(--brand);
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: .2em;
}
.simulation-banner h2 {
  margin: 8px 0 4px;
  font: 600 29px 'Chakra Petch', 'Noto Sans SC', sans-serif;
}
.simulation-banner p { margin: 0; color: var(--text-sub); }
.radar {
  position: absolute;
  right: 180px;
  top: -42px;
  width: 220px; height: 220px;
  border: 1px solid rgba(20,241,217,.12);
  border-radius: 50%;
}
.radar i { position:absolute; inset:30px; border:1px solid rgba(20,241,217,.1); border-radius:50%; }
.radar i:nth-child(2) { inset:65px; }
.radar i:nth-child(3) { left:50%; top:0; bottom:0; width:1px; border:0; border-left:1px solid rgba(20,241,217,.12); border-radius:0; }
.radar b {
  position:absolute; left:50%; top:50%; width:46%; height:1px;
  transform-origin:left; background:linear-gradient(90deg,var(--brand),transparent);
  animation:radar 4s linear infinite;
}
.sim-state {
  position:absolute; right:30px; top:50%; transform:translateY(-50%);
  padding:14px 18px; border-left:1px solid var(--brand);
}
.sim-state small { display:block; color:var(--text-sub); font:9px 'Chakra Petch',sans-serif; }
.sim-state strong { color:var(--brand); font:700 22px 'Chakra Petch',sans-serif; text-shadow:0 0 16px rgba(20,241,217,.5); }
.query-form .form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0 18px;
  align-items: flex-end;
}
.query-readiness {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin-bottom: 18px;
}
.ready-node {
  position: relative;
  min-height: 76px;
  padding: 14px 14px 12px 42px;
  border: 1px solid rgba(118, 228, 221, .13);
  border-radius: 2px 12px 2px 12px;
  background:
    linear-gradient(135deg, rgba(20, 241, 217, .045), rgba(20, 241, 217, .01)),
    rgba(3, 18, 24, .58);
}
.ready-node::after {
  content: '';
  position: absolute;
  left: 21px;
  top: 30px;
  bottom: -12px;
  width: 1px;
  background: rgba(20, 241, 217, .16);
}
.ready-node:last-child::after {
  display: none;
}
.ready-node i {
  position: absolute;
  left: 15px;
  top: 18px;
  width: 13px;
  height: 13px;
  border: 1px solid rgba(118, 228, 221, .3);
  border-radius: 50%;
  background: rgba(118, 228, 221, .1);
}
.ready-node.online i {
  border-color: var(--brand);
  background: var(--brand);
  box-shadow: 0 0 14px rgba(20, 241, 217, .7);
}
.ready-node span {
  display: block;
  color: var(--text-main);
  font-weight: 600;
}
.ready-node small {
  display: block;
  margin-top: 6px;
  color: var(--text-sub);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.query-form .fi {
  flex: 1 1 200px;
  margin-bottom: 8px;
}
.query-form .fi-narrow {
  flex: 0 0 140px;
}
.range-alert {
  margin-top: 6px;
}
.result-card {
  min-height: 300px;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-bottom: 22px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 2px 12px 2px 12px;
  background: rgba(3, 18, 24, .62);
  border: 1px solid var(--line);
}
.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-label {
  font-size: 13px;
  color: var(--text-sub);
  margin-bottom: 4px;
}
.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-main);
  line-height: 1.1;
  word-break: break-all;
}
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}
.chart-box {
  background: rgba(3, 18, 24, .62);
  border: 1px solid var(--line);
  border-radius: 2px 14px 2px 14px;
}
@keyframes radar { to { transform: rotate(360deg); } }
.chart {
  width: 100%;
  height: 360px;
}
@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .query-readiness {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 680px) {
  .query-readiness {
    grid-template-columns: 1fr;
  }
}
</style>
