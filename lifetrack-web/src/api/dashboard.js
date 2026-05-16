import http from './index'

export const dashboardApi = {
  getOverview: () => http.get('/dashboard/overview'),
  getCalendar: (params) => http.get('/dashboard/calendar', { params })
}
