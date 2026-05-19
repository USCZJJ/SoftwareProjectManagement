package com.pms.module.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pms.module.project.entity.PmsProject;
import com.pms.module.project.entity.PmsWbs;
import com.pms.module.project.entity.PmsMilestone;
import com.pms.module.project.entity.PmsTaskDependency;
import com.pms.module.project.entity.PmsPlanChange;
import com.pms.module.project.entity.PmsProjectMember;

import java.util.List;
import java.util.Map;

public interface ProjectService {

    PmsProject createProject(PmsProject project);

    PmsProject updateProject(PmsProject project);

    void deleteProject(Long id);

    IPage<PmsProject> getProjectList(String keyword, String status, int page, int size);

    Map<String, Object> getProjectDetail(Long id);

    List<PmsWbs> getWbsTree(Long projectId);

    PmsWbs saveWbs(PmsWbs wbs);

    void updateWbsProgress(Long wbsId, Integer progress);

    void deleteWbs(Long wbsId);

    List<PmsMilestone> getMilestones(Long projectId);

    PmsMilestone saveMilestone(PmsMilestone ms);

    List<PmsTaskDependency> getDependencies(Long projectId);

    PmsTaskDependency saveDependency(PmsTaskDependency dep);

    void deleteDependency(Long depId);

    List<Long> calculateCriticalPath(Long projectId);

    PmsPlanChange submitPlanChange(PmsPlanChange change);

    void approvePlanChange(Long changeId, boolean approved, String comment);

    List<PmsPlanChange> getPlanChanges(Long projectId);

    void addMember(Long projectId, Long userId, String role);

    void removeMember(Long projectId, Long userId);

    List<PmsProjectMember> getMembers(Long projectId);
}
