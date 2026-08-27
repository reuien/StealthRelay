import { createRouter, createWebHistory } from 'vue-router'
import { userStore } from '../store/user'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  {
    path: '/admin',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { role: 'admin' },
    children: [
      { path: '', redirect: '/admin/overview' },
      { path: 'overview', name: 'admin-overview', component: () => import('../views/admin/Overview.vue') },
    ],
  },
  {
    path: '/owner',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { role: 'owner' },
    children: [
      { path: '', redirect: '/owner/equipments' },
      { path: 'equipments', name: 'owner-equipments', component: () => import('../views/owner/Equipments.vue') },
      { path: 'streams', name: 'owner-streams', component: () => import('../views/owner/Streams.vue') },
      { path: 'policies', name: 'owner-policies', component: () => import('../views/owner/Policies.vue') },
      { path: 'query', name: 'owner-query', component: () => import('../views/owner/DataQuery.vue') },
    ],
  },
  {
    path: '/consumer',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { role: 'consumer' },
    children: [
      { path: '', redirect: '/consumer/query' },
      { path: 'query', name: 'consumer-query', component: () => import('../views/consumer/Query.vue') },
      { path: 'federation', name: 'consumer-federation', component: () => import('../views/consumer/Federation.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.path === '/login') return true
  if (!userStore.isLoggedIn()) return '/login'
  if (to.meta.role && to.meta.role !== userStore.state.role) {
    return userStore.state.role === 'admin' ? '/admin' : userStore.state.role === 'owner' ? '/owner' : '/consumer'
  }
  return true
})

export default router
