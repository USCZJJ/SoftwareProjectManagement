package com.pms.module.requirement.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_req_change")
public class PmsReqChange {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long reqId;

    private String changeDesc;

    private String reason;

    private String impact;

    private String affectedReqs;

    private Long applyUserId;

    private String status;

    private Long approveUserId;

    private String approveComment;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
