package com.pms.module.defect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_quality_checklist")
public class PmsQualityChecklist {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long projectId;

    private String stage;

    private String checkItem;

    private String description;

    private Integer weight;

    private Integer isRequired;

    private LocalDateTime createTime;
}
