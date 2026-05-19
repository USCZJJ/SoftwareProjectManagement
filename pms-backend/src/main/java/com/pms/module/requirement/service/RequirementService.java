package com.pms.module.requirement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pms.module.requirement.entity.PmsReqChange;
import com.pms.module.requirement.entity.PmsReqTrace;
import com.pms.module.requirement.entity.PmsRequirement;

import java.util.List;

public interface RequirementService {

    IPage<PmsRequirement> getList(Long projectId, String keyword, String status, String priority, int page, int size);

    PmsRequirement getById(Long id);

    PmsRequirement create(PmsRequirement req, Long userId);

    PmsRequirement update(PmsRequirement req, Long userId);

    void delete(Long id);

    void submitForReview(Long id, Long reviewerId);

    void review(Long id, String status, Long reviewerId);

    PmsReqChange changeRequest(PmsReqChange change, Long userId);

    void approveChange(Long changeId, boolean approved, Long approverId, String comment);

    List<PmsReqTrace> getTraces(Long projectId, Long reqId);

    PmsReqTrace addTrace(PmsReqTrace trace);

    void deleteTrace(Long traceId);
}
