package com.pms.module.defect.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_test_execution")
public class PmsTestExecution {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long testCaseId;

    private Long executorId;

    private String result;

    private String actualResult;

    private String remark;

    private Long defectId;

    private LocalDateTime execTime;
}
