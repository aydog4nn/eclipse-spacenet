package org.dev.eclipse.config;

import org.dev.eclipse.security.SwaggerEndpointBlockFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

@Configuration
@Profile("prod")
public class SwaggerEndpointFilterConfig {

    @Bean
    public FilterRegistrationBean<SwaggerEndpointBlockFilter> swaggerEndpointBlockFilterRegistration(SwaggerEndpointBlockFilter swaggerEndpointBlockFilter) {
        FilterRegistrationBean<SwaggerEndpointBlockFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(swaggerEndpointBlockFilter);
        registration.addUrlPatterns("/swagger-ui/*", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/*");
        registration.setName("swaggerEndpointBlockFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
