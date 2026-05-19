-- PMS 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS pms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pms;

-- ==================== 用户与权限 ====================
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `department` VARCHAR(100) COMMENT '部门',
    `position` VARCHAR(50) COMMENT '职位',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1启用 0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL,
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `sys_permission` (
    `id` BIGINT NOT NULL,
    `perm_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `perm_code` VARCHAR(50) NOT NULL COMMENT '权限编码',
    `perm_type` VARCHAR(20) COMMENT '类型 menu/button/api',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父级权限ID',
    `path` VARCHAR(200) COMMENT '路由或接口路径',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

CREATE TABLE `sys_role_permission` (
    `role_id` BIGINT NOT NULL,
    `perm_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- ==================== 项目计划 ====================
CREATE TABLE `pms_project` (
    `id` BIGINT NOT NULL COMMENT '项目ID',
    `project_code` VARCHAR(30) NOT NULL COMMENT '项目编号(PROJ-YYYY-NNN)',
    `project_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
    `description` TEXT COMMENT '项目描述',
    `objectives` TEXT COMMENT '项目目标',
    `start_date` DATE COMMENT '开始日期',
    `end_date` DATE COMMENT '结束日期',
    `budget` DECIMAL(15,2) COMMENT '预算',
    `actual_cost` DECIMAL(15,2) DEFAULT 0 COMMENT '实际成本',
    `status` VARCHAR(20) DEFAULT 'INIT' COMMENT '状态 INIT/PLANNING/EXECUTING/MONITORING/CLOSED',
    `manager_id` BIGINT COMMENT '项目经理ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_code` (`project_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_manager` (`manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

CREATE TABLE `pms_project_member` (
    `project_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `role` VARCHAR(30) COMMENT '项目角色 PM/DEV/QA/BA',
    `join_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`project_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员';

CREATE TABLE `pms_wbs` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL COMMENT '所属项目',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父节点ID',
    `wbs_code` VARCHAR(30) COMMENT 'WBS编码',
    `node_name` VARCHAR(200) NOT NULL COMMENT '节点名称',
    `description` TEXT COMMENT '详细描述',
    `assignee_id` BIGINT COMMENT '负责人',
    `planned_start` DATE COMMENT '计划开始',
    `planned_end` DATE COMMENT '计划结束',
    `actual_start` DATE COMMENT '实际开始',
    `actual_end` DATE COMMENT '实际结束',
    `planned_hours` DECIMAL(10,1) COMMENT '计划工时',
    `actual_hours` DECIMAL(10,1) COMMENT '实际工时',
    `progress` INT DEFAULT 0 COMMENT '进度百分比0-100',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_milestone` TINYINT DEFAULT 0 COMMENT '是否为里程碑',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WBS工作分解结构';

CREATE TABLE `pms_milestone` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `wbs_id` BIGINT COMMENT '关联WBS节点',
    `milestone_name` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `planned_date` DATE NOT NULL COMMENT '计划完成日期',
    `actual_date` DATE COMMENT '实际完成日期',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/ACHIEVED/DELAYED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='里程碑';

CREATE TABLE `pms_task_dependency` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `predecessor_wbs_id` BIGINT NOT NULL COMMENT '前置任务WBS ID',
    `successor_wbs_id` BIGINT NOT NULL COMMENT '后置任务WBS ID',
    `dependency_type` VARCHAR(2) DEFAULT 'FS' COMMENT 'FS/SS/FF/SF',
    `lag_days` INT DEFAULT 0 COMMENT '滞后天数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`),
    UNIQUE KEY `uk_dep` (`predecessor_wbs_id`, `successor_wbs_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务依赖关系';

CREATE TABLE `pms_plan_change` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `change_type` VARCHAR(30) COMMENT '变更类型 WBS/SCHEDULE/MILESTONE',
    `change_desc` TEXT NOT NULL COMMENT '变更描述',
    `reason` TEXT COMMENT '原因',
    `impact_analysis` TEXT COMMENT '影响分析',
    `affected_items` TEXT COMMENT '影响范围(JSON)',
    `apply_user_id` BIGINT COMMENT '申请人',
    `approve_user_id` BIGINT COMMENT '审批人',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `approve_comment` TEXT COMMENT '审批意见',
    `version_snapshot` TEXT COMMENT '变更前版本快照(JSON)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划变更记录';

-- ==================== 需求管理 ====================
CREATE TABLE `pms_requirement` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `req_code` VARCHAR(30) COMMENT '需求编号(REQ-PROJID-NNN)',
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `type` VARCHAR(20) COMMENT 'FUNCTIONAL/NON_FUNCTIONAL/UI',
    `priority` VARCHAR(10) DEFAULT 'COULD' COMMENT 'MoSCoW: MUST/SHOULD/COULD/WONT',
    `severity` INT COMMENT '重要度1-5',
    `source` VARCHAR(50) COMMENT '需求来源(客户/内部/市场)',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/SUBMITTED/REVIEWED/APPROVED/REJECTED/CHANGED',
    `submit_user_id` BIGINT COMMENT '提交人',
    `review_user_id` BIGINT COMMENT '评审人',
    `assignee_id` BIGINT COMMENT '负责人',
    `version` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求记录';

CREATE TABLE `pms_req_trace` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `req_id` BIGINT NOT NULL COMMENT '需求ID',
    `task_id` BIGINT COMMENT '关联任务ID',
    `defect_id` BIGINT COMMENT '关联缺陷ID',
    `test_case_id` BIGINT COMMENT '关联测试用例ID',
    `trace_note` VARCHAR(200),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_req` (`req_id`),
    INDEX `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求跟踪矩阵RTM';

CREATE TABLE `pms_req_change` (
    `id` BIGINT NOT NULL,
    `req_id` BIGINT NOT NULL,
    `change_desc` TEXT NOT NULL,
    `reason` TEXT,
    `impact` TEXT COMMENT '影响分析',
    `affected_reqs` TEXT COMMENT '受影响的关联需求',
    `apply_user_id` BIGINT,
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    `approve_user_id` BIGINT,
    `approve_comment` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_req` (`req_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求变更记录';

-- ==================== 任务管理 ====================
CREATE TABLE `pms_task` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `wbs_id` BIGINT COMMENT '关联WBS',
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `type` VARCHAR(20) DEFAULT 'TASK' COMMENT 'TASK/BUG/FEATURE/IMPROVEMENT',
    `priority` VARCHAR(10) DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH/URGENT',
    `status` VARCHAR(20) DEFAULT 'TODO' COMMENT 'TODO/IN_PROGRESS/IN_REVIEW/DONE/CLOSED',
    `assignee_id` BIGINT COMMENT '执行人',
    `reporter_id` BIGINT COMMENT '创建人',
    `kanban_column_id` BIGINT COMMENT '看板列ID',
    `planned_hours` DECIMAL(10,1) COMMENT '计划工时',
    `actual_hours` DECIMAL(10,1) DEFAULT 0 COMMENT '实际工时',
    `progress` INT DEFAULT 0 COMMENT '进度0-100',
    `start_date` DATE COMMENT '开始日期',
    `due_date` DATE COMMENT '截止日期',
    `sort_order` INT DEFAULT 0 COMMENT '在看板列中的排序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_assignee` (`assignee_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_kanban_column` (`kanban_column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

CREATE TABLE `pms_task_log` (
    `id` BIGINT NOT NULL,
    `task_id` BIGINT NOT NULL,
    `oper_user_id` BIGINT,
    `action` VARCHAR(30) COMMENT 'CREATE/STATUS_CHANGE/ASSIGN/UPDATE/DELETE',
    `old_value` VARCHAR(500),
    `new_value` VARCHAR(500),
    `remark` VARCHAR(500),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务操作日志';

CREATE TABLE `pms_worklog` (
    `id` BIGINT NOT NULL,
    `task_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `work_date` DATE NOT NULL COMMENT '工作日期',
    `hours` DECIMAL(5,2) NOT NULL COMMENT '工时(小时)',
    `content` TEXT COMMENT '工作内容',
    `plan_detail` TEXT COMMENT '明日计划',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_user_date` (`user_id`, `work_date`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时登记';

CREATE TABLE `pms_kanban_board` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `board_name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500),
    `is_default` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板';

CREATE TABLE `pms_kanban_column` (
    `id` BIGINT NOT NULL,
    `board_id` BIGINT NOT NULL,
    `column_name` VARCHAR(50) NOT NULL COMMENT '列名称',
    `status_value` VARCHAR(20) NOT NULL COMMENT '对应的任务状态 TODO/IN_PROGRESS/IN_REVIEW/DONE',
    `wip_limit` INT DEFAULT 0 COMMENT 'WIP限制(0=不限制)',
    `sort_order` INT DEFAULT 0,
    `color` VARCHAR(20) COMMENT '列颜色',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_board` (`board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='看板列';

CREATE TABLE `pms_task_review` (
    `id` BIGINT NOT NULL,
    `task_id` BIGINT NOT NULL,
    `reviewer_id` BIGINT COMMENT '评审人',
    `result` VARCHAR(20) COMMENT 'PASS/REJECT',
    `score` INT COMMENT '评分1-10',
    `comment` TEXT COMMENT '评审意见',
    `reject_reason` TEXT COMMENT '退回原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务质量评审';

-- ==================== 缺陷管理 ====================
CREATE TABLE `pms_defect` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `defect_code` VARCHAR(30) COMMENT '缺陷编号(DEF-PROJID-NNN)',
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `steps_to_reproduce` TEXT COMMENT '重现步骤',
    `expected_result` TEXT COMMENT '期望结果',
    `actual_result` TEXT COMMENT '实际结果',
    `severity` VARCHAR(20) DEFAULT 'MINOR' COMMENT 'BLOCKER/CRITICAL/MAJOR/MINOR/TRIVIAL',
    `priority` VARCHAR(10) DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH/URGENT',
    `status` VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN/ASSIGNED/FIXING/FIXED/VERIFYING/VERIFIED/CLOSED/REOPENED',
    `module` VARCHAR(100) COMMENT '所属模块',
    `version_found` VARCHAR(30) COMMENT '发现版本',
    `version_fixed` VARCHAR(30) COMMENT '修复版本',
    `reporter_id` BIGINT COMMENT '报告人',
    `assignee_id` BIGINT COMMENT '修复人',
    `screenshot_urls` TEXT COMMENT '截图(JSON数组)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_assignee` (`assignee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缺陷记录';

CREATE TABLE `pms_test_case` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `case_code` VARCHAR(30),
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `precondition` TEXT COMMENT '前置条件',
    `test_steps` TEXT COMMENT '测试步骤(JSON)',
    `expected_result` TEXT COMMENT '期望结果',
    `type` VARCHAR(20) DEFAULT 'FUNCTIONAL' COMMENT 'FUNCTIONAL/PERFORMANCE/SECURITY',
    `priority` VARCHAR(10) DEFAULT 'MEDIUM',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT/ACTIVE/DEPRECATED',
    `suite_id` BIGINT COMMENT '测试套件ID',
    `create_user_id` BIGINT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`),
    INDEX `idx_suite` (`suite_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例';

CREATE TABLE `pms_test_execution` (
    `id` BIGINT NOT NULL,
    `test_case_id` BIGINT NOT NULL,
    `executor_id` BIGINT,
    `result` VARCHAR(20) COMMENT 'PASS/FAIL/BLOCKED/SKIP',
    `actual_result` TEXT,
    `remark` VARCHAR(500),
    `defect_id` BIGINT COMMENT '执行失败关联的缺陷ID',
    `exec_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_case` (`test_case_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试执行记录';

CREATE TABLE `pms_quality_checklist` (
    `id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `stage` VARCHAR(30) COMMENT '阶段: DESIGN/CODING/TESTING/DEPLOY',
    `check_item` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `weight` INT DEFAULT 1 COMMENT '权重',
    `is_required` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检查单';

CREATE TABLE `pms_quality_check_result` (
    `id` BIGINT NOT NULL,
    `checklist_id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `result` VARCHAR(10) COMMENT 'PASS/FAIL/NA',
    `remark` VARCHAR(500),
    `check_user_id` BIGINT,
    `check_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检查结果';

-- ==================== 通用功能 ====================
CREATE TABLE `pms_announcement` (
    `id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `type` VARCHAR(20) DEFAULT 'INFO' COMMENT 'INFO/WARNING/URGENT',
    `scope` VARCHAR(20) DEFAULT 'ALL' COMMENT 'ALL/PROJECT',
    `project_id` BIGINT COMMENT '项目范围',
    `publisher_id` BIGINT COMMENT '发布人',
    `is_mandatory` TINYINT DEFAULT 0 COMMENT '是否强制阅读',
    `end_date` DATE COMMENT '公告截止日期',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_scope` (`scope`, `project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告通知';

CREATE TABLE `pms_announcement_read` (
    `announcement_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `is_read` TINYINT DEFAULT 0,
    `read_time` DATETIME,
    PRIMARY KEY (`announcement_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告已读记录';

CREATE TABLE `pms_message` (
    `id` BIGINT NOT NULL,
    `receiver_id` BIGINT NOT NULL COMMENT '接收人',
    `sender_id` BIGINT COMMENT '发送人(0=系统)',
    `title` VARCHAR(200),
    `content` TEXT,
    `type` VARCHAR(20) COMMENT 'ASSIGN/DEFECT/REVIEW/ANNOUNCEMENT/MENTION',
    `related_id` BIGINT COMMENT '关联业务ID',
    `is_read` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_receiver` (`receiver_id`, `is_read`),
    INDEX `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心';

CREATE TABLE `pms_work_log` (
    `id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `project_id` BIGINT,
    `work_date` DATE NOT NULL COMMENT '工作日期',
    `content` TEXT NOT NULL COMMENT '工作内容',
    `plan` TEXT COMMENT '明日计划',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_date` (`user_id`, `work_date`),
    INDEX `idx_project_date` (`project_id`, `work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作日志';

-- ==================== 初始数据 ====================
-- 默认管理员 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `department`, `position`, `status`) VALUES
(1, 'admin', '$2a$10$n3u/Qpbwv3nQk7i4n8V1xu47mxwokXiKAFAXRfxNfk4.de95kIY5W', '系统管理员', 'admin@pms.com', '技术部', '系统管理员', 1),
(2, 'pm', '$2a$10$n3u/Qpbwv3nQk7i4n8V1xu47mxwokXiKAFAXRfxNfk4.de95kIY5W', '项目经理', 'pm@pms.com', '项目部', '项目经理', 1),
(3, 'dev', '$2a$10$n3u/Qpbwv3nQk7i4n8V1xu47mxwokXiKAFAXRfxNfk4.de95kIY5W', '开发工程师', 'dev@pms.com', '开发部', '高级开发', 1),
(4, 'qa', '$2a$10$n3u/Qpbwv3nQk7i4n8V1xu47mxwokXiKAFAXRfxNfk4.de95kIY5W', '测试工程师', 'qa@pms.com', '测试部', '测试工程师', 1);

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '系统管理员', 'ADMIN', '系统最高权限'),
(2, '项目经理', 'PM', '项目管理权限'),
(3, '开发工程师', 'DEV', '任务执行与缺陷修复'),
(4, '测试工程师', 'QA', '缺陷报告与测试验证'),
(5, '业务分析', 'BA', '需求管理与文档');

-- 默认看板列模板
INSERT INTO `pms_kanban_board` (`id`, `project_id`, `board_name`, `is_default`) VALUES
(1, 0, '默认看板', 1);

INSERT INTO `pms_kanban_column` (`id`, `board_id`, `column_name`, `status_value`, `wip_limit`, `sort_order`, `color`) VALUES
(1, 1, '待处理', 'TODO', 0, 0, '#909399'),
(2, 1, '进行中', 'IN_PROGRESS', 5, 1, '#409EFF'),
(3, 1, '评审中', 'IN_REVIEW', 3, 2, '#E6A23C'),
(4, 1, '已完成', 'DONE', 0, 3, '#67C23A'),
(5, 1, '已关闭', 'CLOSED', 0, 4, '#F56C6C');

-- 质量检查单模板
INSERT INTO `pms_quality_checklist` (`id`, `project_id`, `stage`, `check_item`, `weight`, `is_required`) VALUES
(1, 0, 'DESIGN', '需求文档是否经过评审', 3, 1),
(2, 0, 'DESIGN', '架构设计是否通过技术评审', 3, 1),
(3, 0, 'CODING', '代码是否通过Code Review', 2, 1),
(4, 0, 'CODING', '单元测试覆盖率是否达标(>=70%)', 2, 1),
(5, 0, 'TESTING', '集成测试是否全部通过', 3, 1),
(6, 0, 'TESTING', '性能测试指标是否达标', 2, 0),
(7, 0, 'TESTING', '安全漏洞扫描是否通过', 2, 1),
(8, 0, 'DEPLOY', '部署回滚方案是否就绪', 2, 0),
(9, 0, 'DEPLOY', '生产环境冒烟测试是否通过', 3, 1);
