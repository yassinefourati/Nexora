package com.fourati.platform.error;

import org.springframework.http.HttpStatus;


/**
 * The complete, stable catalog of machine-readable error codes. Every error
 * response carries exactly one of these as the {@code code} member of its
 * RFC 9457 problem-details body (ADR 0005), and the error catalog endpoint is
 * generated from this enum, so a code cannot exist without documentation.
 *
 * Clients and tests branch on the code, never on the human-readable text.
 * Add new codes here rather than inventing string literals at the throw site.
 */
public enum ErrorCode {

    // --- request shape (400) ---------------------------------------------------
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed",
            "One or more fields failed validation. See 'violations' for field-level details."),
    MALFORMED_REQUEST_BODY(HttpStatus.BAD_REQUEST, "The request body could not be parsed",
            "Check JSON syntax, field names and value types."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "Parameter type mismatch",
            "A path or query parameter has the wrong type (for example 'abc' instead of a UUID)."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "Required query parameter is missing",
            "Add the missing parameter to the request."),
    MISSING_HEADER(HttpStatus.BAD_REQUEST, "Required request header is missing",
            "Add the missing header to the request."),
    MISSING_PATH_VARIABLE(HttpStatus.BAD_REQUEST, "Path variable is missing",
            "The URL template is missing a required segment."),
    MULTIPART_ERROR(HttpStatus.BAD_REQUEST, "Multipart request could not be processed",
            "Check the multipart boundaries and part names."),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "Invalid argument provided",
            "A service method received an argument it cannot process."),
    INJECTION_DETECTED(HttpStatus.BAD_REQUEST, "Potentially dangerous input detected",
            "The request was blocked because it contains SQL injection or XSS patterns."),

    // --- authentication and authorization ----------------------------------------
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required",
            "Add a valid Bearer JWT in the Authorization header, or a valid X-Api-Key."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Insufficient permissions",
            "The credentials are valid but do not grant the required role, scope or ownership."),

    // --- routing ---------------------------------------------------------------------
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found",
            "The resource with the given id does not exist or has been deleted."),
    ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "Endpoint not found",
            "No endpoint exists at this path. Check the URL."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed",
            "The endpoint exists but does not support this HTTP verb."),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Cannot produce the requested media type",
            "The server cannot produce a response matching the Accept header."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type not supported",
            "Send the request with Content-Type: application/json."),
    CONTENT_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "Request body or upload exceeds the size limit",
            "Reduce the payload size or ask an administrator to raise the limit."),

    // --- state (409) -------------------------------------------------------------------
    CONFLICT(HttpStatus.CONFLICT, "Resource already exists",
            "A resource with the same unique key already exists."),
    CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "The resource was modified by someone else",
            "Reload the resource and retry with the current version.", true),
    ILLEGAL_STATE(HttpStatus.CONFLICT, "Operation not permitted in the current state",
            "The resource exists but its current state does not allow this operation."),
    IDEMPOTENT_REQUEST_IN_FLIGHT(HttpStatus.CONFLICT, "Idempotent request still being processed",
            "A request with the same X-Idempotency-Key is in flight. Retry shortly.", true),

    // --- generic business rule (422) ---------------------------------------------------
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_CONTENT, "Business rule violation",
            "The request is syntactically valid but violates a domain rule."),

    // --- rate limiting -----------------------------------------------------------------
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests",
            "The rate limit was exceeded. Slow down and retry after the Retry-After delay.", true),

    // --- server (5xx) ------------------------------------------------------------------
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error",
            "An unhandled exception occurred. Use the correlationId to find the trace in the logs."),
    SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Response could not be serialized",
            "The server produced a response it could not encode."),
    EXPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Export could not be generated",
            "The export failed on the server. Use the correlationId to find the trace."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "Feature not yet implemented",
            "This endpoint exists in the API contract but has not been implemented yet."),
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Downstream service unavailable",
            "A dependency is temporarily unavailable. Retry in a few moments.", true),

    // --- users -------------------------------------------------------------------------
    USER_USERNAME_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Username already taken", "Choose another username."),
    USER_EMAIL_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Email already registered", "Use another email address."),
    USER_NOT_ACTIVE(HttpStatus.UNPROCESSABLE_CONTENT, "User is not active",
            "Suspended or inactive users cannot be granted roles or act on the system."),

    // --- roles / permissions -----------------------------------------------------------
    ROLE_NAME_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Role name already exists", "Choose another role name."),
    ROLE_IS_SYSTEM(HttpStatus.UNPROCESSABLE_CONTENT, "System role is protected",
            "System roles cannot be deleted or renamed."),
    ROLE_ALREADY_ASSIGNED(HttpStatus.UNPROCESSABLE_CONTENT, "Role already assigned",
            "The (user, role, organization) grant already exists."),
    ROLE_ASSIGNMENT_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "Expiry is in the past",
            "expiresAt must be in the future."),
    PERMISSION_CODE_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Permission code already exists", "Choose another code."),
    PERMISSION_ALREADY_GRANTED(HttpStatus.UNPROCESSABLE_CONTENT, "Permission already granted",
            "The (role, permission) grant already exists."),

    // --- organisation structure --------------------------------------------------------
    ORGANIZATION_CODE_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Organization code already exists", "Choose another code."),
    DEPARTMENT_CODE_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Department code already exists", "Choose another code."),
    TEAM_NAME_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Team name already exists", "Choose another name."),
    ORGANIZATION_MEMBER_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "User is already a member",
            "The user already belongs to this organization."),
    HIERARCHY_CYCLE(HttpStatus.UNPROCESSABLE_CONTENT, "Hierarchy cycle",
            "The requested parent would create a cycle in the hierarchy."),

    // --- settings / flags / modules ----------------------------------------------------
    SETTING_NOT_EDITABLE(HttpStatus.UNPROCESSABLE_CONTENT, "Setting is not editable",
            "The setting is marked non-editable and cannot be updated or deleted."),
    SETTING_KEY_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Setting key already exists", "Choose another key."),
    FEATURE_FLAG_KEY_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Feature flag key already exists", "Choose another key."),
    APP_MODULE_KEY_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Application module key already exists", "Choose another key."),

    // --- menus -------------------------------------------------------------------------
    MENU_CODE_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Menu code already exists", "Choose another code."),
    MENU_ITEM_INVALID_PARENT(HttpStatus.UNPROCESSABLE_CONTENT, "Invalid parent menu item",
            "The parent must belong to the same menu and must not be the item itself."),

    // --- security objects --------------------------------------------------------------
    API_KEY_REVOKED(HttpStatus.UNPROCESSABLE_CONTENT, "API key already revoked", "The key was revoked earlier."),
    API_KEY_EXPIRED(HttpStatus.UNPROCESSABLE_CONTENT, "API key expired", "Create a new key."),
    SESSION_ALREADY_REVOKED(HttpStatus.UNPROCESSABLE_CONTENT, "Session already revoked", "The session was revoked earlier."),

    // --- content -----------------------------------------------------------------------
    TAG_NAME_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Tag name already exists", "Choose another name."),
    ENTITY_TAG_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Entity already tagged", "The tag is already attached to this entity."),
    METADATA_KEY_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Metadata key already exists",
            "The (entityType, entityId, key) triple already exists."),

    // --- notifications -----------------------------------------------------------------
    NOTIFICATION_TEMPLATE_CODE_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Notification template code already exists",
            "Choose another code."),
    NOTIFICATION_ALREADY_SENT(HttpStatus.UNPROCESSABLE_CONTENT, "Notification already sent",
            "A sent notification can no longer be changed."),
    USER_NOTIFICATION_ALREADY_EXISTS(HttpStatus.UNPROCESSABLE_CONTENT, "Notification already assigned",
            "The (user, notification) pairing already exists."),

    // --- export ------------------------------------------------------------------------
    EXPORT_TOO_LARGE(HttpStatus.UNPROCESSABLE_CONTENT, "Export exceeds the row limit",
            "Narrow the filter so the export stays under app.export.max-rows.");

    private final HttpStatus status;
    private final String title;
    private final String description;
    private final boolean retryable;

    ErrorCode(HttpStatus status, String title, String description) {
        this(status, title, description, false);
    }

    ErrorCode(HttpStatus status, String title, String description, boolean retryable) {
        this.status = status;
        this.title = title;
        this.description = description;
        this.retryable = retryable;
    }

    /** The stable UPPER_SNAKE_CASE identifier clients branch on. */
    public String code() {
        return name();
    }

    public HttpStatus status() {
        return status;
    }

    /** Short human-readable summary, used as the problem {@code title}. */
    public String title() {
        return title;
    }

    /** Longer guidance for the error catalog. */
    public String description() {
        return description;
    }

    /** Whether a client may retry the same request unchanged. */
    public boolean retryable() {
        return retryable;
    }

    /** Last path segment of the problem {@code type} URI, for example {@code resource-not-found}. */
    public String typeSlug() {
        return name().toLowerCase().replace('_', '-');
    }
}
