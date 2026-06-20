import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export const circuitApi = {
  getAll: () => api.get('/circuits'),
  get: (id) => api.get(`/circuits/${id}`),
  create: (name, description) => api.post('/circuits', { name, description }),
  update: (id, name, description) => api.put(`/circuits/${id}`, { name, description }),
  delete: (id) => api.delete(`/circuits/${id}`),
  getDetail: (id) => api.get(`/circuits/${id}/detail`),
  saveDetail: (id, data) => api.post(`/circuits/${id}/detail`, data),
  simulate: (id) => api.post(`/circuits/${id}/simulate`)
}

export default api
