package com.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_worklog")
public class PmsWorklog extends BaseEntity {

    private Long taskId;

    private Long userId;

    private Long projectId;

    private LocalDate workDate;

    private BigDecimal hours;

    private String content;

    private String planDetail;
}
