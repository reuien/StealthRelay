<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userStore } from '../store/user'
import { api } from '../api'

const route = useRoute()
const router = useRouter()

const isOwner = computed(() => userStore.state.role === 'owner')
const isAdmin = computed(() => userStore.state.role === 'admin')

const menus = computed(() =>
  isAdmin.value
    ? [{ index: '/admin/overview', title: '全域审计台', code: 'ROOT', icon: 'Monitor' }]
    : isOwner.value
    ? [
        { index: '/owner/equipments', title: '路侧设备', code: 'NODE', icon: 'Cpu' },
        { index: '/owner/streams', title: '交通流', code: 'FLOW', icon: 'DataLine' },
        { index: '/owner/policies', title: '数据授权', code: 'AUTH', icon: 'Lock' },
        { index: '/owner/query', title: '数据查询', code: 'QUERY', icon: 'Search' },
      ]
    : [
        { index: '/consumer/query', title: '流量仿真', code: 'SIM', icon: 'Search' },
        { index: '/consumer/federation', title: '联邦查询', code: 'MPC', icon: 'Connection' },
      ]
)

const activeMenu = computed(() => route.path)

async function handleLogout() {
  try {
    await api.logout()
  } catch (e) {
  }
  userStore.clear()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="logo-mark"><span></span><span></span><span></span></div>
        <div>
          <strong>TRAFFIC//OS</strong>
          <small>城市交通数字孪生</small>
        </div>
      </div>
      <div class="system-state"><i></i> NETWORK ONLINE</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.index" :index="m.index">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span><small>{{ m.code }}</small>
        </el-menu-item>
      </el-menu>
      <div class="aside-map">
        <span class="road road-a"></span><span class="road road-b"></span>
        <i class="node node-a"></i><i class="node node-b"></i><i class="node node-c"></i>
        <em>URBAN GRID / 07</em>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">
          <small>{{ isAdmin ? 'GLOBAL FORENSICS COMMAND' : isOwner ? 'TRAFFIC DATA COMMAND' : 'MOBILITY SIMULATION' }}</small>
          {{ isAdmin ? '超级管理员审计与溯源中心' : isOwner ? '交通数据控制台' : '汽车流量仿真中心' }}
        </div>
        <div class="header-right">
          <el-tag :type="isAdmin ? 'danger' : isOwner ? 'primary' : 'success'" effect="light" round>
            {{ isAdmin ? '唯一超级管理员' : isOwner ? '数据节点' : '仿真终端' }}
          </el-tag>
          <span class="user-name">{{ userStore.state.usrName }}（{{ userStore.state.number }}）</span>
          <el-button text type="danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>退出
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
  position: relative;
  z-index: 1;
}
.aside {
  position: relative;
  overflow: hidden;
  background: linear-gradient(180deg, #0c2029 0%, #061117 100%);
  color: #fff;
  border-right: 1px solid var(--line);
}
.logo {
  display: flex;
  align-items: center;
  gap: 13px;
  height: 76px;
  padding: 0 18px;
  color: #fff;
  border-bottom: 1px solid var(--line);
}
.logo strong {
  display: block;
  font-family: 'Chakra Petch', sans-serif;
  letter-spacing: 0.08em;
  font-size: 17px;
}
.logo small {
  display: block;
  color: var(--text-sub);
  font-size: 10px;
  margin-top: 3px;
}
.logo-mark {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  width: 27px;
  height: 27px;
  transform: skewX(-12deg);
}
.logo-mark span {
  width: 6px;
  background: var(--brand);
  box-shadow: 0 0 12px rgba(20, 241, 217, 0.55);
}
.logo-mark span:nth-child(1) { height: 12px; }
.logo-mark span:nth-child(2) { height: 25px; }
.logo-mark span:nth-child(3) { height: 18px; }
.system-state {
  padding: 14px 20px 6px;
  color: #5d888b;
  font: 10px 'Chakra Petch', sans-serif;
  letter-spacing: 0.14em;
}
.system-state i {
  display: inline-block;
  width: 6px;
  height: 6px;
  margin-right: 8px;
  border-radius: 50%;
  background: var(--brand);
  box-shadow: 0 0 9px var(--brand);
}
.menu {
  background: transparent;
  border-right: none;
  padding: 8px 10px;
}
.menu :deep(.el-menu-item) {
  color: #789a9d;
  height: 50px;
  border-radius: 2px 12px 2px 12px;
  margin-bottom: 6px;
  border: 1px solid transparent;
}
.menu :deep(.el-menu-item small) {
  margin-left: auto;
  font: 9px 'Chakra Petch', sans-serif;
  opacity: .55;
}
.menu :deep(.el-menu-item.is-active) {
  color: var(--brand);
  background: linear-gradient(90deg, rgba(20, 241, 217, 0.12), rgba(20, 241, 217, 0.02));
  border-color: rgba(20, 241, 217, 0.18);
  box-shadow: inset 3px 0 var(--brand);
}
.menu :deep(.el-menu-item:hover) {
  background: rgba(20, 241, 217, 0.06);
}
.aside-map {
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 24px;
  height: 120px;
  border: 1px solid rgba(20, 241, 217, 0.08);
  background: linear-gradient(135deg, rgba(20, 241, 217, 0.025), transparent);
  overflow: hidden;
}
.aside-map .road {
  position: absolute;
  background: rgba(20, 241, 217, 0.13);
}
.road-a { width: 160px; height: 1px; top: 52px; left: 8px; transform: rotate(-22deg); }
.road-b { width: 150px; height: 1px; top: 64px; left: 30px; transform: rotate(37deg); }
.aside-map .node {
  position: absolute;
  width: 6px; height: 6px; border-radius: 50%;
  background: var(--signal); box-shadow: 0 0 10px var(--signal);
}
.node-a { top: 30px; left: 42px; }
.node-b { top: 73px; left: 110px; }
.node-c { top: 43px; right: 24px; }
.aside-map em {
  position: absolute;
  bottom: 8px;
  left: 10px;
  color: #365b5f;
  font: normal 9px 'Chakra Petch', sans-serif;
  letter-spacing: .1em;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 76px;
  background: rgba(7, 20, 27, 0.9);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(14px);
}
.header-title {
  font-family: 'Chakra Petch', 'Noto Sans SC', sans-serif;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: .04em;
}
.header-title small {
  display: block;
  color: var(--brand);
  font-size: 9px;
  letter-spacing: .18em;
  margin-bottom: 3px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.user-name {
  color: var(--text-sub);
  font-size: 14px;
}
.main {
  position: relative;
  background:
    radial-gradient(circle at 90% 0%, rgba(20, 241, 217, 0.065), transparent 28%),
    radial-gradient(circle at 10% 100%, rgba(255, 176, 32, 0.045), transparent 24%),
    var(--bg-page);
  padding: 24px;
}
</style>
