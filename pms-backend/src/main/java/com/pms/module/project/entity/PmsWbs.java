package com.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_wbs")
public class PmsWbs extends BaseEntity {

    private Long projectId;

    private Long parentId;

    private String wbsCode;

    private String nodeName;

    private String description;

    private Long assigneeId;

    private LocalDateTime plannedStart;

    private LocalDateTime plannedEnd;

    private LocalDateTime actualStart;

    private LocalDateTime actualEnd;

    private BigDecimal plannedHours;

    private BigDecimal actualHours;

    private Integer progress;

    private Integer sortOrder;

    private Integer isMilestone;

    @TableField(exist = false)
    private List<PmsWbs> children;
}
