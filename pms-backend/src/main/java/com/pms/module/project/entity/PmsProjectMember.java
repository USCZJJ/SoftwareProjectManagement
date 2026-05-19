package com.pms.module.project.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pms_project_member")
public class PmsProjectMember extends BaseEntity {

    private Long projectId;

    private Long userId;

    private String role;

    private LocalDateTime joinTime;
}
