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
 * Works alongside the global SqlInjectionFilter — the filter protects at the HTTP
 * boundary, this annotation protects at the object binding layer (deeper, DTO-specific).
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
