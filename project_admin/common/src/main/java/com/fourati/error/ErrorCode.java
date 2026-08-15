package com.fourati.error;

public enum ErrorCode {

    VALIDATION_FAILED,
    RESOURCE_NOT_FOUND,
    CONFLICT,
    BUSINESS_RULE_VIOLATION,
    INTERNAL_ERROR,
    UNAUTHORIZED,
    FORBIDDEN,
    RATE_LIMIT_EXCEEDED,

    ITEM_INACTIVE,          // item must be active for this operation
    ITEM_EXPORT_TOO_LARGE,  // export exceeds the configured max-rows limit

    SETTING_NOT_EDITABLE,   // setting is marked non-editable and cannot be updated/deleted

    NOTIFICATION_TEMPLATE_CODE_EXISTS,      // duplicate notification_templates.code
    NOTIFICATION_ALREADY_SENT,              // notification status can no longer be updated once sent
    USER_NOTIFICATION_ALREADY_EXISTS;       // duplicate (user_id, notification_id) pairing

    public String code() {
        return this.name();
    }
}
