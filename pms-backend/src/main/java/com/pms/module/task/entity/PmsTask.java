package com.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_task")
public class PmsTask extends BaseEntity {

    private Long projectId;

    private Long wbsId;

    private String title;

    private String description;

    /** TASK / BUG / FEATURE */
    private String type;

    /** LOW / MEDIUM / HIGH / URGENT */
    private String priority;

    /** TODO / IN_PROGRESS / IN_REVIEW / DONE / CLOSED */
    private String status;

    private Long assigneeId;

    private Long reporterId;

    private Long kanbanColumnId;

    private BigDecimal plannedHours;

    private BigDecimal actualHours;

    /** Progress 0-100 */
    private Integer progress;

    private LocalDate startDate;

    private LocalDate dueDate;

    private Integer sortOrder;
}
