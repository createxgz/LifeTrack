import http from './index'

export const taskApi = {
  list: (params) => http.get('/tasks', { params }),
  create: (data) => http.post('/tasks', data),
  update: (id, data) => http.put(`/tasks/${id}`, data),
  delete: (id) => http.delete(`/tasks/${id}`),
  detail: (id) => http.get(`/tasks/${id}`),
  checkin: (id, data) => http.post(`/tasks/${id}/checkin`, data),
  records: (id) => http.get(`/tasks/${id}/records`),
  stats: () => http.get('/tasks/stats')
}
