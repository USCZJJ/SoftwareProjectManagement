package com.pms.module.requirement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pms.common.result.R;
import com.pms.module.requirement.entity.PmsReqChange;
import com.pms.module.requirement.entity.PmsReqTrace;
import com.pms.module.requirement.entity.PmsRequirement;
import com.pms.module.requirement.service.RequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/requirement")
@Tag(name = "需求管理")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementService requirementService;

    @GetMapping("/list")
    @Operation(summary = "需求列表查询")
    public R<IPage<PmsRequirement>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<PmsRequirement> result = requirementService.getList(projectId, keyword, status, priority, page, size);
        return R.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "需求详情")
    public R<PmsRequirement> detail(@PathVariable Long id) {
        PmsRequirement req = requirementService.getById(id);
        return R.ok(req);
    }

    @PostMapping
    @Operation(summary = "创建需求")
    public R<PmsRequirement> create(@RequestBody PmsRequirement req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PmsRequirement created = requirementService.create(req, userId);
        return R.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新需求")
    public R<PmsRequirement> update(@PathVariable Long id, @RequestBody PmsRequirement req, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        req.setId(id);
        PmsRequirement updated = requirementService.update(req, userId);
        return R.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除需求")
    public R<Void> delete(@PathVariable Long id) {
        requirementService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/submit")
    @Operation(summary = "提交评审")
    public R<Void> submitForReview(@PathVariable Long id, @RequestBody SubmitReviewRequest body) {
        requirementService.submitForReview(id, body.getReviewerId());
        return R.ok();
    }

    @PutMapping("/{id}/review")
    @Operation(summary = "评审需求")
    public R<Void> review(@PathVariable Long id, @RequestBody ReviewRequest body) {
        requirementService.review(id, body.getStatus(), body.getReviewerId());
        return R.ok();
    }

    @PostMapping("/change")
    @Operation(summary = "提交变更请求")
    public R<PmsReqChange> changeRequest(@RequestBody PmsReqChange change, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PmsReqChange result = requirementService.changeRequest(change, userId);
        return R.ok(result);
    }

    @PutMapping("/change/{id}/approve")
    @Operation(summary = "审批变更")
    public R<Void> approveChange(@PathVariable Long id, @RequestBody ApproveChangeRequest body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        requirementService.approveChange(id, body.isApproved(), userId, body.getComment());
        return R.ok();
    }

    @GetMapping("/trace")
    @Operation(summary = "查询追溯矩阵")
    public R<List<PmsReqTrace>> getTraces(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long reqId) {
        List<PmsReqTrace> traces = requirementService.getTraces(projectId, reqId);
        return R.ok(traces);
    }

    @PostMapping("/trace")
    @Operation(summary = "添加追溯记录")
    public R<PmsReqTrace> addTrace(@RequestBody PmsReqTrace trace) {
        PmsReqTrace result = requirementService.addTrace(trace);
        return R.ok(result);
    }

    @DeleteMapping("/trace/{id}")
    @Operation(summary = "删除追溯记录")
    public R<Void> deleteTrace(@PathVariable Long id) {
        requirementService.deleteTrace(id);
        return R.ok();
    }

    // Inner DTO classes for request bodies

    @lombok.Data
    static class SubmitReviewRequest {
        private Long reviewerId;
    }

    @lombok.Data
    static class ReviewRequest {
        private String status;
        private Long reviewerId;
    }

    @lombok.Data
    static class ApproveChangeRequest {
        private boolean approved;
        private String comment;
    }
}
