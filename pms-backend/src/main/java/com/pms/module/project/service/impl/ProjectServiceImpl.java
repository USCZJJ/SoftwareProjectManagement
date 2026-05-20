package com.pms.module.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.exception.BusinessException;
import com.pms.common.result.ResultCode;
import com.pms.module.project.entity.PmsMilestone;
import com.pms.module.project.entity.PmsPlanChange;
import com.pms.module.project.entity.PmsProject;
import com.pms.module.project.entity.PmsProjectMember;
import com.pms.module.project.entity.PmsTaskDependency;
import com.pms.module.project.entity.PmsWbs;
import com.pms.module.project.mapper.PmsMilestoneMapper;
import com.pms.module.project.mapper.PmsPlanChangeMapper;
import com.pms.module.project.mapper.PmsProjectMapper;
import com.pms.module.project.mapper.PmsProjectMemberMapper;
import com.pms.module.project.mapper.PmsTaskDependencyMapper;
import com.pms.module.project.mapper.PmsWbsMapper;
import com.pms.module.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final PmsProjectMapper projectMapper;
    private final PmsWbsMapper wbsMapper;
    private final PmsMilestoneMapper milestoneMapper;
    private final PmsTaskDependencyMapper taskDependencyMapper;
    private final PmsPlanChangeMapper planChangeMapper;
    private final PmsProjectMemberMapper projectMemberMapper;

    @Override
    @Transactional
    public PmsProject createProject(PmsProject project) {
        String year = String.valueOf(Year.now().getValue());
        LambdaQueryWrapper<PmsProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(PmsProject::getProjectCode, "PROJ-" + year)
                .orderByDesc(PmsProject::getProjectCode)
                .last("LIMIT 1");
        PmsProject last = projectMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getProjectCode() != null) {
            String code = last.getProjectCode();
            String suffix = code.substring(code.lastIndexOf("-") + 1);
            try {
                seq = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        project.setProjectCode("PROJ-" + year + "-" + String.format("%03d", seq));
        projectMapper.insert(project);
        return project;
    }

    @Override
    @Transactional
    public PmsProject updateProject(PmsProject project) {
        PmsProject existing = projectMapper.selectById(project.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        projectMapper.updateById(project);
        return projectMapper.selectById(project.getId());
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        PmsProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        projectMapper.deleteById(id);
    }

    @Override
    public IPage<PmsProject> getProjectList(String keyword, String status, int page, int size) {
        Page<PmsProject> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<PmsProject> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(PmsProject::getProjectName, keyword)
                    .or()
                    .like(PmsProject::getProjectCode, keyword)
            );
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PmsProject::getStatus, status);
        }
        wrapper.orderByDesc(PmsProject::getCreateTime);
        return projectMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public Map<String, Object> getProjectDetail(Long id) {
        PmsProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        List<PmsProjectMember> members = projectMemberMapper.selectList(
                new LambdaQueryWrapper<PmsProjectMember>().eq(PmsProjectMember::getProjectId, id)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        result.put("members", members);
        return result;
    }

    @Override
    public List<PmsWbs> getWbsTree(Long projectId) {
        List<PmsWbs> allNodes = wbsMapper.selectList(
                new LambdaQueryWrapper<PmsWbs>()
                        .eq(PmsWbs::getProjectId, projectId)
                        .orderByAsc(PmsWbs::getSortOrder)
        );
        if (allNodes.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, List<PmsWbs>> childrenMap = allNodes.stream()
                .filter(w -> w.getParentId() != null && w.getParentId() != 0)
                .collect(Collectors.groupingBy(PmsWbs::getParentId));
        for (PmsWbs node : allNodes) {
            node.setChildren(childrenMap.getOrDefault(node.getId(), new ArrayList<>()));
        }
        return allNodes.stream()
                .filter(w -> w.getParentId() == null || w.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PmsWbs saveWbs(PmsWbs wbs) {
        if (wbs.getId() == null) {
            wbsMapper.insert(wbs);
        } else {
            PmsWbs existing = wbsMapper.selectById(wbs.getId());
            if (existing == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "WBS节点不存在");
            }
            wbsMapper.updateById(wbs);
        }
        return wbsMapper.selectById(wbs.getId());
    }

    @Override
    @Transactional
    public void updateWbsProgress(Long wbsId, Integer progress) {
        PmsWbs wbs = wbsMapper.selectById(wbsId);
        if (wbs == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "WBS节点不存在");
        }
        wbs.setProgress(progress);
        if (progress != null && progress == 100 && wbs.getActualEnd() == null) {
            wbs.setActualEnd(LocalDate.now());
        }
        if (progress != null && progress > 0 && wbs.getActualStart() == null) {
            wbs.setActualStart(LocalDate.now());
        }
        wbsMapper.updateById(wbs);
    }

    @Override
    @Transactional
    public void deleteWbs(Long wbsId) {
        PmsWbs wbs = wbsMapper.selectById(wbsId);
        if (wbs == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "WBS节点不存在");
        }
        Long count = wbsMapper.selectCount(
                new LambdaQueryWrapper<PmsWbs>().eq(PmsWbs::getParentId, wbsId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该节点下存在子节点，无法删除");
        }
        taskDependencyMapper.delete(
                new LambdaQueryWrapper<PmsTaskDependency>()
                        .eq(PmsTaskDependency::getPredecessorWbsId, wbsId)
                        .or()
                        .eq(PmsTaskDependency::getSuccessorWbsId, wbsId)
        );
        wbsMapper.deleteById(wbsId);
    }

    @Override
    public List<PmsMilestone> getMilestones(Long projectId) {
        return milestoneMapper.selectList(
                new LambdaQueryWrapper<PmsMilestone>()
                        .eq(PmsMilestone::getProjectId, projectId)
                        .orderByAsc(PmsMilestone::getPlannedDate)
        );
    }

    @Override
    @Transactional
    public PmsMilestone saveMilestone(PmsMilestone ms) {
        if (ms.getId() == null) {
            milestoneMapper.insert(ms);
        } else {
            PmsMilestone existing = milestoneMapper.selectById(ms.getId());
            if (existing == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "里程碑不存在");
            }
            milestoneMapper.updateById(ms);
        }
        return milestoneMapper.selectById(ms.getId());
    }

    @Override
    public List<PmsTaskDependency> getDependencies(Long projectId) {
        return taskDependencyMapper.selectList(
                new LambdaQueryWrapper<PmsTaskDependency>()
                        .eq(PmsTaskDependency::getProjectId, projectId)
        );
    }

    @Override
    @Transactional
    public PmsTaskDependency saveDependency(PmsTaskDependency dep) {
        if (dep.getPredecessorWbsId() == null || dep.getSuccessorWbsId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "前置任务和后置任务不能为空");
        }
        if (dep.getPredecessorWbsId().equals(dep.getSuccessorWbsId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "任务不能依赖自身");
        }
        PmsWbs pred = wbsMapper.selectById(dep.getPredecessorWbsId());
        if (pred == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "前置WBS节点不存在");
        }
        PmsWbs succ = wbsMapper.selectById(dep.getSuccessorWbsId());
        if (succ == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "后置WBS节点不存在");
        }
        if (!pred.getProjectId().equals(dep.getProjectId()) || !succ.getProjectId().equals(dep.getProjectId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "依赖关系必须属于同一项目");
        }
        if (dep.getId() == null) {
            taskDependencyMapper.insert(dep);
        } else {
            PmsTaskDependency existing = taskDependencyMapper.selectById(dep.getId());
            if (existing == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "依赖关系不存在");
            }
            taskDependencyMapper.updateById(dep);
        }
        return taskDependencyMapper.selectById(dep.getId());
    }

    @Override
    @Transactional
    public void deleteDependency(Long depId) {
        PmsTaskDependency dep = taskDependencyMapper.selectById(depId);
        if (dep == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "依赖关系不存在");
        }
        taskDependencyMapper.deleteById(depId);
    }

    @Override
    public List<Long> calculateCriticalPath(Long projectId) {
        List<PmsWbs> wbsList = wbsMapper.selectList(
                new LambdaQueryWrapper<PmsWbs>().eq(PmsWbs::getProjectId, projectId)
        );
        if (wbsList.isEmpty()) {
            return new ArrayList<>();
        }

        List<PmsTaskDependency> deps = taskDependencyMapper.selectList(
                new LambdaQueryWrapper<PmsTaskDependency>().eq(PmsTaskDependency::getProjectId, projectId)
        );

        int n = wbsList.size();
        Map<Long, Integer> wbsIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            wbsIndex.put(wbsList.get(i).getId(), i);
        }

        // Build adjacency: successors and predecessors
        List<List<Integer>> succ = new ArrayList<>(n);
        List<List<Integer>> pred = new ArrayList<>(n);
        List<List<Integer>> succLag = new ArrayList<>(n);
        List<List<Integer>> predLag = new ArrayList<>(n);
        int[] indegree = new int[n];

        for (int i = 0; i < n; i++) {
            succ.add(new ArrayList<>());
            pred.add(new ArrayList<>());
            succLag.add(new ArrayList<>());
            predLag.add(new ArrayList<>());
        }

        for (PmsTaskDependency dep : deps) {
            Integer u = wbsIndex.get(dep.getPredecessorWbsId());
            Integer v = wbsIndex.get(dep.getSuccessorWbsId());
            if (u != null && v != null) {
                int lag = dep.getLagDays() != null ? dep.getLagDays() : 0;
                succ.get(u).add(v);
                succLag.get(u).add(lag);
                pred.get(v).add(u);
                predLag.get(v).add(lag);
                indegree[v]++;
            }
        }

        // Topological sort (Kahn's algorithm)
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                queue.offer(i);
            }
        }

        List<Integer> topo = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            topo.add(u);
            for (int v : succ.get(u)) {
                indegree[v]--;
                if (indegree[v] == 0) {
                    queue.offer(v);
                }
            }
        }

        if (topo.size() != n) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "任务依赖关系存在循环回路，无法计算关键路径");
        }

        // Compute durations (in days)
        int[] duration = new int[n];
        for (int i = 0; i < n; i++) {
            PmsWbs w = wbsList.get(i);
            int d = 0;
            if (w.getPlannedStart() != null && w.getPlannedEnd() != null) {
                d = (int) ChronoUnit.DAYS.between(w.getPlannedStart(), w.getPlannedEnd());
            }
            duration[i] = Math.max(d, 0);
        }

        // Forward pass: compute ES and EF
        int[] ES = new int[n];
        int[] EF = new int[n];
        for (int idx : topo) {
            int maxPredEF = 0;
            List<Integer> preds = pred.get(idx);
            List<Integer> lags = predLag.get(idx);
            for (int k = 0; k < preds.size(); k++) {
                int p = preds.get(k);
                int lag = lags.get(k);
                maxPredEF = Math.max(maxPredEF, EF[p] + lag);
            }
            ES[idx] = maxPredEF;
            EF[idx] = maxPredEF + duration[idx];
        }

        // Project end is the max EF
        int projectEnd = 0;
        for (int i = 0; i < n; i++) {
            projectEnd = Math.max(projectEnd, EF[i]);
        }

        // Backward pass: compute LF and LS
        int[] LF = new int[n];
        int[] LS = new int[n];
        for (int i = 0; i < n; i++) {
            LF[i] = projectEnd;
        }

        // Process in reverse topological order
        for (int i = topo.size() - 1; i >= 0; i--) {
            int idx = topo.get(i);
            List<Integer> succs = succ.get(idx);
            List<Integer> lags = succLag.get(idx);
            if (!succs.isEmpty()) {
                int minSuccLS = Integer.MAX_VALUE;
                for (int k = 0; k < succs.size(); k++) {
                    int s = succs.get(k);
                    int lag = lags.get(k);
                    minSuccLS = Math.min(minSuccLS, LS[s] - lag);
                }
                LF[idx] = minSuccLS;
            }
            // If no successors, LF stays at projectEnd
            LS[idx] = LF[idx] - duration[idx];
        }

        // Identify critical nodes (ES == LS)
        List<Long> criticalIds = new ArrayList<>();
        for (int idx : topo) {
            if (ES[idx] == LS[idx]) {
                criticalIds.add(wbsList.get(idx).getId());
            }
        }

        return criticalIds;
    }

    @Override
    @Transactional
    public PmsPlanChange submitPlanChange(PmsPlanChange change) {
        change.setStatus("PENDING");
        planChangeMapper.insert(change);
        return planChangeMapper.selectById(change.getId());
    }

    @Override
    @Transactional
    public void approvePlanChange(Long changeId, boolean approved, String comment) {
        PmsPlanChange change = planChangeMapper.selectById(changeId);
        if (change == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "变更申请不存在");
        }
        if (!"PENDING".equals(change.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该变更申请已处理，无法重复审批");
        }
        change.setStatus(approved ? "APPROVED" : "REJECTED");
        change.setApproveComment(comment);
        planChangeMapper.updateById(change);
    }

    @Override
    public List<PmsPlanChange> getPlanChanges(Long projectId) {
        return planChangeMapper.selectList(
                new LambdaQueryWrapper<PmsPlanChange>()
                        .eq(PmsPlanChange::getProjectId, projectId)
                        .orderByDesc(PmsPlanChange::getCreateTime)
        );
    }

    @Override
    @Transactional
    public void addMember(Long projectId, Long userId, String role) {
        PmsProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
        Long count = projectMemberMapper.selectCount(
                new LambdaQueryWrapper<PmsProjectMember>()
                        .eq(PmsProjectMember::getProjectId, projectId)
                        .eq(PmsProjectMember::getUserId, userId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该用户已是项目成员");
        }
        PmsProjectMember member = new PmsProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(LocalDateTime.now());
        projectMemberMapper.insert(member);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        Long count = projectMemberMapper.selectCount(
                new LambdaQueryWrapper<PmsProjectMember>()
                        .eq(PmsProjectMember::getProjectId, projectId)
                        .eq(PmsProjectMember::getUserId, userId)
        );
        if (count == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "该用户不是项目成员");
        }
        projectMemberMapper.delete(
                new LambdaQueryWrapper<PmsProjectMember>()
                        .eq(PmsProjectMember::getProjectId, projectId)
                        .eq(PmsProjectMember::getUserId, userId)
        );
    }

    @Override
    public List<PmsProjectMember> getMembers(Long projectId) {
        return projectMemberMapper.selectList(
                new LambdaQueryWrapper<PmsProjectMember>()
                        .eq(PmsProjectMember::getProjectId, projectId)
        );
    }
}
