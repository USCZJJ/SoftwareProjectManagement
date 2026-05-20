import request from '@/utils/request'

export const reqApi = {
  list: (params: any) => request({ url: '/requirement/list', method: 'get', params }),
  detail: (id: number) => request({ url: `/requirement/${id}`, method: 'get' }),
  create: (data: any) => request({ url: '/requirement', method: 'post', data }),
  update: (id: number, data: any) => request({ url: `/requirement/${id}`, method: 'put', data }),
  delete: (id: number) => request({ url: `/requirement/${id}`, method: 'delete' }),
  submit: (id: number, reviewerId: number) =>
    request({ url: `/requirement/${id}/submit`, method: 'put', data: { reviewerId } }),
  review: (id: number, status: string, reviewerId: number) =>
    request({ url: `/requirement/${id}/review`, method: 'put', data: { status, reviewerId } }),
  submitChange: (data: any) => request({ url: '/requirement/change', method: 'post', data }),
  approveChange: (id: number, approved: boolean, comment: string) =>
    request({ url: `/requirement/change/${id}/approve`, method: 'put', data: { approved, comment } }),
  getTraces: (params: any) => request({ url: '/requirement/trace', method: 'get', params }),
  addTrace: (data: any) => request({ url: '/requirement/trace', method: 'post', data }),
  deleteTrace: (id: number) => request({ url: `/requirement/trace/${id}`, method: 'delete' }),
}
