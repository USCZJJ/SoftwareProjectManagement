package com.pms.module.task.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.exception.BusinessException;
import com.pms.common.result.R;
import com.pms.common.result.ResultCode;
import com.pms.module.task.entity.*;
import com.pms.module.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "任务管理")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ============================================================
    // Utility
    // ============================================================

    private Long getCurrentUserId(HttpServletRequest request) {
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return (Long) userIdAttr;
    }

    // ============================================================
    // Kanban
    // ============================================================

    @Operation(summary = "获取项目看板（含任务）")
    @GetMapping("/kanban/{projectId}")
    public R<Map<String, Object>> getKanbanBoard(@PathVariable Long projectId) {
        return R.ok(taskService.getKanbanBoard(projectId));
    }

    // ============================================================
    // Task CRUD
    // ============================================================

    @Operation(summary = "创建任务")
    @PostMapping
    public R<PmsTask> createTask(@RequestBody PmsTask task, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return R.ok(taskService.createTask(task, userId));
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public R<PmsTask> updateTask(@PathVariable Long id, @RequestBody PmsTask task, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        task.setId(id);
        return R.ok(taskService.updateTask(task, userId));
    }

    @Operation(summary = "移动任务")
    @PutMapping("/{id}/move")
    public R<Void> moveTask(@PathVariable Long id,
                             @Parameter(description = "目标列ID") @RequestParam Long targetColumnId,
                             @Parameter(description = "新排序位置") @RequestParam Integer newOrder,
                             HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        taskService.moveTask(id, targetColumnId, newOrder, userId);
        return R.ok();
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public R<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return R.ok();
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public R<PmsTask> getTaskDetail(@PathVariable Long id) {
        return R.ok(taskService.getTaskDetail(id));
    }

    // ============================================================
    // Progress & Assignment
    // ============================================================

    @Operation(summary = "更新任务进度")
    @PutMapping("/{id}/progress")
    public R<Void> updateProgress(@PathVariable Long id,
                                   @Parameter(description = "进度 0-100") @RequestParam Integer progress,
                                   @Parameter(description = "剩余工时") @RequestParam(required = false) java.math.BigDecimal remainingHours) {
        taskService.updateProgress(id, progress, remainingHours);
        return R.ok();
    }

    @Operation(summary = "分配任务")
    @PutMapping("/{id}/assign")
    public R<Void> assignTask(@PathVariable Long id,
                               @Parameter(description = "负责人ID") @RequestParam Long assigneeId,
                               HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        taskService.assignTask(id, assigneeId, userId);
        return R.ok();
    }

    // ============================================================
    // Task Lists
    // ============================================================

    @Operation(summary = "任务列表（分页查询）")
    @GetMapping("/list")
    public R<Page<PmsTask>> getTaskList(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "负责人ID") @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        return R.ok(taskService.getTaskList(projectId, assigneeId, status, keyword, page, size));
    }

    @Operation(summary = "获取当前用户的任务")
    @GetMapping("/my")
    public R<Page<PmsTask>> getMyTasks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return R.ok(taskService.getMyTasks(userId, page, size));
    }

    // ============================================================
    // Task Logs
    // ============================================================

    @Operation(summary = "获取任务操作日志")
    @GetMapping("/{taskId}/logs")
    public R<List<PmsTaskLog>> getTaskLogs(@PathVariable Long taskId) {
        return R.ok(taskService.getTaskLogs(taskId));
    }

    // ============================================================
    // Worklog
    // ============================================================

    @Operation(summary = "提交工时记录")
    @PostMapping("/worklog")
    public R<Void> submitWorklog(@RequestBody PmsWorklog worklog, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        taskService.submitWorklog(worklog, userId);
        return R.ok();
    }

    @Operation(summary = "查询工时记录列表")
    @GetMapping("/worklog/list")
    public R<List<PmsWorklog>> getWorklogs(
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return R.ok(taskService.getWorklogs(projectId, userId, startDate, endDate));
    }

    @Operation(summary = "获取当前用户工时记录")
    @GetMapping("/worklog/my")
    public R<List<PmsWorklog>> getMyWorklogs(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return R.ok(taskService.getMyWorklogs(userId, startDate, endDate));
    }

    // ============================================================
    // Task Review
    // ============================================================

    @Operation(summary = "提交任务评审")
    @PostMapping("/{taskId}/review")
    public R<Void> reviewTask(@PathVariable Long taskId,
                               @RequestBody PmsTaskReview review,
                               HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        review.setTaskId(taskId);
        taskService.reviewTask(review, userId);
        return R.ok();
    }

    @Operation(summary = "获取任务评审记录")
    @GetMapping("/{taskId}/reviews")
    public R<List<PmsTaskReview>> getTaskReviews(@PathVariable Long taskId) {
        return R.ok(taskService.getTaskReviews(taskId));
    }
}
