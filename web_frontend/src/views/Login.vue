<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { userStore } from '../store/user'

const router = useRouter()
const mode = ref('login')
const loading = ref(false)

const form = reactive({
  number: '',
  usrName: '',
  password: '',
  role: 'owner',
})

async function handleLogin() {
  if (!form.number || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await api.login(form.number, form.password, form.role)
    userStore.setLogin(data)
    ElMessage.success('登录成功')
    router.replace(data.role === 'admin' ? '/admin' : data.role === 'owner' ? '/owner' : '/consumer')
  } catch (e) {
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!form.usrName || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await api.register(form.usrName, form.password, form.role)
    ElMessage.success(`注册成功，您的账号是 ${data.number}，请登录`)
    form.number = data.number
    mode.value = 'login'
  } catch (e) {
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="city-grid">
      <span class="route route-a"></span><span class="route route-b"></span><span class="route route-c"></span>
      <i class="signal s1"></i><i class="signal s2"></i><i class="signal s3"></i><i class="signal s4"></i>
    </div>
    <section class="hero">
      <div class="eyebrow"><i></i> URBAN MOBILITY DIGITAL TWIN</div>
      <h2>让每一条道路<br><em>成为可计算的数据流</em></h2>
      <p>接入路侧设备、交通检测器与历史 CSV 数据，在隐私授权边界内完成车辆流量分析、趋势查询与仿真推演。</p>
      <div class="hero-metrics">
        <div><strong>24/7</strong><span>实时路网监测</span></div>
        <div><strong>SECURE</strong><span>隐私策略控制</span></div>
        <div><strong>SIM</strong><span>交通流量仿真</span></div>
      </div>
    </section>
    <div class="login-card">
      <div class="brand">
        <div class="brand-code">TRAFFIC//OS <span>v2.6</span></div>
        <h1>交通流量数据平台</h1>
        <p>城市汽车流量采集 · 隐私授权 · 数字仿真</p>
      </div>

      <el-segmented
        v-model="mode"
        :options="[
          { label: '登录', value: 'login' },
          { label: '注册', value: 'register' },
        ]"
        block
        class="seg"
      />

      <el-form :model="form" label-position="top" size="large" @submit.prevent>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role">
            <el-radio-button value="owner">交通数据节点</el-radio-button>
            <el-radio-button value="consumer">仿真分析终端</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="mode === 'login'" label="账号">
          <el-input v-model="form.number" placeholder="4 位数字账号" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item v-else label="用户名">
          <el-input v-model="form.usrName" placeholder="设置用户名" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            @keyup.enter="mode === 'login' ? handleLogin() : handleRegister()"
          >
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="submit"
          :loading="loading"
          @click="mode === 'login' ? handleLogin() : handleRegister()"
        >
          {{ mode === 'login' ? '进入交通控制台' : '注册接入节点' }}
        </el-button>
      </el-form>

      <div class="hint">
        演示账号：拥有者 1001/1，消费者 11/11
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 70px;
  padding: 7vw;
  background:
    radial-gradient(circle at 15% 20%, rgba(20, 241, 217, 0.12), transparent 24%),
    radial-gradient(circle at 85% 85%, rgba(255, 176, 32, 0.08), transparent 20%),
    #050d12;
}
.city-grid {
  position: absolute;
  inset: 0;
  opacity: .8;
  background:
    linear-gradient(rgba(20, 241, 217, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(20, 241, 217, 0.035) 1px, transparent 1px);
  background-size: 48px 48px;
}
.route {
  position: absolute;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(20, 241, 217, .5), transparent);
  box-shadow: 0 0 15px rgba(20, 241, 217, .18);
}
.route-a { width: 65vw; top: 32%; left: -8%; transform: rotate(18deg); }
.route-b { width: 72vw; top: 64%; left: 5%; transform: rotate(-12deg); }
.route-c { width: 50vw; top: 48%; right: -15%; transform: rotate(52deg); }
.signal {
  position: absolute;
  width: 7px; height: 7px;
  border: 2px solid var(--brand);
  border-radius: 50%;
  box-shadow: 0 0 16px var(--brand);
  animation: pulse 2.4s infinite;
}
.s1 { top: 27%; left: 22%; }
.s2 { top: 57%; left: 39%; animation-delay: .6s; }
.s3 { top: 41%; right: 21%; animation-delay: 1.2s; }
.s4 { bottom: 16%; right: 36%; animation-delay: 1.8s; border-color: var(--signal); }
.hero {
  position: relative;
  z-index: 1;
  width: min(610px, 48vw);
}
.eyebrow {
  color: #72a4a6;
  font: 12px 'Chakra Petch', sans-serif;
  letter-spacing: .22em;
}
.eyebrow i {
  display: inline-block;
  width: 30px; height: 1px;
  margin-right: 10px;
  vertical-align: middle;
  background: var(--brand);
}
.hero h2 {
  margin: 24px 0;
  font: 600 clamp(38px, 4.3vw, 68px)/1.08 'Chakra Petch', 'Noto Sans SC', sans-serif;
  letter-spacing: -.04em;
}
.hero h2 em {
  color: transparent;
  font-style: normal;
  -webkit-text-stroke: 1px var(--brand);
  text-shadow: 0 0 30px rgba(20, 241, 217, .18);
}
.hero > p {
  max-width: 560px;
  color: #789a9d;
  line-height: 1.9;
}
.hero-metrics {
  display: flex;
  gap: 34px;
  margin-top: 42px;
}
.hero-metrics div {
  padding-left: 14px;
  border-left: 1px solid var(--line-strong);
}
.hero-metrics strong {
  display: block;
  color: var(--brand);
  font: 600 18px 'Chakra Petch', sans-serif;
}
.hero-metrics span {
  color: #53777b;
  font-size: 11px;
}
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  flex-shrink: 0;
  background: linear-gradient(145deg, rgba(16, 39, 50, .96), rgba(7, 21, 28, .96));
  border: 1px solid var(--line-strong);
  border-radius: 4px 24px 4px 24px;
  padding: 36px 36px 28px;
  box-shadow: 0 35px 100px rgba(0, 0, 0, 0.45);
}
.brand {
  text-align: left;
  margin-bottom: 22px;
}
.brand-code {
  color: var(--brand);
  font: 600 12px 'Chakra Petch', sans-serif;
  letter-spacing: .16em;
}
.brand-code span { color: #476b70; font-size: 9px; }
.brand h1 {
  font: 600 24px 'Chakra Petch', 'Noto Sans SC', sans-serif;
  margin: 12px 0 5px;
}
.brand p {
  font-size: 13px;
  color: var(--text-sub);
  margin: 0;
}
.seg {
  margin-bottom: 18px;
}
.submit {
  width: 100%;
}
.hint {
  margin-top: 16px;
  text-align: center;
  font-size: 12px;
  color: var(--text-sub);
}
@keyframes pulse {
  0%, 100% { transform: scale(.8); opacity: .45; }
  50% { transform: scale(1.6); opacity: 1; }
}
@media (max-width: 980px) {
  .login-page { justify-content: center; padding: 24px; }
  .hero { display: none; }
}
</style>
