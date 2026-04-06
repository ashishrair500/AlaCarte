package com.alaCarte.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RestaurantAdminRequestInterceptor restaurantAdminRequestInterceptor;

    public WebMvcConfig(RestaurantAdminRequestInterceptor restaurantAdminRequestInterceptor) {
        this.restaurantAdminRequestInterceptor = restaurantAdminRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(restaurantAdminRequestInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
