package com.pms.common.result;

public enum ResultCode {
    SUCCESS(200, "success"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    USERNAME_PASSWORD_ERROR(1001, "用户名或密码错误"),
    TOKEN_EXPIRED(1002, "令牌已过期"),
    TOKEN_INVALID(1003, "无效令牌"),
    DUPLICATE_NAME(1004, "名称已存在"),
    VALIDATION_ERROR(1005, "数据校验失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
