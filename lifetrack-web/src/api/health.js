import http from './index'

export const healthApi = {
  // 体重记录
  addWeight: (data) => http.post('/health/weights', data),
  getWeights: (params) => http.get('/health/weights', { params }),
  updateWeight: (id, data) => http.put(`/health/weights/${id}`, data),
  deleteWeight: (id) => http.delete(`/health/weights/${id}`),
  getWeightTrend: (days = 90) => http.get('/health/weights/trend', { params: { days } }),

  // 饮食记录
  addDiet: (data) => http.post('/health/diets', data),
  getDiets: (params) => http.get('/health/diets', { params }),
  getDietStats: (date) => http.get('/health/diets/stats', { params: { date } }),

  // 食物库
  searchFoods: (params) => http.get('/foods', { params })
}
