package com.pms.module.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pms.common.result.R;
import com.pms.module.project.entity.PmsMilestone;
import com.pms.module.project.entity.PmsPlanChange;
import com.pms.module.project.entity.PmsProject;
import com.pms.module.project.entity.PmsProjectMember;
import com.pms.module.project.entity.PmsTaskDependency;
import com.pms.module.project.entity.PmsWbs;
import com.pms.module.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "项目管理")
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "获取项目列表")
    @GetMapping("/list")
    public R<IPage<PmsProject>> list(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "项目状态") @RequestParam(required = false) String status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        return R.ok(projectService.getProjectList(keyword, status, page, size));
    }

    @Operation(summary = "获取项目详情")
    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@Parameter(description = "项目ID") @PathVariable Long id) {
        return R.ok(projectService.getProjectDetail(id));
    }

    @Operation(summary = "创建项目")
    @PostMapping
    public R<PmsProject> create(@RequestBody PmsProject project) {
        return R.ok(projectService.createProject(project));
    }

    @Operation(summary = "更新项目")
    @PutMapping("/{id}")
    public R<PmsProject> update(
            @Parameter(description = "项目ID") @PathVariable Long id,
            @RequestBody PmsProject project) {
        project.setId(id);
        return R.ok(projectService.updateProject(project));
    }

    @Operation(summary = "删除项目")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "项目ID") @PathVariable Long id) {
        projectService.deleteProject(id);
        return R.ok();
    }

    @Operation(summary = "获取WBS树")
    @GetMapping("/{projectId}/wbs")
    public R<List<PmsWbs>> getWbsTree(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        return R.ok(projectService.getWbsTree(projectId));
    }

    @Operation(summary = "保存WBS节点")
    @PostMapping("/{projectId}/wbs")
    public R<PmsWbs> saveWbs(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody PmsWbs wbs) {
        wbs.setProjectId(projectId);
        return R.ok(projectService.saveWbs(wbs));
    }

    @Operation(summary = "更新WBS进度")
    @PutMapping("/{projectId}/wbs/{wbsId}/progress")
    public R<Void> updateWbsProgress(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "WBS节点ID") @PathVariable Long wbsId,
            @Parameter(description = "进度(0-100)") @RequestParam Integer progress) {
        projectService.updateWbsProgress(wbsId, progress);
        return R.ok();
    }

    @Operation(summary = "删除WBS节点")
    @DeleteMapping("/{projectId}/wbs/{wbsId}")
    public R<Void> deleteWbs(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "WBS节点ID") @PathVariable Long wbsId) {
        projectService.deleteWbs(wbsId);
        return R.ok();
    }

    @Operation(summary = "获取里程碑列表")
    @GetMapping("/{projectId}/milestones")
    public R<List<PmsMilestone>> getMilestones(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        return R.ok(projectService.getMilestones(projectId));
    }

    @Operation(summary = "保存里程碑")
    @PostMapping("/{projectId}/milestone")
    public R<PmsMilestone> saveMilestone(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody PmsMilestone milestone) {
        milestone.setProjectId(projectId);
        return R.ok(projectService.saveMilestone(milestone));
    }

    @Operation(summary = "获取任务依赖列表")
    @GetMapping("/{projectId}/dependencies")
    public R<List<PmsTaskDependency>> getDependencies(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        return R.ok(projectService.getDependencies(projectId));
    }

    @Operation(summary = "保存任务依赖")
    @PostMapping("/{projectId}/dependency")
    public R<PmsTaskDependency> saveDependency(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody PmsTaskDependency dependency) {
        dependency.setProjectId(projectId);
        return R.ok(projectService.saveDependency(dependency));
    }

    @Operation(summary = "删除任务依赖")
    @DeleteMapping("/{projectId}/dependency/{id}")
    public R<Void> deleteDependency(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "依赖关系ID") @PathVariable Long id) {
        projectService.deleteDependency(id);
        return R.ok();
    }

    @Operation(summary = "计算关键路径")
    @GetMapping("/{projectId}/critical-path")
    public R<List<Long>> calculateCriticalPath(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        return R.ok(projectService.calculateCriticalPath(projectId));
    }

    @Operation(summary = "获取计划变更列表")
    @GetMapping("/{projectId}/plan-changes")
    public R<List<PmsPlanChange>> getPlanChanges(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        return R.ok(projectService.getPlanChanges(projectId));
    }

    @Operation(summary = "提交计划变更")
    @PostMapping("/{projectId}/plan-change")
    public R<PmsPlanChange> submitPlanChange(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @RequestBody PmsPlanChange change) {
        change.setProjectId(projectId);
        return R.ok(projectService.submitPlanChange(change));
    }

    @Operation(summary = "审批计划变更")
    @PutMapping("/{projectId}/plan-change/{id}/approve")
    public R<Void> approvePlanChange(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "变更ID") @PathVariable Long id,
            @Parameter(description = "是否通过") @RequestParam boolean approved,
            @Parameter(description = "审批意见") @RequestParam(required = false) String comment) {
        projectService.approvePlanChange(id, approved, comment);
        return R.ok();
    }

    @Operation(summary = "获取项目成员")
    @GetMapping("/{projectId}/members")
    public R<List<PmsProjectMember>> getMembers(@Parameter(description = "项目ID") @PathVariable Long projectId) {
        return R.ok(projectService.getMembers(projectId));
    }

    @Operation(summary = "添加项目成员")
    @PostMapping("/{projectId}/member")
    public R<Void> addMember(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色") @RequestParam String role) {
        projectService.addMember(projectId, userId, role);
        return R.ok();
    }

    @Operation(summary = "移除项目成员")
    @DeleteMapping("/{projectId}/member/{userId}")
    public R<Void> removeMember(
            @Parameter(description = "项目ID") @PathVariable Long projectId,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        projectService.removeMember(projectId, userId);
        return R.ok();
    }
}
