package com.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_milestone")
public class PmsMilestone extends BaseEntity {

    private Long projectId;

    private Long wbsId;

    private String milestoneName;

    private String description;

    private LocalDate plannedDate;

    private LocalDate actualDate;

    private String status;
}
