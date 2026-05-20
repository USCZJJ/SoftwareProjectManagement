# PMS 数据库设计文档

## 数据库基本信息

| 属性 | 值 |
|---|---|
| 数据库名 | `pms` |
| 字符集 | `utf8mb4` |
| 排序规则 | `utf8mb4_unicode_ci` |
| 存储引擎 | InnoDB |

---

## 1. 用户与权限模块

### 1.1 sys_user（用户表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 用户ID，主键 |
| username | VARCHAR(50) | NOT NULL | - | 用户名，唯一 |
| password | VARCHAR(255) | NOT NULL | - | 密码（BCrypt加密） |
| real_name | VARCHAR(50) | - | NULL | 真实姓名 |
| email | VARCHAR(100) | - | NULL | 邮箱 |
| phone | VARCHAR(20) | - | NULL | 手机号 |
| avatar | VARCHAR(255) | - | NULL | 头像URL |
| department | VARCHAR(100) | - | NULL | 部门 |
| position | VARCHAR(50) | - | NULL | 职位 |
| status | TINYINT | - | 1 | 状态：1启用 0禁用 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **唯一索引**: `uk_username` (`username`)

---

### 1.2 sys_role（角色表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 角色ID，主键 |
| role_name | VARCHAR(50) | NOT NULL | - | 角色名称 |
| role_code | VARCHAR(50) | NOT NULL | - | 角色编码，唯一 |
| description | VARCHAR(200) | - | NULL | 描述 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **唯一索引**: `uk_role_code` (`role_code`)

---

### 1.3 sys_permission（权限表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 权限ID，主键 |
| perm_name | VARCHAR(50) | NOT NULL | - | 权限名称 |
| perm_code | VARCHAR(50) | NOT NULL | - | 权限编码 |
| perm_type | VARCHAR(20) | - | NULL | 类型：menu/button/api |
| parent_id | BIGINT | - | 0 | 父级权限ID |
| path | VARCHAR(200) | - | NULL | 路由或接口路径 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`

---

## 2. 项目计划模块

### 2.1 pms_project（项目表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 项目ID，主键 |
| project_code | VARCHAR(30) | NOT NULL | - | 项目编号（PROJ-YYYY-NNN） |
| project_name | VARCHAR(200) | NOT NULL | - | 项目名称 |
| description | TEXT | - | NULL | 项目描述 |
| objectives | TEXT | - | NULL | 项目目标 |
| start_date | DATE | - | NULL | 开始日期 |
| end_date | DATE | - | NULL | 结束日期 |
| budget | DECIMAL(15,2) | - | NULL | 预算 |
| actual_cost | DECIMAL(15,2) | - | 0 | 实际成本 |
| status | VARCHAR(20) | - | INIT | 状态：INIT/PLANNING/EXECUTING/MONITORING/CLOSED |
| manager_id | BIGINT | - | NULL | 项目经理ID |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **唯一索引**: `uk_project_code` (`project_code`)
- **普通索引**: `idx_status` (`status`), `idx_manager` (`manager_id`)

---

### 2.2 pms_project_member（项目成员表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键ID |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| user_id | BIGINT | NOT NULL | - | 用户ID |
| role | VARCHAR(30) | - | NULL | 项目角色：PM/DEV/QA/BA |
| join_time | DATETIME | - | CURRENT_TIMESTAMP | 加入时间 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **唯一索引**: `uk_project_user` (`project_id`, `user_id`)

---

### 2.3 pms_wbs（WBS工作分解结构表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 所属项目ID |
| parent_id | BIGINT | - | 0 | 父节点ID |
| wbs_code | VARCHAR(30) | - | NULL | WBS编码 |
| node_name | VARCHAR(200) | NOT NULL | - | 节点名称 |
| description | TEXT | - | NULL | 详细描述 |
| assignee_id | BIGINT | - | NULL | 负责人ID |
| planned_start | DATE | - | NULL | 计划开始日期 |
| planned_end | DATE | - | NULL | 计划结束日期 |
| actual_start | DATE | - | NULL | 实际开始日期 |
| actual_end | DATE | - | NULL | 实际结束日期 |
| planned_hours | DECIMAL(10,1) | - | NULL | 计划工时 |
| actual_hours | DECIMAL(10,1) | - | NULL | 实际工时 |
| progress | INT | - | 0 | 进度百分比（0-100） |
| sort_order | INT | - | 0 | 排序 |
| is_milestone | TINYINT | - | 0 | 是否为里程碑 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`), `idx_parent` (`parent_id`)

---

### 2.4 pms_milestone（里程碑表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| wbs_id | BIGINT | - | NULL | 关联WBS节点ID |
| milestone_name | VARCHAR(200) | NOT NULL | - | 里程碑名称 |
| description | TEXT | - | NULL | 描述 |
| planned_date | DATE | NOT NULL | - | 计划完成日期 |
| actual_date | DATE | - | NULL | 实际完成日期 |
| status | VARCHAR(20) | - | PENDING | 状态：PENDING/ACHIEVED/DELAYED |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`)

---

### 2.5 pms_task_dependency（任务依赖关系表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| predecessor_wbs_id | BIGINT | NOT NULL | - | 前置任务WBS ID |
| successor_wbs_id | BIGINT | NOT NULL | - | 后置任务WBS ID |
| dependency_type | VARCHAR(2) | - | FS | 依赖类型：FS/SS/FF/SF |
| lag_days | INT | - | 0 | 滞后天数 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **唯一索引**: `uk_dep` (`predecessor_wbs_id`, `successor_wbs_id`)
- **普通索引**: `idx_project` (`project_id`)

---

### 2.6 pms_plan_change（计划变更记录表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| change_type | VARCHAR(30) | - | NULL | 变更类型：WBS/SCHEDULE/MILESTONE |
| change_desc | TEXT | NOT NULL | - | 变更描述 |
| reason | TEXT | - | NULL | 原因 |
| impact_analysis | TEXT | - | NULL | 影响分析 |
| affected_items | TEXT | - | NULL | 影响范围（JSON格式） |
| apply_user_id | BIGINT | - | NULL | 申请人ID |
| approve_user_id | BIGINT | - | NULL | 审批人ID |
| status | VARCHAR(20) | - | PENDING | 状态：PENDING/APPROVED/REJECTED |
| approve_comment | TEXT | - | NULL | 审批意见 |
| version_snapshot | TEXT | - | NULL | 变更前版本快照（JSON格式） |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`)

---

## 3. 需求管理模块

### 3.1 pms_requirement（需求记录表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| req_code | VARCHAR(30) | - | NULL | 需求编号（REQ-PROJID-NNN） |
| title | VARCHAR(200) | NOT NULL | - | 标题 |
| description | TEXT | - | NULL | 描述 |
| type | VARCHAR(20) | - | NULL | 类型：FUNCTIONAL/NON_FUNCTIONAL/UI |
| priority | VARCHAR(10) | - | COULD | MoSCoW优先级：MUST/SHOULD/COULD/WONT |
| severity | INT | - | NULL | 重要度（1-5） |
| source | VARCHAR(50) | - | NULL | 来源：客户/内部/市场 |
| status | VARCHAR(20) | - | DRAFT | 状态：DRAFT/SUBMITTED/REVIEWED/APPROVED/REJECTED/CHANGED |
| submit_user_id | BIGINT | - | NULL | 提交人ID |
| review_user_id | BIGINT | - | NULL | 评审人ID |
| assignee_id | BIGINT | - | NULL | 负责人ID |
| version | INT | - | 1 | 版本号 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`), `idx_status` (`status`)

---

### 3.2 pms_req_trace（需求跟踪矩阵RTM表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| req_id | BIGINT | NOT NULL | - | 需求ID |
| task_id | BIGINT | - | NULL | 关联任务ID |
| defect_id | BIGINT | - | NULL | 关联缺陷ID |
| test_case_id | BIGINT | - | NULL | 关联测试用例ID |
| trace_note | VARCHAR(200) | - | NULL | 跟踪备注 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |

- **主键**: `id`
- **普通索引**: `idx_req` (`req_id`), `idx_project` (`project_id`)

---

### 3.3 pms_req_change（需求变更记录表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| req_id | BIGINT | NOT NULL | - | 需求ID |
| change_desc | TEXT | NOT NULL | - | 变更描述 |
| reason | TEXT | - | NULL | 原因 |
| impact | TEXT | - | NULL | 影响分析 |
| affected_reqs | TEXT | - | NULL | 受影响的关联需求 |
| apply_user_id | BIGINT | - | NULL | 申请人ID |
| status | VARCHAR(20) | - | PENDING | 状态：PENDING/APPROVED/REJECTED |
| approve_user_id | BIGINT | - | NULL | 审批人ID |
| approve_comment | TEXT | - | NULL | 审批意见 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |

- **主键**: `id`
- **普通索引**: `idx_req` (`req_id`)

---

## 4. 任务管理模块

### 4.1 pms_task（任务表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| wbs_id | BIGINT | - | NULL | 关联WBS节点ID |
| title | VARCHAR(200) | NOT NULL | - | 标题 |
| description | TEXT | - | NULL | 描述 |
| type | VARCHAR(20) | - | TASK | 类型：TASK/BUG/FEATURE/IMPROVEMENT |
| priority | VARCHAR(10) | - | MEDIUM | 优先级：LOW/MEDIUM/HIGH/URGENT |
| status | VARCHAR(20) | - | TODO | 状态：TODO/IN_PROGRESS/IN_REVIEW/DONE/CLOSED |
| assignee_id | BIGINT | - | NULL | 执行人ID |
| reporter_id | BIGINT | - | NULL | 创建人ID |
| kanban_column_id | BIGINT | - | NULL | 看板列ID |
| planned_hours | DECIMAL(10,1) | - | NULL | 计划工时 |
| actual_hours | DECIMAL(10,1) | - | 0 | 实际工时 |
| progress | INT | - | 0 | 进度（0-100） |
| start_date | DATE | - | NULL | 开始日期 |
| due_date | DATE | - | NULL | 截止日期 |
| sort_order | INT | - | 0 | 在看板列中的排序 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`), `idx_assignee` (`assignee_id`), `idx_status` (`status`), `idx_kanban_column` (`kanban_column_id`)

---

### 4.2 pms_task_log（任务操作日志表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| task_id | BIGINT | NOT NULL | - | 任务ID |
| oper_user_id | BIGINT | - | NULL | 操作人ID |
| action | VARCHAR(30) | - | NULL | 操作类型：CREATE/STATUS_CHANGE/ASSIGN/UPDATE/DELETE |
| old_value | VARCHAR(500) | - | NULL | 旧值 |
| new_value | VARCHAR(500) | - | NULL | 新值 |
| remark | VARCHAR(500) | - | NULL | 备注 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |

- **主键**: `id`
- **普通索引**: `idx_task` (`task_id`)

---

### 4.3 pms_worklog（工时登记表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| task_id | BIGINT | NOT NULL | - | 任务ID |
| user_id | BIGINT | NOT NULL | - | 用户ID |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| work_date | DATE | NOT NULL | - | 工作日期 |
| hours | DECIMAL(5,2) | NOT NULL | - | 工时（小时） |
| content | TEXT | - | NULL | 工作内容 |
| plan_detail | TEXT | - | NULL | 明日计划 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_user_date` (`user_id`, `work_date`), `idx_project` (`project_id`), `idx_task` (`task_id`)

---

### 4.4 pms_kanban_board（看板表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| board_name | VARCHAR(100) | NOT NULL | - | 看板名称 |
| description | VARCHAR(500) | - | NULL | 描述 |
| is_default | TINYINT | - | 0 | 是否默认看板 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`)

---

### 4.5 pms_kanban_column（看板列表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| board_id | BIGINT | NOT NULL | - | 所属看板ID |
| column_name | VARCHAR(50) | NOT NULL | - | 列名称 |
| status_value | VARCHAR(20) | NOT NULL | - | 对应任务状态：TODO/IN_PROGRESS/IN_REVIEW/DONE |
| wip_limit | INT | - | 0 | WIP限制（0=不限制） |
| sort_order | INT | - | 0 | 排序 |
| color | VARCHAR(20) | - | NULL | 列颜色 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_board` (`board_id`)

---

### 4.6 pms_task_review（任务质量评审表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| task_id | BIGINT | NOT NULL | - | 任务ID |
| reviewer_id | BIGINT | - | NULL | 评审人ID |
| result | VARCHAR(20) | - | NULL | 评审结果：PASS/REJECT |
| score | INT | - | NULL | 评分（1-10） |
| comment | TEXT | - | NULL | 评审意见 |
| reject_reason | TEXT | - | NULL | 退回原因 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |

- **主键**: `id`
- **普通索引**: `idx_task` (`task_id`)

---

## 5. 缺陷管理模块

### 5.1 pms_defect（缺陷记录表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| defect_code | VARCHAR(30) | - | NULL | 缺陷编号（DEF-PROJID-NNN） |
| title | VARCHAR(200) | NOT NULL | - | 标题 |
| description | TEXT | - | NULL | 描述 |
| steps_to_reproduce | TEXT | - | NULL | 重现步骤 |
| expected_result | TEXT | - | NULL | 期望结果 |
| actual_result | TEXT | - | NULL | 实际结果 |
| severity | VARCHAR(20) | - | MINOR | 严重程度：BLOCKER/CRITICAL/MAJOR/MINOR/TRIVIAL |
| priority | VARCHAR(10) | - | MEDIUM | 优先级：LOW/MEDIUM/HIGH/URGENT |
| status | VARCHAR(20) | - | OPEN | 状态：OPEN/ASSIGNED/FIXING/FIXED/VERIFYING/VERIFIED/CLOSED/REOPENED |
| module | VARCHAR(100) | - | NULL | 所属模块 |
| version_found | VARCHAR(30) | - | NULL | 发现版本 |
| version_fixed | VARCHAR(30) | - | NULL | 修复版本 |
| reporter_id | BIGINT | - | NULL | 报告人ID |
| assignee_id | BIGINT | - | NULL | 修复人ID |
| screenshot_urls | TEXT | - | NULL | 截图URL（JSON数组） |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`), `idx_status` (`status`), `idx_assignee` (`assignee_id`)

---

### 5.2 pms_test_case（测试用例表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| case_code | VARCHAR(30) | - | NULL | 用例编号 |
| title | VARCHAR(200) | NOT NULL | - | 标题 |
| description | TEXT | - | NULL | 描述 |
| precondition | TEXT | - | NULL | 前置条件 |
| test_steps | TEXT | - | NULL | 测试步骤（JSON格式） |
| expected_result | TEXT | - | NULL | 期望结果 |
| type | VARCHAR(20) | - | FUNCTIONAL | 类型：FUNCTIONAL/PERFORMANCE/SECURITY |
| priority | VARCHAR(10) | - | MEDIUM | 优先级：LOW/MEDIUM/HIGH |
| status | VARCHAR(20) | - | DRAFT | 状态：DRAFT/ACTIVE/DEPRECATED |
| suite_id | BIGINT | - | NULL | 测试套件ID |
| create_user_id | BIGINT | - | NULL | 创建人ID |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | - | CURRENT_TIMESTAMP | 更新时间 |
| deleted | TINYINT | - | 0 | 逻辑删除 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`), `idx_suite` (`suite_id`)

---

### 5.3 pms_test_execution（测试执行记录表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| test_case_id | BIGINT | NOT NULL | - | 测试用例ID |
| executor_id | BIGINT | - | NULL | 执行人ID |
| result | VARCHAR(20) | - | NULL | 执行结果：PASS/FAIL/BLOCKED/SKIP |
| actual_result | TEXT | - | NULL | 实际结果 |
| remark | VARCHAR(500) | - | NULL | 备注 |
| defect_id | BIGINT | - | NULL | 关联缺陷ID |
| exec_time | DATETIME | - | CURRENT_TIMESTAMP | 执行时间 |

- **主键**: `id`
- **普通索引**: `idx_case` (`test_case_id`)

---

### 5.4 pms_quality_checklist（质量检查单表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| stage | VARCHAR(30) | - | NULL | 阶段：DESIGN/CODING/TESTING/DEPLOY |
| check_item | VARCHAR(200) | NOT NULL | - | 检查项 |
| description | TEXT | - | NULL | 描述 |
| weight | INT | - | 1 | 权重 |
| is_required | TINYINT | - | 0 | 是否必检 |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`)

---

### 5.5 pms_quality_check_result（质量检查结果表）

| 字段名 | 数据类型 | 是否必填 | 默认值 | 说明 |
|---|---|---|---|---|
| id | BIGINT | NOT NULL | - | 主键 |
| checklist_id | BIGINT | NOT NULL | - | 检查单ID |
| project_id | BIGINT | NOT NULL | - | 项目ID |
| result | VARCHAR(10) | - | NULL | 检查结果：PASS/FAIL/NA |
| remark | VARCHAR(500) | - | NULL | 备注 |
| check_user_id | BIGINT | - | NULL | 检查人ID |
| check_time | DATETIME | - | CURRENT_TIMESTAMP | 检查时间 |

- **主键**: `id`
- **普通索引**: `idx_project` (`project_id`)

---

---

## 附录：数据类型速查

| 数据类型 | 说明 | Java 映射 |
|---|---|---|
| BIGINT | 64位整数，主键和外键统一使用 | `Long` |
| VARCHAR(N) | 变长字符串，N为最大长度 | `String` |
| TEXT | 长文本 | `String` |
| DECIMAL(M,D) | 定点小数，M为总位数，D为小数位数 | `BigDecimal` |
| INT | 32位整数 | `Integer` |
| TINYINT | 8位整数，用于布尔标志 | `Integer` |
| DATE | 日期（年-月-日） | `LocalDate` |
| DATETIME | 日期时间（年-月-日 时:分:秒） | `LocalDateTime` |

## 附录：公用字段说明

所有业务主表均包含以下公用字段（关联表除外）：

| 字段名 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键，雪花算法生成 |
| create_time | DATETIME | 创建时间，插入时自动填充 |
| update_time | DATETIME | 更新时间，更新时自动填充 |
| deleted | TINYINT | 逻辑删除标记（0=正常, 1=已删除） |
