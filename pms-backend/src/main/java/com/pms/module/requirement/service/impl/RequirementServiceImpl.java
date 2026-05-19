package com.pms.module.requirement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.exception.BusinessException;
import com.pms.common.result.ResultCode;
import com.pms.module.requirement.entity.PmsReqChange;
import com.pms.module.requirement.entity.PmsReqTrace;
import com.pms.module.requirement.entity.PmsRequirement;
import com.pms.module.requirement.mapper.PmsReqChangeMapper;
import com.pms.module.requirement.mapper.PmsReqTraceMapper;
import com.pms.module.requirement.mapper.PmsRequirementMapper;
import com.pms.module.requirement.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequirementServiceImpl implements RequirementService {

    private final PmsRequirementMapper requirementMapper;
    private final PmsReqTraceMapper reqTraceMapper;
    private final PmsReqChangeMapper reqChangeMapper;

    @Override
    public IPage<PmsRequirement> getList(Long projectId, String keyword, String status, String priority, int page, int size) {
        LambdaQueryWrapper<PmsRequirement> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(PmsRequirement::getProjectId, projectId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(PmsRequirement::getTitle, keyword)
                    .or()
                    .like(PmsRequirement::getReqCode, keyword));
        }
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(PmsRequirement::getStatus, status);
        }
        if (priority != null && !priority.trim().isEmpty()) {
            wrapper.eq(PmsRequirement::getPriority, priority);
        }
        wrapper.orderByDesc(PmsRequirement::getCreateTime);
        return requirementMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public PmsRequirement getById(Long id) {
        PmsRequirement req = requirementMapper.selectById(id);
        if (req == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "需求不存在");
        }
        return req;
    }

    @Override
    @Transactional
    public PmsRequirement create(PmsRequirement req, Long userId) {
        req.setId(null);
        req.setStatus("DRAFT");
        req.setVersion(1);
        req.setSubmitUserId(userId);

        String reqCode = generateReqCode(req.getProjectId());
        req.setReqCode(reqCode);

        requirementMapper.insert(req);
        return req;
    }

    @Override
    @Transactional
    public PmsRequirement update(PmsRequirement req, Long userId) {
        PmsRequirement existing = getById(req.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "需求不存在");
        }

        req.setReqCode(existing.getReqCode());
        req.setCreateTime(existing.getCreateTime());
        req.setVersion(existing.getVersion() + 1);

        requirementMapper.updateById(req);
        return req;
    }

    @Override
    public void delete(Long id) {
        PmsRequirement existing = getById(id);
        requirementMapper.deleteById(existing.getId());
    }

    @Override
    @Transactional
    public void submitForReview(Long id, Long reviewerId) {
        PmsRequirement req = getById(id);
        if (!"DRAFT".equals(req.getStatus()) && !"REJECTED".equals(req.getStatus())
                && !"CHANGED".equals(req.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许提交评审");
        }
        req.setStatus("SUBMITTED");
        req.setReviewUserId(reviewerId);
        requirementMapper.updateById(req);
    }

    @Override
    @Transactional
    public void review(Long id, String status, Long reviewerId) {
        PmsRequirement req = getById(id);
        if (!"SUBMITTED".equals(req.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许评审");
        }
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "评审结果只能是APPROVED或REJECTED");
        }
        req.setStatus(status);
        req.setReviewUserId(reviewerId);
        requirementMapper.updateById(req);
    }

    @Override
    @Transactional
    public PmsReqChange changeRequest(PmsReqChange change, Long userId) {
        PmsRequirement req = getById(change.getReqId());
        if (!"APPROVED".equals(req.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "只有已通过的需求才能提交变更");
        }

        change.setId(null);
        change.setApplyUserId(userId);
        change.setStatus("PENDING");
        change.setCreateTime(LocalDateTime.now());
        change.setUpdateTime(LocalDateTime.now());

        reqChangeMapper.insert(change);
        return change;
    }

    @Override
    @Transactional
    public void approveChange(Long changeId, boolean approved, Long approverId, String comment) {
        PmsReqChange change = reqChangeMapper.selectById(changeId);
        if (change == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "变更请求不存在");
        }
        if (!"PENDING".equals(change.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "当前状态不允许审批");
        }

        if (approved) {
            change.setStatus("APPROVED");
            PmsRequirement req = getById(change.getReqId());
            req.setStatus("CHANGED");
            req.setDescription(change.getChangeDesc());
            req.setVersion(req.getVersion() + 1);
            requirementMapper.updateById(req);
        } else {
            change.setStatus("REJECTED");
        }

        change.setApproveUserId(approverId);
        change.setApproveComment(comment);
        change.setUpdateTime(LocalDateTime.now());
        reqChangeMapper.updateById(change);
    }

    @Override
    public List<PmsReqTrace> getTraces(Long projectId, Long reqId) {
        LambdaQueryWrapper<PmsReqTrace> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(PmsReqTrace::getProjectId, projectId);
        }
        if (reqId != null) {
            wrapper.eq(PmsReqTrace::getReqId, reqId);
        }
        return reqTraceMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public PmsReqTrace addTrace(PmsReqTrace trace) {
        trace.setId(null);
        trace.setCreateTime(LocalDateTime.now());
        reqTraceMapper.insert(trace);
        return trace;
    }

    @Override
    @Transactional
    public void deleteTrace(Long traceId) {
        PmsReqTrace trace = reqTraceMapper.selectById(traceId);
        if (trace == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "追溯记录不存在");
        }
        reqTraceMapper.deleteById(traceId);
    }

    private String generateReqCode(Long projectId) {
        String prefix = "REQ-" + projectId + "-";
        LambdaQueryWrapper<PmsRequirement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PmsRequirement::getProjectId, projectId)
                .likeRight(PmsRequirement::getReqCode, prefix);
        Long count = requirementMapper.selectCount(wrapper);
        long seq = count + 1;
        return prefix + String.format("%04d", seq);
    }
}
