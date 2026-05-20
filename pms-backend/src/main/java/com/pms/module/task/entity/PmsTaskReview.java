package com.pms.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_task_review")
public class PmsTaskReview {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long taskId;

    private Long reviewerId;

    /** PASS / REJECT */
    private String result;

    private Integer score;

    private String comment;

    private String rejectReason;

    private LocalDateTime createTime;
}
