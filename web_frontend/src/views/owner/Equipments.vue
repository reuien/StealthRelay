<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import { userStore } from '../../store/user'

const list = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({
  name: '',
  port: '',
  ip: '',
})
const rules = {
  name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  ip: [{ required: true, message: '请输入 IP', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    list.value = await api.listEquipments(userStore.state.number)
  } catch (e) {
  } finally {
    loading.value = false
  }
}

function openDialog() {
  form.name = ''
  form.port = ''
  form.ip = ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function handleSubmit() {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  submitting.value = true
  try {
    await api.createEquipment({ name: form.name, port: form.port, ip: form.ip })
    ElMessage.success('设备注册成功')
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
  <div class="equipments-view">
    <section class="device-hero">
      <div>
        <span class="section-code">ROADSIDE NODE / ACCESS</span>
        <h2>路侧设备接入台</h2>
        <p>注册道路检测器、边缘网关或 CSV 数据代理设备，为交通流创建绑定可用的数据生产节点。</p>
      </div>
      <div class="device-orbit">
        <i></i><i></i><i></i>
        <b>{{ list.length }}</b>
        <small>ONLINE NODES</small>
      </div>
    </section>

    <div class="page-card device-card">
      <div class="page-toolbar">
        <div>
          <span class="section-code">DEVICE REGISTRY</span>
          <h2 class="page-title">设备管理</h2>
        </div>
        <el-button type="primary" @click="openDialog">
          <el-icon><Plus /></el-icon>注册路侧设备
        </el-button>
      </div>

      <div class="device-guide">
        <div><strong>01</strong><span>填写设备名称</span></div>
        <div><strong>02</strong><span>绑定 IP / 端口</span></div>
        <div><strong>03</strong><span>创建交通流时选择该设备</span></div>
      </div>

      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column prop="eqId" label="设备号" min-width="120" />
        <el-table-column prop="name" label="路侧节点名称" min-width="170" />
        <el-table-column prop="port" label="通信端口" min-width="110" />
        <el-table-column prop="ip" label="节点 IP" min-width="160" />
        <el-table-column label="接入状态" min-width="120">
          <template #default>
            <el-tag type="success" effect="light">READY</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" width="520px" class="traffic-dialog">
        <template #header>
          <div class="dialog-title">
            <span>ROADSIDE NODE</span>
            <strong>注册路侧设备</strong>
          </div>
        </template>
        <el-alert
          class="dialog-hint"
          type="info"
          :closable="false"
          show-icon
          title="设备用于创建交通流时绑定数据生产者；本地演示常用 127.0.0.1 与对应服务端口。"
        />
        <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
          <el-form-item label="节点名称" prop="name">
            <el-input v-model="form.name" placeholder="例如：G107-北向检测器" clearable />
          </el-form-item>
          <el-form-item label="通信端口" prop="port">
            <el-input v-model="form.port" placeholder="例如：1234" clearable />
          </el-form-item>
          <el-form-item label="节点 IP" prop="ip">
            <el-input v-model="form.ip" placeholder="例如：127.0.0.1" clearable />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">接入设备</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<style scoped>
.equipments-view {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.device-hero {
  position: relative;
  overflow: hidden;
  min-height: 168px;
  padding: 28px 34px;
  border: 1px solid var(--line);
  border-radius: 22px 3px 22px 3px;
  background:
    radial-gradient(circle at 82% 40%, rgba(20, 241, 217, .18), transparent 26%),
    linear-gradient(110deg, rgba(10, 35, 43, .98), rgba(4, 17, 24, .94));
}
.section-code {
  color: var(--brand);
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: .22em;
}
.device-hero h2 {
  margin: 8px 0;
  font: 600 30px 'Chakra Petch', 'Noto Sans SC', sans-serif;
}
.device-hero p {
  margin: 0;
  max-width: 620px;
  color: var(--text-sub);
}
.device-orbit {
  position: absolute;
  right: 44px;
  top: 28px;
  width: 118px;
  height: 118px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(20, 241, 217, .2);
  border-radius: 50%;
}
.device-orbit i {
  position: absolute;
  inset: 12px;
  border: 1px dashed rgba(20, 241, 217, .18);
  border-radius: 50%;
}
.device-orbit i:nth-child(2) { inset: 30px; }
.device-orbit i:nth-child(3) { inset: -18px; opacity: .4; }
.device-orbit b {
  color: var(--brand);
  font: 700 34px 'Chakra Petch', sans-serif;
}
.device-orbit small {
  position: absolute;
  bottom: -24px;
  color: var(--text-sub);
  font: 9px 'Chakra Petch', sans-serif;
}
.device-card .page-toolbar {
  align-items: flex-start;
}
.device-guide {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.device-guide div {
  padding: 13px 15px;
  border: 1px solid var(--line);
  background: rgba(20, 241, 217, .04);
}
.device-guide strong {
  margin-right: 10px;
  color: var(--brand);
  font-family: 'Chakra Petch', sans-serif;
}
.device-guide span {
  color: var(--text-sub);
}
.dialog-title span {
  display: block;
  color: var(--brand);
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: .2em;
}
.dialog-title strong {
  display: block;
  margin-top: 4px;
  font-size: 18px;
}
.dialog-hint {
  margin-bottom: 16px;
}
@media (max-width: 900px) {
  .device-orbit { display: none; }
  .device-guide { grid-template-columns: 1fr; }
}
</style>
