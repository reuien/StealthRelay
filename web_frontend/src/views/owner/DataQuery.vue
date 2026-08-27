<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { api } from '../../api'
import { userStore } from '../../store/user'
import { fmtTime } from '../../utils/time'

const streams = ref([])
const loading = ref(false)
const queried = ref(false)
const form = reactive({
  streamId: '',
  startTime: null,
  endTime: null,
  blockMillis: 600000,
})
const result = reactive({
  blocks: [],
  statistics: null,
  lineChart: null,
  barChart: null,
})

const currentStream = computed(() => streams.value.find((item) => item.id === form.streamId))
const cards = computed(() => {
  const s = result.statistics
  if (!s) return []
  return [
    { label: '样本量', value: formatInt(s.count), icon: 'Coin' },
    { label: '平均车流', value: Number(s.mean || 0).toFixed(2), icon: 'TrendCharts' },
    { label: '波动强度', value: Number(s.std || 0).toFixed(2), icon: 'Histogram' },
    { label: '累计流量', value: formatInt(s.sum), icon: 'Sort' },
  ]
})
const lineRef = ref(null)
const barRef = ref(null)
let lineChart = null
let barChart = null

async function loadStreams() {
  streams.value = await api.listStreams(userStore.state.number)
}

function onStreamChange() {
  const stream = currentStream.value
  if (!stream) return
  form.startTime = new Date(Number(stream.startTime))
  form.endTime = new Date(Number(stream.endTime))
  form.blockMillis = Number(stream.granularity || stream.minGranularity || 600000)
}

async function handleQuery() {
  if (!currentStream.value || !form.startTime || !form.endTime) {
    ElMessage.warning('请选择交通流和查询时间')
    return
  }
  if (form.startTime.getTime() >= form.endTime.getTime()) {
    ElMessage.warning('开始时间必须早于结束时间')
    return
  }
  loading.value = true
  try {
    const data = await api.ownerQuery({
      streamId: form.streamId,
      startTime: form.startTime.getTime(),
      endTime: form.endTime.getTime(),
      blockMillis: form.blockMillis,
    })
    result.blocks = data.blocks || []
    result.statistics = data.statistics || null
    result.lineChart = data.lineChart || null
    result.barChart = data.barChart || null
    queried.value = true
    await nextTick()
    initCharts()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function formatInt(value) {
  return Number(value || 0).toLocaleString('en-US')
}

function fmtClock(ms) {
  const date = new Date(Number(ms))
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function initCharts() {
  if (lineRef.value && !lineChart) lineChart = echarts.init(lineRef.value)
  if (barRef.value && !barChart) barChart = echarts.init(barRef.value)
}

function renderCharts() {
  if (lineChart && result.lineChart) {
    const chartData = result.lineChart
    lineChart.setOption({
      title: { text: 'OWNER TRAFFIC WAVE / 明文接入校验', left: 16, top: 12, textStyle: { fontSize: 13, color: '#bfe9e5', fontFamily: 'Chakra Petch' } },
      tooltip: { trigger: 'axis', backgroundColor: '#07171e', borderColor: '#14f1d9', textStyle: { color: '#dffaf7' } },
      grid: { left: 52, right: 24, top: 56, bottom: 42 },
      xAxis: { type: 'category', data: (chartData.time || []).map(fmtClock), boundaryGap: false, axisLine: { lineStyle: { color: '#294a50' } }, axisLabel: { color: '#668e92', hideOverlap: true } },
      yAxis: { type: 'value', axisLabel: { color: '#668e92' }, splitLine: { lineStyle: { color: 'rgba(118,228,221,.08)' } } },
      series: [{
        name: '车流量',
        type: 'line',
        smooth: true,
        showSymbol: false,
        data: chartData.avg || [],
        lineStyle: { width: 2.5, color: '#14f1d9', shadowBlur: 12, shadowColor: 'rgba(20,241,217,.45)' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(20,241,217,.25)' }, { offset: 1, color: 'rgba(20,241,217,.01)' }]) },
      }],
    })
  }
  if (barChart && result.barChart) {
    const chartData = result.barChart
    barChart.setOption({
      title: { text: 'FLOW DISTRIBUTION / 区间密度', left: 16, top: 12, textStyle: { fontSize: 13, color: '#bfe9e5', fontFamily: 'Chakra Petch' } },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#07171e', borderColor: '#14f1d9', textStyle: { color: '#dffaf7' } },
      grid: { left: 52, right: 24, top: 56, bottom: 42 },
      xAxis: { type: 'category', data: chartData.categories || [], axisLine: { lineStyle: { color: '#294a50' } }, axisLabel: { color: '#668e92' } },
      yAxis: { type: 'value', axisLabel: { color: '#668e92' }, splitLine: { lineStyle: { color: 'rgba(118,228,221,.08)' } } },
      series: [{
        name: '样本数',
        type: 'bar',
        data: chartData.counts || [],
        barWidth: '52%',
        itemStyle: { borderRadius: [6, 6, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#ffb020' }, { offset: 1, color: '#14f1d9' }]) },
      }],
    })
  }
}

function handleResize() {
  lineChart && lineChart.resize()
  barChart && barChart.resize()
}

onMounted(() => {
  loadStreams()
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
  <div class="owner-query">
    <div class="page-card command-card">
      <div class="page-toolbar">
        <h2 class="page-title">数据查询</h2>
        <el-tag effect="dark">OWNER DATA BLOCKS</el-tag>
      </div>
      <el-form class="query-grid" :model="form" @submit.prevent>
        <el-form-item label="选择流">
          <el-select v-model="form.streamId" filterable placeholder="选择交通流" @change="onStreamChange">
            <el-option v-for="stream in streams" :key="stream.id" :value="stream.id" :label="stream.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="流名称">
          <el-input :model-value="currentStream?.name || '-'" disabled />
        </el-form-item>
        <el-form-item label="流描述">
          <el-input :model-value="currentStream?.description || '-'" disabled />
        </el-form-item>
        <el-form-item label="聚合粒度(ms)">
          <el-input-number v-model="form.blockMillis" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%" />
        </el-form-item>
        <el-form-item label=" ">
          <el-button type="primary" :loading="loading" @click="handleQuery">
            <el-icon><Search /></el-icon> 数据查询
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="stat-strip" v-if="queried">
      <div v-for="card in cards" :key="card.label" class="stat-tile">
        <el-icon><component :is="card.icon" /></el-icon>
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
      </div>
    </div>

    <div class="page-card result-card" v-loading="loading">
      <template v-if="queried">
        <div class="owner-chart-grid">
          <div class="chart-box"><div ref="lineRef" class="chart"></div></div>
          <div class="chart-box"><div ref="barRef" class="chart"></div></div>
        </div>
        <el-table :data="result.blocks" height="420">
          <el-table-column prop="range" label="数据块区间" min-width="150" />
          <el-table-column label="时间范围" min-width="250">
            <template #default="{ row }">{{ fmtTime(row.startTime) }}—{{ fmtTime(row.endTime) }}</template>
          </el-table-column>
          <el-table-column prop="route" label="路段" width="110" />
          <el-table-column label="车流量" width="120">
            <template #default="{ row }">{{ Number(row.vehicleFlow).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="平均速度" width="120">
            <template #default="{ row }">{{ Number(row.avgSpeed).toFixed(1) }} km/h</template>
          </el-table-column>
          <el-table-column label="拥堵率" width="110">
            <template #default="{ row }">{{ Number(row.occupancy).toFixed(1) }}%</template>
          </el-table-column>
          <el-table-column label="积分值" width="120">
            <template #default="{ row }">{{ formatInt(row.integral) }}</template>
          </el-table-column>
        </el-table>
      </template>
      <el-empty v-else description="选择已上传 CSV 的交通流进行数据块查询" />
    </div>
  </div>
</template>

<style scoped>
.owner-query {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.command-card {
  background:
    radial-gradient(circle at 90% 12%, rgba(20, 241, 217, .12), transparent 28%),
    linear-gradient(145deg, rgba(16, 38, 49, .96), rgba(8, 23, 31, .96));
}
.query-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(220px, 1fr));
  gap: 2px 18px;
}
.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.stat-tile {
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: 2px 14px 2px 14px;
  background: rgba(3, 18, 24, .72);
}
.stat-tile .el-icon {
  color: var(--brand);
  font-size: 24px;
}
.stat-tile span {
  display: block;
  color: var(--text-sub);
  margin-top: 8px;
}
.stat-tile strong {
  display: block;
  margin-top: 4px;
  font: 700 24px 'Chakra Petch', sans-serif;
}
.result-card {
  min-height: 360px;
}
.owner-chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 18px;
}
.chart-box {
  border: 1px solid var(--line);
  border-radius: 2px 14px 2px 14px;
  background:
    radial-gradient(circle at 16% 12%, rgba(20, 241, 217, .08), transparent 28%),
    rgba(3, 18, 24, .62);
}
.chart {
  width: 100%;
  height: 320px;
}
@media (max-width: 1100px) {
  .query-grid,
  .stat-strip,
  .owner-chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
