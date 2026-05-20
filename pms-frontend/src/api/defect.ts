import request from '@/utils/request'

export const defectApi = {
  list: (params: any) => request({ url: '/defect/list', method: 'get', params }),
  detail: (id: number) => request({ url: `/defect/${id}`, method: 'get' }),
  create: (data: any) => request({ url: '/defect', method: 'post', data }),
  update: (id: number, data: any) => request({ url: `/defect/${id}`, method: 'put', data }),
  assign: (id: number, assigneeId: number) => request({ url: `/defect/${id}/assign`, method: 'put', data: { assigneeId } }),
  updateStatus: (id: number, status: string) => request({ url: `/defect/${id}/status`, method: 'put', data: { status } }),
  delete: (id: number) => request({ url: `/defect/${id}`, method: 'delete' }),
  testCaseList: (params: any) => request({ url: '/defect/testcase/list', method: 'get', params }),
  createTestCase: (data: any) => request({ url: '/defect/testcase', method: 'post', data }),
  updateTestCase: (id: number, data: any) => request({ url: `/defect/testcase/${id}`, method: 'put', data }),
  deleteTestCase: (id: number) => request({ url: `/defect/testcase/${id}`, method: 'delete' }),
  executeTest: (data: any) => request({ url: '/defect/testcase/execute', method: 'post', data }),
  getExecutions: (caseId: number) => request({ url: `/defect/testcase/${caseId}/executions`, method: 'get' }),
  getChecklist: (params: any) => request({ url: '/defect/checklist', method: 'get', params }),
  saveCheckResult: (data: any) => request({ url: '/defect/checklist/result', method: 'post', data }),
  getCheckResults: (params: any) => request({ url: '/defect/checklist/results', method: 'get', params }),
}
