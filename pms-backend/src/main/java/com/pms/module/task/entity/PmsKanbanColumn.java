package com.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_kanban_column")
public class PmsKanbanColumn extends BaseEntity {

    private Long boardId;

    private String columnName;

    private String statusValue;

    private Integer wipLimit;

    private Integer sortOrder;

    private String color;
}
