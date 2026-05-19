package com.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_kanban_board")
public class PmsKanbanBoard extends BaseEntity {

    private Long projectId;

    private String boardName;

    private String description;

    private Integer isDefault;
}
