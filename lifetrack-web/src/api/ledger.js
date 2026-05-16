import http from './index'

export const ledgerApi = {
  addRecord: (data) => http.post('/ledger/records', data),
  getRecords: (params) => http.get('/ledger/records', { params }),
  updateRecord: (id, data) => http.put(`/ledger/records/${id}`, data),
  deleteRecord: (id) => http.delete(`/ledger/records/${id}`),
  getCategories: () => http.get('/ledger/categories'),
  getStats: (params) => http.get('/ledger/stats', { params }),
  exportCSV: (params) => http.get('/ledger/stats/export', { params })
}
