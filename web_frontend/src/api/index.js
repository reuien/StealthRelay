import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 180000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['X-Token'] = token
  }
  return config
})

http.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body.code !== 'undefined') {
      if (body.code === 0) {
        return body.data
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (err) => {
    let msg = err.response?.data?.message || err.message || '网络错误'
    if (err.code === 'ECONNABORTED') {
      msg = '请求超时：CSV 上传/处理耗时较长，请确认后端仍在运行，或换用较小文件测试'
    } else if (msg === 'Network Error') {
      msg = '网络错误：请确认前端代理、网关 8080、数据服务 1101/1234 与 Kafka 都已启动'
    }
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export const api = {
  login: (number, password, role) => http.post('/login', { number, password, role }),
  register: (usrName, password, role) => http.post('/register', { usrName, password, role }),
  logout: () => http.post('/logout'),
  consumers: () => http.get('/consumers'),

  listEquipments: (ownerId) => http.get('/equipments', { params: { ownerId } }),
  createEquipment: (payload) => http.post('/equipments', payload),

  listStreams: (ownerId) => http.get('/streams', { params: { ownerId } }),
  createStream: (payload) => http.post('/streams', payload),
  deleteStream: (id) => http.delete(`/streams/${id}`),
  uploadStream: (id, file) => {
    if (!file) {
      return http.post(`/streams/${id}/upload`)
    }
    const formData = new FormData()
    formData.append('file', file)
    return http.post(`/streams/${id}/upload`, formData, {
      timeout: 600000,
    })
  },

  createPolicy: (payload) => http.post('/policies', payload),
  listOwnerPolicies: () => http.get('/owner/policies'),
  deleteOwnerPolicy: (policyId) => http.delete(`/owner/policies/${policyId}`),
  listPolicies: (consumer) => http.get('/policies', { params: { consumer } }),

  sharedStreams: (consumer, owner) => http.get('/shared-streams', { params: { consumer, owner } }),
  query: (payload) => http.post('/query', payload),

  ownerQuery: (payload) => http.post('/owner/query', payload),
  createFederationPolicy: (payload) => http.post('/federation/policies', payload),
  listOwnerFederationPolicies: () => http.get('/owner/federation/policies'),
  updateOwnerFederationPolicy: (policyId, payload) => http.put(`/owner/federation/policies/${policyId}`, payload),
  deleteOwnerFederationPolicy: (policyId) => http.delete(`/owner/federation/policies/${policyId}`),
  listFederationPolicies: (type) => http.get('/federation/policies', { params: { type } }),
  federationQuery: (payload) => http.post('/federation/query', payload),

  adminSummary: () => http.get('/admin/summary'),
  adminUsers: () => http.get('/admin/users'),
  adminSetUserStatus: (number, disabled, reason) => http.put(`/admin/users/${number}/status`, { disabled, reason }),
  adminDeleteUser: (number) => http.delete(`/admin/users/${number}`),
  adminStreams: () => http.get('/admin/streams'),
  adminDeleteStream: (id) => http.delete(`/admin/streams/${id}`),
  adminPolicies: () => http.get('/admin/policies'),
  adminPolicyDetail: (type, id) => http.get(`/admin/policies/${type}/${id}`),
  adminDeletePolicy: (type, id) => http.delete(`/admin/policies/${type}/${id}`),
  adminAudit: (params) => http.get('/admin/audit', { params }),
  adminTrace: (id) => http.get(`/admin/streams/${id}/trace`),
  adminAnchors: (params) => http.get('/admin/anchors', { params }),
  adminAnchorDetail: (id) => http.get(`/admin/anchors/${id}`),
  adminRetryAnchor: (id) => http.post(`/admin/anchors/${id}/retry`),
}

export default http
