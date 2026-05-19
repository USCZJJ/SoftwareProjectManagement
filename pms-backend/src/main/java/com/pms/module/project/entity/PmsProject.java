package com.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_project")
public class PmsProject extends BaseEntity {

    private String projectCode;

    private String projectName;

    private String description;

    private String objectives;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private BigDecimal budget;

    private BigDecimal actualCost;

    private String status;

    private Long managerId;
}
