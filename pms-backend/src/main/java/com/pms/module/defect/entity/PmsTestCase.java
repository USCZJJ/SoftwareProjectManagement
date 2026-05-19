package com.pms.module.defect.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_test_case")
public class PmsTestCase extends BaseEntity {

    private Long projectId;

    private String caseCode;

    private String title;

    private String description;

    private String precondition;

    private String testSteps;

    private String expectedResult;

    private String type;

    private String priority;

    private String status;

    private Long suiteId;

    private Long createUserId;
}
