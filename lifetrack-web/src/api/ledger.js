import http from './index'

export const ledgerApi = {
  // 收支记录
  addRecord: (data) => http.post('/ledger/records', data),
  getRecords: (params) => http.get('/ledger/records', { params }),
  updateRecord: (id, data) => http.put(`/ledger/records/${id}`, data),
  deleteRecord: (id) => http.delete(`/ledger/records/${id}`),

  // 分类
  getCategories: () => http.get('/ledger/categories'),
  addCategory: (data) => http.post('/ledger/categories', data),
  updateCategory: (id, data) => http.put(`/ledger/categories/${id}`, data),
  deleteCategory: (id, migrateTo) => http.delete(`/ledger/categories/${id}`, { params: migrateTo ? { migrateTo } : {} }),

  // 钱包
  getWallets: () => http.get('/ledger/wallets'),
  addWallet: (data) => http.post('/ledger/wallets', data),
  updateWallet: (id, data) => http.put(`/ledger/wallets/${id}`, data),
  deleteWallet: (id) => http.delete(`/ledger/wallets/${id}`),

  // 统计 + 导出
  getStats: (params) => http.get('/ledger/stats', { params }),
  exportCSV: (params) => http.get('/ledger/stats/export', { params }),

  // 预算
  getBudgets: (params) => http.get('/ledger/budgets', { params }),
  saveBudget: (data) => http.post('/ledger/budgets', data),
  updateBudget: (id, data) => http.put(`/ledger/budgets/${id}`, data),
  deleteBudget: (id) => http.delete(`/ledger/budgets/${id}`)
}
