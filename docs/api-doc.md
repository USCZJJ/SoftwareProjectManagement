# PMS 后端 API 接口文档

## 基础信息

| 项目 | 值 |
|---|---|
| 基础路径 | `http://{host}:{port}` |
| 响应格式 | JSON |
| 认证方式 | Bearer Token（请求头 `Authorization: Bearer <token>`） |

## 统一响应格式

所有接口返回统一包装：

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| code | 说明 |
|---|---|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器错误 |

---

## 1. 认证授权 — `/auth`

### 1.1 登录

```
POST /auth/login
```

**请求体**:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应**:

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGci...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@pms.com",
      "department": "技术部",
      "avatar": null
    }
  }
}
```

### 1.2 获取当前用户信息

```
GET /auth/user-info
```

**请求头**: `Authorization: Bearer <token>`

**响应**:

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "email": "admin@pms.com",
    "department": "技术部",
    "avatar": null
  }
}
```

---

## 2. 项目管理 — `/project`

### 2.1 获取项目列表

```
GET /project/list
```

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|---|---|---|---|---|
| keyword | String | 否 | - | 搜索关键词（匹配项目名/编号） |
| status | String | 否 | - | 状态：INIT/PLANNING/EXECUTING/MONITORING/CLOSED |
| page | int | 否 | 1 | 页码 |
| size | int | 否 | 10 | 每页条数 |

**响应**:

```json
{
  "code": 200,
  "data": {
    "records": [{
      "id": 1001,
      "projectCode": "PROJ-2026-001",
      "projectName": "电商平台重构项目",
      "description": "...",
      "objectives": "...",
      "startDate": "2026-06-01",
      "endDate": "2026-12-31",
      "budget": 500.00,
      "actualCost": 0,
      "status": "EXECUTING",
      "managerId": 2,
      "createTime": "2026-05-01T10:00:00"
    }],
    "total": 3,
    "current": 1,
    "size": 10
  }
}
```

### 2.2 获取项目详情

```
GET /project/{id}
```

**响应**: 包含 `project` 和 `members` 两部分。

```json
{
  "code": 200,
  "data": {
    "project": { "id": 1001, "projectName": "...", ... },
    "members": [
      { "id": 1, "projectId": 1001, "userId": 2, "role": "PM", "joinTime": "..." }
    ]
  }
}
```

### 2.3 创建项目

```
POST /project
```

**请求体**:

```json
{
  "projectName": "新项目",
  "description": "项目描述",
  "startDate": "2026-06-01",
  "endDate": "2026-12-31",
  "budget": 200.00,
  "status": "PLANNING",
  "managerId": 2
}
```

**响应**: 返回完整项目对象（含自动生成的 `projectCode`）。

### 2.4 更新项目

```
PUT /project/{id}
```

请求体同创建。

### 2.5 删除项目

```
DELETE /project/{id}
```

### 2.6 获取 WBS 树

```
GET /project/{projectId}/wbs
```

**响应**: 树形 WBS 节点列表，每个节点含 `children` 数组。

```json
{
  "code": 200,
  "data": [
    {
      "id": 2001,
      "projectId": 1001,
      "parentId": 0,
      "wbsCode": "1",
      "nodeName": "需求分析与设计",
      "plannedStart": "2026-06-01",
      "plannedEnd": "2026-07-15",
      "progress": 100,
      "children": [
        { "id": 2011, "parentId": 2001, "nodeName": "业务需求调研", ... }
      ]
    }
  ]
}
```

### 2.7 保存 WBS 节点

```
POST /project/{projectId}/wbs
```

**请求体**:

```json
{
  "parentId": 2001,
  "wbsCode": "1.4",
  "nodeName": "UI设计",
  "assigneeId": 3,
  "plannedStart": "2026-07-01",
  "plannedEnd": "2026-07-15",
  "plannedHours": 80.0
}
```

- `id` 为空时新建，不为空时更新

### 2.8 更新 WBS 进度

```
PUT /project/{projectId}/wbs/{wbsId}/progress
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| progress | Integer | 是 | 进度 0-100 |

### 2.9 删除 WBS 节点

```
DELETE /project/{projectId}/wbs/{wbsId}
```

有子节点的节点无法删除。

### 2.10 获取里程碑列表

```
GET /project/{projectId}/milestones
```

**响应**:

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "projectId": 1001,
      "wbsId": 2013,
      "milestoneName": "技术方案评审通过",
      "plannedDate": "2026-07-15",
      "actualDate": null,
      "status": "PENDING"
    }
  ]
}
```

### 2.11 保存里程碑

```
POST /project/{projectId}/milestone
```

**请求体**:

```json
{
  "milestoneName": "新里程碑",
  "plannedDate": "2026-08-15",
  "status": "PENDING",
  "wbsId": null
}
```

### 2.12 获取任务依赖列表

```
GET /project/{projectId}/dependencies
```

**响应**: 依赖列表，每个记录含 `predecessorWbsId`, `successorWbsId`, `dependencyType`, `lagDays`。

### 2.13 保存任务依赖

```
POST /project/{projectId}/dependency
```

**请求体**:

```json
{
  "predecessorWbsId": 2011,
  "successorWbsId": 2012,
  "dependencyType": "FS",
  "lagDays": 0
}
```

### 2.14 删除任务依赖

```
DELETE /project/{projectId}/dependency/{id}
```

### 2.15 计算关键路径

```
GET /project/{projectId}/critical-path
```

**响应**: 关键路径上的 WBS 节点 ID 列表。

```json
{
  "code": 200,
  "data": [2001, 2011, 2012, 2013]
}
```

### 2.16 获取计划变更列表

```
GET /project/{projectId}/plan-changes
```

### 2.17 提交计划变更

```
POST /project/{projectId}/plan-change
```

**请求体**:

```json
{
  "changeType": "SCHEDULE",
  "changeDesc": "延期一周交付",
  "reason": "人手不足",
  "impactAnalysis": "后续里程碑顺延",
  "applyUserId": 2
}
```

### 2.18 审批计划变更

```
PUT /project/{projectId}/plan-change/{id}/approve
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| approved | boolean | 是 | 是否通过 |
| comment | String | 否 | 审批意见 |

### 2.19 获取项目成员

```
GET /project/{projectId}/members
```

### 2.20 添加项目成员

```
POST /project/{projectId}/member
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| userId | Long | 是 | 用户ID |
| role | String | 是 | 角色（PM/DEV/QA/BA） |

### 2.21 移除项目成员

```
DELETE /project/{projectId}/member/{userId}
```

---

## 3. 需求管理 — `/requirement`

### 3.1 需求列表

```
GET /requirement/list
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| projectId | Long | 否 | 项目ID |
| keyword | String | 否 | 关键词 |
| status | String | 否 | 状态 |
| priority | String | 否 | MUST/SHOULD/COULD/WONT |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

### 3.2 需求详情

```
GET /requirement/{id}
```

### 3.3 创建需求

```
POST /requirement
```

**请求体**:

```json
{
  "projectId": 1001,
  "title": "订单支持部分退款",
  "description": "用户可选择部分商品退款",
  "type": "FUNCTIONAL",
  "priority": "MUST",
  "severity": 5
}
```

### 3.4 更新需求

```
PUT /requirement/{id}
```

### 3.5 删除需求

```
DELETE /requirement/{id}
```

### 3.6 提交评审

```
PUT /requirement/{id}/submit
```

**请求体**:

```json
{
  "reviewerId": 1
}
```

### 3.7 评审需求

```
PUT /requirement/{id}/review
```

**请求体**:

```json
{
  "status": "APPROVED",
  "reviewerId": 1
}
```

status 取值为 `APPROVED` 或 `REJECTED`。

### 3.8 提交需求变更

```
POST /requirement/change
```

**请求体**:

```json
{
  "reqId": 6001,
  "changeDesc": "增加团体退款场景",
  "reason": "业务新增场景",
  "impact": "需调整退款计算逻辑"
}
```

### 3.9 审批需求变更

```
PUT /requirement/change/{id}/approve
```

**请求体**:

```json
{
  "approved": true,
  "comment": "同意变更"
}
```

### 3.10 查询追溯矩阵

```
GET /requirement/trace
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| projectId | Long | 否 | 项目ID |
| reqId | Long | 否 | 需求ID |

### 3.11 添加追溯记录

```
POST /requirement/trace
```

**请求体**:

```json
{
  "projectId": 1001,
  "reqId": 6001,
  "taskId": 3004,
  "testCaseId": null,
  "traceNote": "关联任务"
}
```

### 3.12 删除追溯记录

```
DELETE /requirement/trace/{id}
```

---

## 4. 任务管理 — `/task`

### 4.1 获取看板

```
GET /task/kanban/{projectId}
```

**响应**:

```json
{
  "code": 200,
  "data": {
    "board": { "id": 1, "boardName": "...", "projectId": 1001, "isDefault": 1 },
    "columns": [
      {
        "id": 1, "columnName": "待办", "statusValue": "TODO",
        "wipLimit": 0, "sortOrder": 0, "color": "#3498db",
        "tasks": [
          { "id": 3001, "title": "微信支付SDK升级到V3", "status": "TODO", ... }
        ]
      }
    ]
  }
}
```

### 4.2 创建任务

```
POST /task
```

**请求体**:

```json
{
  "projectId": 1001,
  "wbsId": 2017,
  "title": "新任务",
  "type": "TASK",
  "priority": "MEDIUM",
  "kanbanColumnId": 1,
  "assigneeId": 3,
  "plannedHours": 40.0,
  "startDate": "2026-09-01",
  "dueDate": "2026-09-15"
}
```

### 4.3 更新任务

```
PUT /task/{id}
```

### 4.4 移动任务（跨列拖拽）

```
PUT /task/{id}/move
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| targetColumnId | Long | 是 | 目标看板列ID |
| newOrder | Integer | 是 | 在目标列中的排序位置 |

### 4.5 删除任务

```
DELETE /task/{id}
```

### 4.6 任务详情

```
GET /task/{id}
```

### 4.7 更新任务进度

```
PUT /task/{id}/progress
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| progress | Integer | 是 | 进度 0-100 |
| remainingHours | BigDecimal | 否 | 剩余工时 |

### 4.8 分配任务

```
PUT /task/{id}/assign
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| assigneeId | Long | 是 | 负责人用户ID |

### 4.9 任务列表（查询）

```
GET /task/list
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| projectId | Long | 否 | 项目ID |
| assigneeId | Long | 否 | 负责人ID |
| status | String | 否 | 状态 |
| keyword | String | 否 | 关键词 |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

### 4.10 我的任务

```
GET /task/my?page=1&size=10
```

返回当前登录用户被分配的任务。

### 4.11 任务操作日志

```
GET /task/{taskId}/logs
```

### 4.12 提交工时

```
POST /task/worklog
```

**请求体**:

```json
{
  "taskId": 3004,
  "projectId": 1001,
  "workDate": "2026-05-20",
  "hours": 8.0,
  "content": "完成状态机核心代码",
  "planDetail": "明天开始集成测试"
}
```

### 4.13 工时列表

```
GET /task/worklog/list
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| projectId | Long | 否 | 项目ID |
| userId | Long | 否 | 用户ID |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

### 4.14 我的工时

```
GET /task/worklog/my?startDate=2026-05-01&endDate=2026-05-31
```

### 4.15 提交任务评审

```
POST /task/{taskId}/review
```

**请求体**:

```json
{
  "result": "PASS",
  "score": 85,
  "comment": "代码质量好",
  "rejectReason": null
}
```

- `result`: PASS 或 REJECT
- `score`: 1-10 评分

### 4.16 任务评审记录

```
GET /task/{taskId}/reviews
```

---

## 5. 缺陷管理 — `/defect`

### 5.1 缺陷列表

```
GET /defect/list
```

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| projectId | Long | 否 | 项目ID |
| keyword | String | 否 | 关键词 |
| status | String | 否 | OPEN/ASSIGNED/FIXING/FIXED/VERIFYING/VERIFIED/CLOSED/REOPENED |
| severity | String | 否 | BLOCKER/CRITICAL/MAJOR/MINOR/TRIVIAL |
| assigneeId | Long | 否 | 修复人ID |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |

### 5.2 缺陷详情

```
GET /defect/{id}
```

### 5.3 创建缺陷

```
POST /defect
```

**请求体**:

```json
{
  "projectId": 1001,
  "title": "订单支付后状态未更新",
  "description": "高并发下偶发",
  "stepsToReproduce": "100并发同时支付",
  "severity": "BLOCKER",
  "priority": "URGENT"
}
```

### 5.4 更新缺陷

```
PUT /defect/{id}
```

### 5.5 分配缺陷

```
PUT /defect/{id}/assign
```

**请求体**:

```json
{
  "assigneeId": 3
}
```

### 5.6 更新缺陷状态

```
PUT /defect/{id}/status
```

**请求体**:

```json
{
  "status": "FIXING"
}
```

### 5.7 删除缺陷

```
DELETE /defect/{id}
```

### 5.8 测试用例列表

```
GET /defect/testcase/list?projectId=1001&page=1&size=10
```

### 5.9 创建测试用例

```
POST /defect/testcase
```

**请求体**:

```json
{
  "projectId": 1001,
  "title": "订单创建-正常流程",
  "precondition": "用户已登录",
  "testSteps": "[{\"step\":1,\"desc\":\"选择商品\"}]",
  "expectedResult": "订单创建成功",
  "type": "FUNCTIONAL",
  "priority": "HIGH"
}
```

### 5.10 更新测试用例

```
PUT /defect/testcase/{id}
```

### 5.11 删除测试用例

```
DELETE /defect/testcase/{id}
```

### 5.12 执行测试

```
POST /defect/testcase/execute
```

**请求体**:

```json
{
  "testCaseId": 5001,
  "executorId": 4,
  "result": "PASS",
  "actualResult": "订单创建成功",
  "defectId": null
}
```

### 5.13 测试执行历史

```
GET /defect/testcase/{id}/executions
```

### 5.14 质量检查清单

```
GET /defect/checklist?projectId=1001&stage=DESIGN
```

### 5.15 保存检查结果

```
POST /defect/checklist/result
```

**请求体**:

```json
{
  "checklistId": 1,
  "projectId": 1001,
  "result": "PASS",
  "remark": "已通过",
  "checkUserId": 1
}
```

### 5.16 查询检查结果

```
GET /defect/checklist/results?projectId=1001
```

---

## 6. 统计分析 — `/dashboard`

### 6.1 仪表盘统计

```
GET /dashboard/stats
```

**响应**:

```json
{
  "code": 200,
  "data": {
    "activeProjects": 2,
    "pendingTasks": 5,
    "openDefects": 3,
    "weekHours": "168.5"
  }
}
```

### 6.2 EVM 挣值分析

```
GET /dashboard/evm/{projectId}
```

**响应**:

```json
{
  "code": 200,
  "data": {
    "pv": "500.0",
    "ev": "380.0",
    "ac": "420.0",
    "spi": "0.76",
    "cpi": "0.90",
    "status": "BEHIND_SCHEDULE"
  }
}
```

| 指标 | 说明 |
|---|---|
| PV | 计划价值 |
| EV | 挣值 |
| AC | 实际成本 |
| SPI | 进度绩效指数 (<1 进度滞后) |
| CPI | 成本绩效指数 (<1 成本超支) |
| status | ON_TRACK / OVER_BUDGET / BEHIND_SCHEDULE |

### 6.3 缺陷统计

```
GET /dashboard/defect-stats/{projectId}
```

**响应**:

```json
{
  "code": 200,
  "data": {
    "total": 5,
    "bySeverity": { "BLOCKER": 1, "CRITICAL": 1, "MAJOR": 2, "MINOR": 1 },
    "byStatus": { "OPEN": 2, "ASSIGNED": 1, "FIXING": 1, "FIXED": 1 },
    "closeRate": "0%"
  }
}
```

### 6.4 工时统计

```
GET /dashboard/worklog-stats?projectId=1001&startDate=2026-05-01&endDate=2026-05-31
```

**响应**:

```json
{
  "code": 200,
  "data": [
    { "date": "2026-05-12", "hours": "6.0" },
    { "date": "2026-05-13", "hours": "15.5" }
  ]
}
```

---

## 附录：状态枚举值速查

### 项目状态
`INIT` `PLANNING` `EXECUTING` `MONITORING` `CLOSED`

### 任务状态
`TODO` `IN_PROGRESS` `IN_REVIEW` `DONE` `CLOSED`

### 任务类型
`TASK` `BUG` `FEATURE` `IMPROVEMENT`

### 任务优先级
`LOW` `MEDIUM` `HIGH` `URGENT`

### 需求状态
`DRAFT` `SUBMITTED` `REVIEWED` `APPROVED` `REJECTED` `CHANGED`

### 需求优先级 (MoSCoW)
`MUST` `SHOULD` `COULD` `WONT`

### 缺陷严重程度
`BLOCKER` `CRITICAL` `MAJOR` `MINOR` `TRIVIAL`

### 缺陷状态
`OPEN` `ASSIGNED` `FIXING` `FIXED` `VERIFYING` `VERIFIED` `CLOSED` `REOPENED`

### 依赖类型
`FS`（完成-开始） `SS`（开始-开始） `FF`（完成-完成） `SF`（开始-完成）

### 质量检查阶段
`DESIGN` `CODING` `TESTING` `DEPLOY`

### 测试用例类型
`FUNCTIONAL` `PERFORMANCE` `SECURITY`

### 测试执行结果
`PASS` `FAIL` `BLOCKED` `SKIP`

### 项目成员角色
`PM` `DEV` `QA` `BA`

### 公告类型
`INFO` `WARNING` `URGENT`
