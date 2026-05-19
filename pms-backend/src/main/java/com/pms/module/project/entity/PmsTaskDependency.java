package com.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_task_dependency")
public class PmsTaskDependency extends BaseEntity {

    private Long projectId;

    private Long predecessorWbsId;

    private Long successorWbsId;

    private String dependencyType;

    private Integer lagDays;
}
