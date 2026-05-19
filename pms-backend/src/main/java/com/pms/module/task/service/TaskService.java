package com.pms.module.task.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.module.task.entity.*;

import java.util.List;
import java.util.Map;

public interface TaskService {

    /**
     * Get or create default kanban board with columns for a project.
     * Returns board with its columns and tasks grouped under each column.
     */
    Map<String, Object> getKanbanBoard(Long projectId);

    /**
     * Return tasks grouped by kanban column for the project.
     */
    Map<String, Object> getKanbanTasks(Long projectId);

    /**
     * Create a new task with auto-generated sort order and creation log.
     */
    PmsTask createTask(PmsTask task, Long currentUserId);

    /**
     * Update task, detect status change and log if needed.
     */
    PmsTask updateTask(PmsTask task, Long currentUserId);

    /**
     * Move task to a target column and reorder within that column.
     */
    void moveTask(Long taskId, Long targetColumnId, Integer newOrder, Long currentUserId);

    /**
     * Soft delete a task by ID.
     */
    void deleteTask(Long taskId);

    /**
     * Get task detail by ID.
     */
    PmsTask getTaskDetail(Long taskId);

    /**
     * Update task progress and auto-calculate actual hours.
     */
    void updateProgress(Long taskId, Integer progress, java.math.BigDecimal remainingHours);

    /**
     * Assign task to a user.
     */
    void assignTask(Long taskId, Long assigneeId, Long currentUserId);

    /**
     * Paginated task list query.
     */
    Page<PmsTask> getTaskList(Long projectId, Long assigneeId, String status, String keyword, int page, int size);

    /**
     * Get current user's tasks with pagination.
     */
    Page<PmsTask> getMyTasks(Long userId, int page, int size);

    /**
     * Get all logs for a specific task.
     */
    List<PmsTaskLog> getTaskLogs(Long taskId);

    /**
     * Submit a worklog entry. Validates max 16 hours/day and no overlapping entries.
     */
    void submitWorklog(PmsWorklog worklog, Long currentUserId);

    /**
     * Query worklog list with filters.
     */
    List<PmsWorklog> getWorklogs(Long projectId, Long userId, String startDate, String endDate);

    /**
     * Get current user's worklogs in a date range.
     */
    List<PmsWorklog> getMyWorklogs(Long userId, String startDate, String endDate);

    /**
     * Submit a task review, calculate score, update task status accordingly.
     */
    void reviewTask(PmsTaskReview review, Long currentUserId);

    /**
     * Get all reviews for a specific task.
     */
    List<PmsTaskReview> getTaskReviews(Long taskId);
}
