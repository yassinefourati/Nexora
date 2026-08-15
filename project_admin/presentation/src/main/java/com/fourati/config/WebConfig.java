package com.fourati.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration: pagination argument resolvers.
 *
 * CORS is configured in {@link SecurityConfig} (as a CorsConfigurationSource wired
 * into the security filter chain), not here — Spring Security's filter chain runs
 * before DispatcherServlet, so an MVC-level CorsRegistry mapping never even sees a
 * cross-origin preflight that Security's authorizeHttpRequests() already rejected.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        SortHandlerMethodArgumentResolver sortResolver = new SortHandlerMethodArgumentResolver();
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver(sortResolver);
        pageableResolver.setMaxPageSize(100);
        resolvers.add(sortResolver);
        resolvers.add(pageableResolver);
    }
}
