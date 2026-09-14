/**
 * Content domain: tags, entity tags, comments and attachments.
 *
 * <p>Public API (this package): the request and response records and
 * {@link com.fourati.content.ContentFacade}. Everything under {@code internal}
 * (entities, repositories, use cases, controllers) is private to the module;
 * {@code ModularityTest} fails the build on an outside import (ADR 0004).
 */
@ApplicationModule(displayName = "Content")
package com.fourati.content;

import org.springframework.modulith.ApplicationModule;
