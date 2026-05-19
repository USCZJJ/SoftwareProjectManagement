package com.pms.module.defect.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pms.common.result.R;
import com.pms.module.defect.entity.*;
import com.pms.module.defect.service.DefectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/defect")
@Tag(name = "缺陷管理")
@RequiredArgsConstructor
public class DefectController {

    private final DefectService defectService;

    // ============================================================
    // Defects
    // ============================================================

    @GetMapping("/list")
    @Operation(summary = "缺陷列表查询")
    public R<IPage<PmsDefect>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<PmsDefect> result = defectService.getList(projectId, keyword, status, severity, assigneeId, page, size);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "缺陷详情")
    public R<PmsDefect> detail(@PathVariable Long id) {
        PmsDefect defect = defectService.getById(id);
        return R.ok(defect);
    }

    @PostMapping
    @Operation(summary = "创建缺陷")
    public R<PmsDefect> create(@RequestBody PmsDefect defect, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PmsDefect created = defectService.create(defect, userId);
        return R.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新缺陷")
    public R<PmsDefect> update(@PathVariable Long id, @RequestBody PmsDefect defect) {
        defect.setId(id);
        PmsDefect updated = defectService.update(defect);
        return R.ok(updated);
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "分配缺陷")
    public R<Void> assign(@PathVariable Long id, @RequestBody AssignRequest body) {
        defectService.assign(id, body.getAssigneeId());
        return R.ok();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新缺陷状态")
    public R<Void> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        defectService.updateStatus(id, body.getStatus(), userId);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除缺陷")
    public R<Void> delete(@PathVariable Long id) {
        defectService.delete(id);
        return R.ok();
    }

    // ============================================================
    // Test Cases
    // ============================================================

    @GetMapping("/testcase/list")
    @Operation(summary = "测试用例列表")
    public R<IPage<PmsTestCase>> caseList(
            @RequestParam(required = false) Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<PmsTestCase> result = defectService.getCaseList(projectId, page, size);
        return R.ok(result);
    }

    @PostMapping("/testcase")
    @Operation(summary = "创建测试用例")
    public R<PmsTestCase> createCase(@RequestBody PmsTestCase tc) {
        PmsTestCase created = defectService.createCase(tc);
        return R.ok(created);
    }

    @PutMapping("/testcase/{id}")
    @Operation(summary = "更新测试用例")
    public R<PmsTestCase> updateCase(@PathVariable Long id, @RequestBody PmsTestCase tc) {
        tc.setId(id);
        PmsTestCase updated = defectService.updateCase(tc);
        return R.ok(updated);
    }

    @DeleteMapping("/testcase/{id}")
    @Operation(summary = "删除测试用例")
    public R<Void> deleteCase(@PathVariable Long id) {
        defectService.deleteCase(id);
        return R.ok();
    }

    @PostMapping("/testcase/execute")
    @Operation(summary = "执行测试用例")
    public R<PmsTestExecution> executeTest(@RequestBody PmsTestExecution exec) {
        PmsTestExecution result = defectService.executeTest(exec);
        return R.ok(result);
    }

    @GetMapping("/testcase/{id}/executions")
    @Operation(summary = "测试执行历史")
    public R<List<PmsTestExecution>> getExecutions(@PathVariable Long id) {
        List<PmsTestExecution> executions = defectService.getExecutions(id);
        return R.ok(executions);
    }

    // ============================================================
    // Quality Checklist
    // ============================================================

    @GetMapping("/checklist")
    @Operation(summary = "质量检查清单")
    public R<List<PmsQualityChecklist>> getChecklist(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String stage) {
        List<PmsQualityChecklist> list = defectService.getChecklist(projectId, stage);
        return R.ok(list);
    }

    @PostMapping("/checklist/result")
    @Operation(summary = "保存检查结果")
    public R<PmsQualityCheckResult> saveCheckResult(@RequestBody PmsQualityCheckResult result) {
        PmsQualityCheckResult saved = defectService.saveCheckResult(result);
        return R.ok(saved);
    }

    @GetMapping("/checklist/results")
    @Operation(summary = "查询检查结果")
    public R<List<PmsQualityCheckResult>> getCheckResults(@RequestParam(required = false) Long projectId) {
        List<PmsQualityCheckResult> results = defectService.getCheckResults(projectId);
        return R.ok(results);
    }

    // ============================================================
    // Inner request DTOs
    // ============================================================

    @Data
    static class AssignRequest {
        private Long assigneeId;
    }

    @Data
    static class StatusUpdateRequest {
        private String status;
    }
}
