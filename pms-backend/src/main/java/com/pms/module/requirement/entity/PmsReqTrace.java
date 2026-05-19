package com.pms.module.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_req_trace")
public class PmsReqTrace {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long projectId;

    private Long reqId;

    private Long taskId;

    private Long defectId;

    private Long testCaseId;

    private String traceNote;

    private LocalDateTime createTime;
}
