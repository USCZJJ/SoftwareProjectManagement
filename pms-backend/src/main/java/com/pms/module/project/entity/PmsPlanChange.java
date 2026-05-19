package com.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_plan_change")
public class PmsPlanChange extends BaseEntity {

    private Long projectId;

    private String changeType;

    private String changeDesc;

    private String reason;

    private String impactAnalysis;

    private String affectedItems;

    private Long applyUserId;

    private Long approveUserId;

    private String status;

    private String approveComment;

    private LocalDateTime approveTime;

    private String versionSnapshot;
}
