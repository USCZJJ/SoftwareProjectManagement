import request from '@/utils/request'

export const projectApi = {
  list: (params: any) => request({ url: '/project/list', method: 'get', params }),
  detail: (id: number) => request({ url: `/project/${id}`, method: 'get' }),
  create: (data: any) => request({ url: '/project', method: 'post', data }),
  update: (id: number, data: any) => request({ url: `/project/${id}`, method: 'put', data }),
  delete: (id: number) => request({ url: `/project/${id}`, method: 'delete' }),
  getWbs: (projectId: number) => request({ url: `/project/${projectId}/wbs`, method: 'get' }),
  saveWbs: (projectId: number, data: any) => request({ url: `/project/${projectId}/wbs`, method: 'post', data }),
  updateWbsProgress: (projectId: number, wbsId: number, progress: number) =>
    request({ url: `/project/${projectId}/wbs/${wbsId}/progress`, method: 'put', params: { progress } }),
  deleteWbs: (projectId: number, wbsId: number) => request({ url: `/project/${projectId}/wbs/${wbsId}`, method: 'delete' }),
  getMilestones: (projectId: number) => request({ url: `/project/${projectId}/milestones`, method: 'get' }),
  saveMilestone: (projectId: number, data: any) => request({ url: `/project/${projectId}/milestone`, method: 'post', data }),
  getDependencies: (projectId: number) => request({ url: `/project/${projectId}/dependencies`, method: 'get' }),
  saveDependency: (projectId: number, data: any) => request({ url: `/project/${projectId}/dependency`, method: 'post', data }),
  deleteDependency: (projectId: number, depId: number) => request({ url: `/project/${projectId}/dependency/${depId}`, method: 'delete' }),
  criticalPath: (projectId: number) => request({ url: `/project/${projectId}/critical-path`, method: 'get' }),
  getChanges: (projectId: number) => request({ url: `/project/${projectId}/plan-changes`, method: 'get' }),
  submitChange: (projectId: number, data: any) => request({ url: `/project/${projectId}/plan-change`, method: 'post', data }),
  approveChange: (projectId: number, id: number, approved: boolean, comment: string) =>
    request({ url: `/project/${projectId}/plan-change/${id}/approve`, method: 'put', params: { approved, comment } }),
  getMembers: (projectId: number) => request({ url: `/project/${projectId}/members`, method: 'get' }),
  addMember: (projectId: number, userId: number, role: string) =>
    request({ url: `/project/${projectId}/member`, method: 'post', params: { userId, role } }),
  removeMember: (projectId: number, userId: number) =>
    request({ url: `/project/${projectId}/member/${userId}`, method: 'delete' }),
}
