package com.pms.module.defect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_defect")
public class PmsDefect extends BaseEntity {

    private Long projectId;

    private String defectCode;

    private String title;

    private String description;

    private String stepsToReproduce;

    private String expectedResult;

    private String actualResult;

    private String severity;

    private String priority;

    private String status;

    private String module;

    private String versionFound;

    private String versionFixed;

    private Long reporterId;

    private Long assigneeId;

    private String screenshotUrls;
}
