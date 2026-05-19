package com.pms.module.defect.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pms.module.defect.entity.*;

import java.util.List;

public interface DefectService {

    // ========== Defects ==========
    IPage<PmsDefect> getList(Long projectId, String keyword, String status, String severity, Long assigneeId, int page, int size);

    PmsDefect getById(Long id);

    PmsDefect create(PmsDefect defect, Long reporterId);

    PmsDefect update(PmsDefect defect);

    void assign(Long defectId, Long assigneeId);

    void updateStatus(Long defectId, String newStatus, Long operatorId);

    void delete(Long id);

    // ========== Test Cases ==========
    IPage<PmsTestCase> getCaseList(Long projectId, int page, int size);

    PmsTestCase createCase(PmsTestCase tc);

    PmsTestCase updateCase(PmsTestCase tc);

    void deleteCase(Long id);

    PmsTestExecution executeTest(PmsTestExecution exec);

    List<PmsTestExecution> getExecutions(Long testCaseId);

    // ========== Quality Checklist ==========
    List<PmsQualityChecklist> getChecklist(Long projectId, String stage);

    PmsQualityCheckResult saveCheckResult(PmsQualityCheckResult result);

    List<PmsQualityCheckResult> getCheckResults(Long projectId);
}
