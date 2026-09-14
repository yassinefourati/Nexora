package com.fourati.platform.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field-level annotation that rejects SQL injection and XSS patterns.
 * Use on any String field in a request DTO.
 *
 * Screening happens at DTO validation time, so the rejection surfaces as a
 * normal 400 field violation with the offending field name.
 *
 * Example:
 *   public record CreateItemRequest(
 *       @SafeInput @NotBlank @Size(max = 255) String name,
 *       @SafeInput @Size(max = 1000)          String description
 *   ) {}
 *
 * If injection is detected, the response is a standard 400 validation error:
 *   { "fieldErrors": { "name": ["Input contains potentially dangerous characters"] } }
 *
 * @param allowHtml  set true to skip XSS checks (e.g. rich-text editor fields)
 * @param allowSql   set true to skip SQL checks (almost never needed)
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeInputValidator.class)
public @interface SafeInput {

	String message() default "Input contains potentially dangerous characters";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean allowHtml() default false;

	boolean allowSql() default false;
}
