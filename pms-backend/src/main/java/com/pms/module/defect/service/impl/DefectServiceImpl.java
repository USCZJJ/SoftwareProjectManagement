package com.pms.module.defect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.exception.BusinessException;
import com.pms.common.result.ResultCode;
import com.pms.module.defect.entity.*;
import com.pms.module.defect.mapper.*;
import com.pms.module.defect.service.DefectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefectServiceImpl implements DefectService {

    private final PmsDefectMapper defectMapper;
    private final PmsTestCaseMapper testCaseMapper;
    private final PmsTestExecutionMapper testExecutionMapper;
    private final PmsQualityChecklistMapper qualityChecklistMapper;
    private final PmsQualityCheckResultMapper qualityCheckResultMapper;

    // ============================================================
    // Defects
    // ============================================================

    @Override
    public IPage<PmsDefect> getList(Long projectId, String keyword, String status, String severity,
                                     Long assigneeId, int page, int size) {
        LambdaQueryWrapper<PmsDefect> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(PmsDefect::getProjectId, projectId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(PmsDefect::getTitle, keyword)
                    .or()
                    .like(PmsDefect::getDefectCode, keyword));
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(PmsDefect::getStatus, status);
        }
        if (severity != null && !severity.trim().isEmpty()) {
            wrapper.eq(PmsDefect::getSeverity, severity);
        }
        if (assigneeId != null) {
            wrapper.eq(PmsDefect::getAssigneeId, assigneeId);
        }
        wrapper.orderByDesc(PmsDefect::getCreateTime);
        return defectMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public PmsDefect getById(Long id) {
        PmsDefect defect = defectMapper.selectById(id);
        if (defect == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "缺陷不存在");
        }
        return defect;
    }

    @Override
    @Transactional
    public PmsDefect create(PmsDefect defect, Long reporterId) {
        defect.setId(null);
        defect.setReporterId(reporterId);
        defect.setStatus("OPEN");

        String defectCode = generateDefectCode(defect.getProjectId());
        defect.setDefectCode(defectCode);

        defectMapper.insert(defect);
        return defect;
    }

    @Override
    @Transactional
    public PmsDefect update(PmsDefect defect) {
        PmsDefect existing = getById(defect.getId());

        defect.setDefectCode(existing.getDefectCode());
        defect.setCreateTime(existing.getCreateTime());

        defectMapper.updateById(defect);
        return defect;
    }

    @Override
    @Transactional
    public void assign(Long defectId, Long assigneeId) {
        PmsDefect defect = getById(defectId);
        defect.setAssigneeId(assigneeId);
        if ("OPEN".equals(defect.getStatus())) {
            defect.setStatus("ASSIGNED");
        }
        defectMapper.updateById(defect);
    }

    @Override
    @Transactional
    public void updateStatus(Long defectId, String newStatus, Long operatorId) {
        PmsDefect defect = getById(defectId);

        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "状态不能为空");
        }

        String[] validStatuses = {"OPEN", "ASSIGNED", "FIXING", "FIXED", "VERIFYING", "VERIFIED", "CLOSED", "REOPENED"};
        boolean valid = false;
        for (String s : validStatuses) {
            if (s.equals(newStatus)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的状态值: " + newStatus);
        }

        defect.setStatus(newStatus);
        defectMapper.updateById(defect);
    }

    @Override
    public void delete(Long id) {
        PmsDefect defect = getById(id);
        defectMapper.deleteById(defect.getId());
    }

    private String generateDefectCode(Long projectId) {
        String prefix = "DEF-" + projectId + "-";
        LambdaQueryWrapper<PmsDefect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsDefect::getProjectId, projectId)
                .likeRight(PmsDefect::getDefectCode, prefix);
        Long count = defectMapper.selectCount(wrapper);
        long seq = count + 1;
        return prefix + String.format("%04d", seq);
    }

    // ============================================================
    // Test Cases
    // ============================================================

    @Override
    public IPage<PmsTestCase> getCaseList(Long projectId, int page, int size) {
        LambdaQueryWrapper<PmsTestCase> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(PmsTestCase::getProjectId, projectId);
        }
        wrapper.orderByDesc(PmsTestCase::getCreateTime);
        return testCaseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public PmsTestCase createCase(PmsTestCase tc) {
        tc.setId(null);
        tc.setStatus(tc.getStatus() == null ? "DRAFT" : tc.getStatus());

        String caseCode = generateCaseCode(tc.getProjectId());
        tc.setCaseCode(caseCode);

        testCaseMapper.insert(tc);
        return tc;
    }

    @Override
    @Transactional
    public PmsTestCase updateCase(PmsTestCase tc) {
        PmsTestCase existing = testCaseMapper.selectById(tc.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "测试用例不存在");
        }
        tc.setCaseCode(existing.getCaseCode());
        tc.setCreateTime(existing.getCreateTime());
        testCaseMapper.updateById(tc);
        return tc;
    }

    @Override
    @Transactional
    public void deleteCase(Long id) {
        PmsTestCase tc = testCaseMapper.selectById(id);
        if (tc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "测试用例不存在");
        }
        testCaseMapper.deleteById(id);
    }

    @Override
    @Transactional
    public PmsTestExecution executeTest(PmsTestExecution exec) {
        // verify test case exists
        PmsTestCase tc = testCaseMapper.selectById(exec.getTestCaseId());
        if (tc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "测试用例不存在");
        }

        exec.setId(null);
        if (exec.getExecTime() == null) {
            exec.setExecTime(LocalDateTime.now());
        }

        // if execution result is FAIL and has a defectId, optionally link back
        testExecutionMapper.insert(exec);
        return exec;
    }

    @Override
    public List<PmsTestExecution> getExecutions(Long testCaseId) {
        LambdaQueryWrapper<PmsTestExecution> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsTestExecution::getTestCaseId, testCaseId)
                .orderByDesc(PmsTestExecution::getExecTime);
        return testExecutionMapper.selectList(wrapper);
    }

    private String generateCaseCode(Long projectId) {
        String prefix = "TC-" + projectId + "-";
        LambdaQueryWrapper<PmsTestCase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsTestCase::getProjectId, projectId)
                .likeRight(PmsTestCase::getCaseCode, prefix);
        Long count = testCaseMapper.selectCount(wrapper);
        long seq = count + 1;
        return prefix + String.format("%04d", seq);
    }

    // ============================================================
    // Quality Checklist
    // ============================================================

    @Override
    public List<PmsQualityChecklist> getChecklist(Long projectId, String stage) {
        LambdaQueryWrapper<PmsQualityChecklist> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(PmsQualityChecklist::getProjectId, projectId);
        }
        if (stage != null && !stage.trim().isEmpty()) {
            wrapper.eq(PmsQualityChecklist::getStage, stage);
        }
        return qualityChecklistMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public PmsQualityCheckResult saveCheckResult(PmsQualityCheckResult result) {
        result.setId(null);
        if (result.getCheckTime() == null) {
            result.setCheckTime(LocalDateTime.now());
        }
        qualityCheckResultMapper.insert(result);
        return result;
    }

    @Override
    public List<PmsQualityCheckResult> getCheckResults(Long projectId) {
        LambdaQueryWrapper<PmsQualityCheckResult> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(PmsQualityCheckResult::getProjectId, projectId);
        }
        wrapper.orderByDesc(PmsQualityCheckResult::getCheckTime);
        return qualityCheckResultMapper.selectList(wrapper);
    }
}
