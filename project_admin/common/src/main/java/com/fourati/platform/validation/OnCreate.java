package com.fourati.platform.validation;

import jakarta.validation.groups.Default;

/**
 * Validation group for CREATE operations.
 *
 * Use @Validated(OnCreate.class) on the controller method instead of @Valid.
 * Extends Default so non-grouped constraints still apply.
 *
 * Example DTO:
 *   public record CreateItemRequest(
 *       @NotNull(groups = OnCreate.class)  // required only on create
 *       @Null(groups = OnUpdate.class)     // must be absent on update
 *       UUID externalId,
 *
 *       @NotBlank  // no group = applies to both (via Default)
 *       String name
 *   ) {}
 *
 * Controller:
 *   @PostMapping
 *   public ItemResponse create(@Validated(OnCreate.class) @RequestBody CreateItemRequest req) { ... }
 *
 *   @PutMapping("/{id}")
 *   public ItemResponse update(@Validated(OnUpdate.class) @RequestBody UpdateItemRequest req) { ... }
 */
public interface OnCreate extends Default {
	
}
