package com.fourati.app.config;

import java.util.List;

import com.fourati.platform.web.SunsetHeadersInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration: pagination argument resolvers and the deprecation
 * headers for endpoints marked {@code @SunsetEndpoint}.
 *
 * CORS is configured in {@code SecurityConfig} (as a CorsConfigurationSource wired
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SunsetHeadersInterceptor());
    }
}
