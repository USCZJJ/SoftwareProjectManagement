package com.pms.module.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.exception.BusinessException;
import com.pms.common.result.ResultCode;
import com.pms.module.task.entity.*;
import com.pms.module.task.mapper.*;
import com.pms.module.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final PmsKanbanBoardMapper kanbanBoardMapper;
    private final PmsKanbanColumnMapper kanbanColumnMapper;
    private final PmsTaskMapper taskMapper;
    private final PmsTaskLogMapper taskLogMapper;
    private final PmsWorklogMapper worklogMapper;
    private final PmsTaskReviewMapper taskReviewMapper;

    // ============================================================
    // Kanban Board
    // ============================================================

    @Override
    @Transactional
    public Map<String, Object> getKanbanBoard(Long projectId) {
        // Find or create the default board
        PmsKanbanBoard board = kanbanBoardMapper.selectOne(
                new LambdaQueryWrapper<PmsKanbanBoard>()
                        .eq(PmsKanbanBoard::getProjectId, projectId)
                        .eq(PmsKanbanBoard::getIsDefault, 1)
        );
        if (board == null) {
            board = createDefaultBoard(projectId);
        }

        // Get all columns for this board
        List<PmsKanbanColumn> columns = kanbanColumnMapper.selectList(
                new LambdaQueryWrapper<PmsKanbanColumn>()
                        .eq(PmsKanbanColumn::getBoardId, board.getId())
                        .orderByAsc(PmsKanbanColumn::getSortOrder)
        );

        // Get all tasks for the project (non-deleted)
        List<PmsTask> tasks = taskMapper.selectList(
                new LambdaQueryWrapper<PmsTask>()
                        .eq(PmsTask::getProjectId, projectId)
                        .orderByAsc(PmsTask::getSortOrder)
        );

        // Group tasks by kanbanColumnId
        Map<Long, List<PmsTask>> tasksByColumn = tasks.stream()
                .filter(t -> t.getKanbanColumnId() != null)
                .collect(Collectors.groupingBy(PmsTask::getKanbanColumnId));

        // Assign tasks to each column for API response
        List<Map<String, Object>> columnVos = columns.stream().map(col -> {
            Map<String, Object> colMap = new HashMap<>();
            colMap.put("id", col.getId());
            colMap.put("columnName", col.getColumnName());
            colMap.put("statusValue", col.getStatusValue());
            colMap.put("wipLimit", col.getWipLimit());
            colMap.put("sortOrder", col.getSortOrder());
            colMap.put("color", col.getColor());
            colMap.put("tasks", tasksByColumn.getOrDefault(col.getId(), Collections.emptyList()));
            return colMap;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("board", board);
        result.put("columns", columnVos);
        return result;
    }

    @Override
    public Map<String, Object> getKanbanTasks(Long projectId) {
        return getKanbanBoard(projectId);
    }

    private PmsKanbanBoard createDefaultBoard(Long projectId) {
        PmsKanbanBoard board = new PmsKanbanBoard();
        board.setProjectId(projectId);
        board.setBoardName("默认看板");
        board.setDescription("系统默认看板");
        board.setIsDefault(1);
        kanbanBoardMapper.insert(board);

        // Create default columns
        String[][] defaultColumns = {
                {"待办", "TODO", "#3498db"},
                {"进行中", "IN_PROGRESS", "#f39c12"},
                {"评审中", "IN_REVIEW", "#9b59b6"},
                {"已完成", "DONE", "#27ae60"}
        };

        for (int i = 0; i < defaultColumns.length; i++) {
            PmsKanbanColumn column = new PmsKanbanColumn();
            column.setBoardId(board.getId());
            column.setColumnName(defaultColumns[i][0]);
            column.setStatusValue(defaultColumns[i][1]);
            column.setSortOrder(i + 1);
            column.setColor(defaultColumns[i][2]);
            column.setWipLimit(0);
            kanbanColumnMapper.insert(column);
        }

        return board;
    }

    // ============================================================
    // Task CRUD
    // ============================================================

    @Override
    @Transactional
    public PmsTask createTask(PmsTask task, Long currentUserId) {
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("TODO");
        }
        if (task.getProgress() == null) {
            task.setProgress(0);
        }

        // Auto set sort order
        if (task.getSortOrder() == null) {
            Long count = taskMapper.selectCount(
                    new LambdaQueryWrapper<PmsTask>()
                            .eq(PmsTask::getProjectId, task.getProjectId())
                            .eq(task.getKanbanColumnId() != null, PmsTask::getKanbanColumnId, task.getKanbanColumnId())
            );
            task.setSortOrder(count.intValue() + 1);
        }

        taskMapper.insert(task);

        // Log task creation
        PmsTaskLog log = new PmsTaskLog();
        log.setTaskId(task.getId());
        log.setOperUserId(currentUserId);
        log.setAction("CREATE");
        log.setNewValue(task.getTitle());
        log.setRemark("创建任务");
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);

        return task;
    }

    @Override
    @Transactional
    public PmsTask updateTask(PmsTask task, Long currentUserId) {
        PmsTask existing = taskMapper.selectById(task.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }

        // Detect status change
        boolean statusChanged = task.getStatus() != null
                && !task.getStatus().equals(existing.getStatus());

        taskMapper.updateById(task);

        if (statusChanged) {
            PmsTaskLog log = new PmsTaskLog();
            log.setTaskId(task.getId());
            log.setOperUserId(currentUserId);
            log.setAction("STATUS_CHANGE");
            log.setOldValue(existing.getStatus());
            log.setNewValue(task.getStatus());
            log.setRemark("状态变更: " + existing.getStatus() + " -> " + task.getStatus());
            log.setCreateTime(LocalDateTime.now());
            taskLogMapper.insert(log);
        } else {
            PmsTaskLog log = new PmsTaskLog();
            log.setTaskId(task.getId());
            log.setOperUserId(currentUserId);
            log.setAction("UPDATE");
            log.setRemark("更新任务信息");
            log.setCreateTime(LocalDateTime.now());
            taskLogMapper.insert(log);
        }

        return taskMapper.selectById(task.getId());
    }

    @Override
    @Transactional
    public void moveTask(Long taskId, Long targetColumnId, Integer newOrder, Long currentUserId) {
        PmsTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }

        PmsKanbanColumn targetColumn = kanbanColumnMapper.selectById(targetColumnId);
        if (targetColumn == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "目标列不存在");
        }

        String oldStatus = task.getStatus();
        Long oldColumnId = task.getKanbanColumnId();

        // Update column and order
        task.setKanbanColumnId(targetColumnId);
        task.setSortOrder(newOrder);
        if (targetColumn.getStatusValue() != null && !targetColumn.getStatusValue().equals(task.getStatus())) {
            task.setStatus(targetColumn.getStatusValue());
        }
        taskMapper.updateById(task);

        // Log the move
        PmsTaskLog log = new PmsTaskLog();
        log.setTaskId(taskId);
        log.setOperUserId(currentUserId);
        log.setAction("STATUS_CHANGE");
        log.setOldValue(oldStatus);
        log.setNewValue(task.getStatus());
        log.setRemark("移动任务到列: " + targetColumn.getColumnName()
                + (oldColumnId != null ? " (从列ID:" + oldColumnId + ")" : ""));
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        PmsTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        taskMapper.deleteById(taskId);
    }

    @Override
    public PmsTask getTaskDetail(Long taskId) {
        PmsTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }
        return task;
    }

    // ============================================================
    // Progress
    // ============================================================

    @Override
    @Transactional
    public void updateProgress(Long taskId, Integer progress, BigDecimal remainingHours) {
        PmsTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }

        task.setProgress(progress);

        // Auto-calculate actualHours from plannedHours - remainingHours
        if (task.getPlannedHours() != null && remainingHours != null) {
            BigDecimal actual = task.getPlannedHours().subtract(remainingHours);
            if (actual.compareTo(BigDecimal.ZERO) < 0) {
                actual = BigDecimal.ZERO;
            }
            task.setActualHours(actual.setScale(2, RoundingMode.HALF_UP));
        }

        taskMapper.updateById(task);
    }

    // ============================================================
    // Assignment
    // ============================================================

    @Override
    @Transactional
    public void assignTask(Long taskId, Long assigneeId, Long currentUserId) {
        PmsTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }

        Long oldAssigneeId = task.getAssigneeId();
        task.setAssigneeId(assigneeId);
        taskMapper.updateById(task);

        PmsTaskLog log = new PmsTaskLog();
        log.setTaskId(taskId);
        log.setOperUserId(currentUserId);
        log.setAction("ASSIGN");
        log.setOldValue(oldAssigneeId != null ? oldAssigneeId.toString() : null);
        log.setNewValue(assigneeId != null ? assigneeId.toString() : null);
        log.setRemark("分配任务");
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);
    }

    // ============================================================
    // Task Listing
    // ============================================================

    @Override
    public Page<PmsTask> getTaskList(Long projectId, Long assigneeId, String status,
                                      String keyword, int page, int size) {
        LambdaQueryWrapper<PmsTask> wrapper = new LambdaQueryWrapper<PmsTask>()
                .eq(projectId != null, PmsTask::getProjectId, projectId)
                .eq(assigneeId != null, PmsTask::getAssigneeId, assigneeId)
                .eq(StringUtils.hasText(status), PmsTask::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(PmsTask::getTitle, keyword)
                        .or()
                        .like(PmsTask::getDescription, keyword))
                .orderByDesc(PmsTask::getCreateTime);

        Page<PmsTask> pageParam = new Page<>(page, size);
        return taskMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Page<PmsTask> getMyTasks(Long userId, int page, int size) {
        LambdaQueryWrapper<PmsTask> wrapper = new LambdaQueryWrapper<PmsTask>()
                .eq(PmsTask::getAssigneeId, userId)
                .orderByDesc(PmsTask::getCreateTime);

        Page<PmsTask> pageParam = new Page<>(page, size);
        return taskMapper.selectPage(pageParam, wrapper);
    }

    // ============================================================
    // Task Logs
    // ============================================================

    @Override
    public List<PmsTaskLog> getTaskLogs(Long taskId) {
        return taskLogMapper.selectList(
                new LambdaQueryWrapper<PmsTaskLog>()
                        .eq(PmsTaskLog::getTaskId, taskId)
                        .orderByDesc(PmsTaskLog::getCreateTime)
        );
    }

    // ============================================================
    // Worklog
    // ============================================================

    @Override
    @Transactional
    public void submitWorklog(PmsWorklog worklog, Long currentUserId) {
        if (worklog.getHours() == null || worklog.getHours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "工时数必须大于0");
        }
        if (worklog.getHours().compareTo(new BigDecimal("16")) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "单日工时不能超过16小时");
        }
        if (worklog.getWorkDate() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "工作日期不能为空");
        }

        // Check that total hours for the day does not exceed 16
        BigDecimal existingHours = worklogMapper.selectList(
                new LambdaQueryWrapper<PmsWorklog>()
                        .eq(PmsWorklog::getUserId, currentUserId)
                        .eq(PmsWorklog::getWorkDate, worklog.getWorkDate())
        ).stream()
                .map(PmsWorklog::getHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalHours = existingHours.add(worklog.getHours());
        if (totalHours.compareTo(new BigDecimal("16")) > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "当日累计工时(" + totalHours + "小时)超过16小时限制");
        }

        worklog.setUserId(currentUserId);
        worklogMapper.insert(worklog);
    }

    @Override
    public List<PmsWorklog> getWorklogs(Long projectId, Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<PmsWorklog> wrapper = new LambdaQueryWrapper<PmsWorklog>()
                .eq(projectId != null, PmsWorklog::getProjectId, projectId)
                .eq(userId != null, PmsWorklog::getUserId, userId)
                .orderByDesc(PmsWorklog::getWorkDate)
                .orderByDesc(PmsWorklog::getCreateTime);

        if (StringUtils.hasText(startDate)) {
            wrapper.ge(PmsWorklog::getWorkDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(PmsWorklog::getWorkDate, LocalDate.parse(endDate));
        }

        return worklogMapper.selectList(wrapper);
    }

    @Override
    public List<PmsWorklog> getMyWorklogs(Long userId, String startDate, String endDate) {
        return getWorklogs(null, userId, startDate, endDate);
    }

    // ============================================================
    // Task Review
    // ============================================================

    @Override
    @Transactional
    public void reviewTask(PmsTaskReview review, Long currentUserId) {
        PmsTask task = taskMapper.selectById(review.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "任务不存在");
        }

        review.setReviewerId(currentUserId);
        review.setCreateTime(LocalDateTime.now());

        // Calculate score (default 0-100 scale, or use provided score)
        if (review.getScore() == null) {
            // If no explicit score, default: PASS=85, REJECT=30
            review.setScore("PASS".equals(review.getResult())
                    ? new BigDecimal("85")
                    : new BigDecimal("30"));
        }

        taskReviewMapper.insert(review);

        // Update task status based on review result
        if ("PASS".equalsIgnoreCase(review.getResult())) {
            task.setStatus("DONE");
        } else if ("REJECT".equalsIgnoreCase(review.getResult())) {
            task.setStatus("IN_PROGRESS");
        }
        taskMapper.updateById(task);

        // Log the review
        PmsTaskLog log = new PmsTaskLog();
        log.setTaskId(review.getTaskId());
        log.setOperUserId(currentUserId);
        log.setAction("STATUS_CHANGE");
        log.setOldValue("IN_REVIEW");
        log.setNewValue(task.getStatus());
        log.setRemark("评审结果: " + review.getResult()
                + (review.getRejectReason() != null ? ", 原因: " + review.getRejectReason() : ""));
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);
    }

    @Override
    public List<PmsTaskReview> getTaskReviews(Long taskId) {
        return taskReviewMapper.selectList(
                new LambdaQueryWrapper<PmsTaskReview>()
                        .eq(PmsTaskReview::getTaskId, taskId)
                        .orderByDesc(PmsTaskReview::getCreateTime)
        );
    }
}
