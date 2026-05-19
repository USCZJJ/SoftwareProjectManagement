package com.pms.module.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pms.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {
    private String permName;
    private String permCode;
    private String permType;
    private Long parentId;
    private String path;
}
