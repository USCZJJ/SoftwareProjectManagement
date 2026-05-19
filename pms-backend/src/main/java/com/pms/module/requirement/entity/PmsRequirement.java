package com.pms.module.requirement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_requirement")
public class PmsRequirement extends BaseEntity {

    private Long projectId;

    private String reqCode;

    private String title;

    private String description;

    private String type;

    private String priority;

    private Integer severity;

    private String source;

    private String status;

    private Long submitUserId;

    private Long reviewUserId;

    private Long assigneeId;

    private Integer version;
}
