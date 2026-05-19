import request from '@/utils/request'

export const taskApi = {
  kanban: (projectId: number) => request({ url: `/task/kanban/${projectId}`, method: 'get' }),
  create: (data: any) => request({ url: '/task', method: 'post', data }),
  update: (id: number, data: any) => request({ url: `/task/${id}`, method: 'put', data }),
  move: (id: number, targetColumnId: number, newOrder: number) =>
    request({ url: `/task/${id}/move`, method: 'put', params: { targetColumnId, newOrder } }),
  delete: (id: number) => request({ url: `/task/${id}`, method: 'delete' }),
  detail: (id: number) => request({ url: `/task/${id}`, method: 'get' }),
  updateProgress: (id: number, progress: number, remainingHours: number) =>
    request({ url: `/task/${id}/progress`, method: 'put', params: { progress, remainingHours } }),
  assign: (id: number, assigneeId: number) => request({ url: `/task/${id}/assign`, method: 'put', params: { assigneeId } }),
  list: (params: any) => request({ url: '/task/list', method: 'get', params }),
  myTasks: (params: any) => request({ url: '/task/my', method: 'get', params }),
  getLogs: (taskId: number) => request({ url: `/task/${taskId}/logs`, method: 'get' }),
  submitWorklog: (data: any) => request({ url: '/task/worklog', method: 'post', data }),
  getWorklogs: (params: any) => request({ url: '/task/worklog/list', method: 'get', params }),
  getMyWorklogs: (params: any) => request({ url: '/task/worklog/my', method: 'get', params }),
  reviewTask: (taskId: number, data: any) => request({ url: `/task/${taskId}/review`, method: 'post', data }),
  getReviews: (taskId: number) => request({ url: `/task/${taskId}/reviews`, method: 'get' }),
}
