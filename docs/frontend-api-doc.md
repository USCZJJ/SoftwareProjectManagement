# PMS 前端 API 接口文档

## 基础配置

| 配置项 | 值 |
|---|---|
| `baseURL` | `/api` |
| 超时时间 | 30 秒 |
| Token 传递 | 请求头 `Authorization: Bearer <token>` |
| 响应解包 | 拦截器自动提取 `data`，code≠200 时报错跳转登录 |
| HTTP 库 | axios |

## API 模块总览

| 模块 | 文件 | 接口数 | 使用的视图 |
|---|---|---|---|
| 认证 | `src/api/auth.ts` | 2 | LoginView, Layout |
| 项目 | `src/api/project.ts` | 21 | ProjectListView, ProjectDetailView, GanttView, StatisticsView |
| 任务 | `src/api/task.ts` | 15 | KanbanView, TaskListView |
| 需求 | `src/api/requirement.ts` | 12 | RequirementListView |
| 缺陷 | `src/api/defect.ts` | 13 | DefectListView |
| 仪表盘 | 内联调用 | 4 | DashboardView, StatisticsView |

---

## 1. 认证 API — `src/api/auth.ts`

### login(username, password)

```
POST /auth/login
```
- `username: string` — 用户名
- `password: string` — 密码
- 返回 `{ token, userInfo }`

### getUserInfo()

```
GET /auth/user-info
```
- 返回用户信息对象

---

## 2. 项目 API — `projectApi`

| 方法 | HTTP | 路径 | 参数 | 说明 |
|---|---|---|---|---|
| `list(params)` | GET | `/project/list` | keyword, status, page, size | 分页列表 |
| `detail(id)` | GET | `/project/{id}` | id | 项目详情+成员 |
| `create(data)` | POST | `/project` | 项目对象 | 新建 |
| `update(id, data)` | PUT | `/project/{id}` | id, 项目对象 | 更新 |
| `delete(id)` | DELETE | `/project/{id}` | id | 删除 |
| `getWbs(projectId)` | GET | `/project/{projectId}/wbs` | projectId | WBS树 |
| `saveWbs(projectId, data)` | POST | `/project/{projectId}/wbs` | projectId, WBS对象 | 新建/更新WBS |
| `updateWbsProgress(projectId, wbsId, progress)` | PUT | `/project/{projectId}/wbs/{wbsId}/progress` | Query: progress | 更新进度 |
| `deleteWbs(projectId, wbsId)` | DELETE | `/project/{projectId}/wbs/{wbsId}` | wbsId | 删除WBS |
| `getMilestones(projectId)` | GET | `/project/{projectId}/milestones` | projectId | 里程碑列表 |
| `saveMilestone(projectId, data)` | POST | `/project/{projectId}/milestone` | projectId, 里程碑对象 | 新建/更新 |
| `getDependencies(projectId)` | GET | `/project/{projectId}/dependencies` | projectId | 依赖列表 |
| `saveDependency(projectId, data)` | POST | `/project/{projectId}/dependency` | projectId, 依赖对象 | 新建/更新 |
| `deleteDependency(projectId, depId)` | DELETE | `/project/{projectId}/dependency/{depId}` | depId | 删除依赖 |
| `criticalPath(projectId)` | GET | `/project/{projectId}/critical-path` | projectId | 关键路径 |
| `getChanges(projectId)` | GET | `/project/{projectId}/plan-changes` | projectId | 变更列表 |
| `submitChange(projectId, data)` | POST | `/project/{projectId}/plan-change` | projectId, 变更对象 | 提交变更 |
| `approveChange(projectId, id, approved, comment)` | PUT | `/project/{projectId}/plan-change/{id}/approve` | Query: approved, comment | 审批变更 |
| `getMembers(projectId)` | GET | `/project/{projectId}/members` | projectId | 成员列表 |
| `addMember(projectId, userId, role)` | POST | `/project/{projectId}/member` | Query: userId, role | 添加成员 |
| `removeMember(projectId, userId)` | DELETE | `/project/{projectId}/member/{userId}` | userId | 移除成员 |

---

## 3. 任务 API — `taskApi`

| 方法 | HTTP | 路径 | 参数 | 说明 |
|---|---|---|---|---|
| `kanban(projectId)` | GET | `/task/kanban/{projectId}` | projectId | 看板+列+任务 |
| `create(data)` | POST | `/task` | 任务对象 | 创建任务 |
| `update(id, data)` | PUT | `/task/{id}` | id, 任务对象 | 更新任务 |
| `move(id, targetColumnId, newOrder)` | PUT | `/task/{id}/move` | Query: targetColumnId, newOrder | 移动任务 |
| `delete(id)` | DELETE | `/task/{id}` | id | 删除任务 |
| `detail(id)` | GET | `/task/{id}` | id | 任务详情 |
| `updateProgress(id, progress, remainingHours)` | PUT | `/task/{id}/progress` | Query: progress, remainingHours | 更新进度 |
| `assign(id, assigneeId)` | PUT | `/task/{id}/assign` | Query: assigneeId | 分配任务 |
| `list(params)` | GET | `/task/list` | projectId, assigneeId, status, keyword, page, size | 任务列表 |
| `myTasks(params)` | GET | `/task/my` | page, size | 我的任务 |
| `getLogs(taskId)` | GET | `/task/{taskId}/logs` | taskId | 操作日志 |
| `submitWorklog(data)` | POST | `/task/worklog` | 工时对象 | 提交工时 |
| `getWorklogs(params)` | GET | `/task/worklog/list` | projectId, userId, startDate, endDate | 工时列表 |
| `getMyWorklogs(params)` | GET | `/task/worklog/my` | startDate, endDate | 我的工时 |
| `reviewTask(taskId, data)` | POST | `/task/{taskId}/review` | taskId, 评审对象(Body) | 提交评审 |
| `getReviews(taskId)` | GET | `/task/{taskId}/reviews` | taskId | 评审记录 |

---

## 4. 需求 API — `reqApi`

| 方法 | HTTP | 路径 | 参数 | 说明 |
|---|---|---|---|---|
| `list(params)` | GET | `/requirement/list` | projectId, keyword, status, priority, page, size | 分页列表 |
| `detail(id)` | GET | `/requirement/{id}` | id | 详情 |
| `create(data)` | POST | `/requirement` | 需求对象 | 创建 |
| `update(id, data)` | PUT | `/requirement/{id}` | id, 需求对象 | 更新 |
| `delete(id)` | DELETE | `/requirement/{id}` | id | 删除 |
| `submit(id, reviewerId)` | PUT | `/requirement/{id}/submit` | Body: reviewerId | 提交评审 |
| `review(id, status, reviewerId)` | PUT | `/requirement/{id}/review` | Body: status, reviewerId | 评审 |
| `submitChange(data)` | POST | `/requirement/change` | 变更对象 | 提交变更 |
| `approveChange(id, approved, comment)` | PUT | `/requirement/change/{id}/approve` | Body: approved, comment | 审批变更 |
| `getTraces(params)` | GET | `/requirement/trace` | projectId, reqId | 追溯矩阵 |
| `addTrace(data)` | POST | `/requirement/trace` | 追溯对象 | 添加追溯 |
| `deleteTrace(id)` | DELETE | `/requirement/trace/{id}` | id | 删除追溯 |

---

## 5. 缺陷 API — `defectApi`

| 方法 | HTTP | 路径 | 参数 | 说明 |
|---|---|---|---|---|
| `list(params)` | GET | `/defect/list` | projectId, keyword, status, severity, assigneeId, page, size | 分页列表 |
| `detail(id)` | GET | `/defect/{id}` | id | 详情 |
| `create(data)` | POST | `/defect` | 缺陷对象 | 创建 |
| `update(id, data)` | PUT | `/defect/{id}` | id, 缺陷对象 | 更新 |
| `assign(id, assigneeId)` | PUT | `/defect/{id}/assign` | Body: assigneeId | 分配 |
| `updateStatus(id, status)` | PUT | `/defect/{id}/status` | Body: status | 更新状态 |
| `delete(id)` | DELETE | `/defect/{id}` | id | 删除 |
| `testCaseList(params)` | GET | `/defect/testcase/list` | projectId, page, size | 用例列表 |
| `createTestCase(data)` | POST | `/defect/testcase` | 用例对象 | 创建用例 |
| `updateTestCase(id, data)` | PUT | `/defect/testcase/{id}` | id, 用例对象 | 更新用例 |
| `deleteTestCase(id)` | DELETE | `/defect/testcase/{id}` | id | 删除用例 |
| `executeTest(data)` | POST | `/defect/testcase/execute` | 执行对象 | 执行测试 |
| `getExecutions(caseId)` | GET | `/defect/testcase/{caseId}/executions` | caseId | 执行历史 |
| `getChecklist(params)` | GET | `/defect/checklist` | projectId, stage | 检查清单 |
| `saveCheckResult(data)` | POST | `/defect/checklist/result` | 结果对象 | 保存结果 |
| `getCheckResults(params)` | GET | `/defect/checklist/results` | projectId | 检查结果 |

---

## 6. 仪表盘 API — 内联调用

统计视图通过 `request()` 直接调用，不经过 API 模块封装：

| HTTP | 路径 | 参数 | 调用位置 |
|---|---|---|---|
| GET | `/dashboard/stats` | - | DashboardView |
| GET | `/dashboard/evm/{projectId}` | projectId | StatisticsView |
| GET | `/dashboard/defect-stats/{projectId}` | projectId | StatisticsView |
| GET | `/dashboard/worklog-stats` | projectId, userId, startDate, endDate | StatisticsView |

---

## 请求与响应格式

### 请求拦截器行为

1. 自动添加 `Authorization: Bearer <token>` 请求头
2. `baseURL` 为 `/api`，所有请求自动拼接前缀

### 响应拦截器行为

1. `code === 200` → 返回 `response.data`（已解包）
2. `code !== 200` → `ElMessage.error` 提示错误
3. `code === 401/1002/1003` → 清除 token，跳转登录页
4. HTTP 401 → 同样跳转登录

### 调用示例

```typescript
// GET 请求
const res = await projectApi.list({ keyword: '电商', page: 1, size: 10 })
// res.data.records 为项目数组

// POST 请求 (body)
await projectApi.create({ projectName: '新项目', startDate: '2026-06-01' })

// PUT 请求 (query params)
await taskApi.assign(taskId, userId)

// PUT 请求 (body)
await reqApi.submit(reqId, reviewerId)  // data: { reviewerId }
```
