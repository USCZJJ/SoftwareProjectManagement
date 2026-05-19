package com.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_task_log")
public class PmsTaskLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    private Long operUserId;

    /** CREATE / STATUS_CHANGE / ASSIGN / UPDATE */
    private String action;

    private String oldValue;

    private String newValue;

    private String remark;

    private LocalDateTime createTime;
}
