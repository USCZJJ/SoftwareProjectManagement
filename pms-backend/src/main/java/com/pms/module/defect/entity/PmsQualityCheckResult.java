package com.pms.module.defect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_quality_check_result")
public class PmsQualityCheckResult {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long checklistId;

    private Long projectId;

    private String result;

    private String remark;

    private Long checkUserId;

    private LocalDateTime checkTime;
}
