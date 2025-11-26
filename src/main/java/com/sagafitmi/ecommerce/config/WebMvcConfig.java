package com.sagafitmi.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);

    @Value("${PRODUCT_IMAGE_BASE_PATH:C:/imgs}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (basePath == null || basePath.isBlank()) return;
        String path = basePath.replace('\\', '/');
        if (!path.endsWith("/")) path = path + "/";
        // Spring accepts locations like "file:C:/imgs/"
        String location = "file:" + path;
        logger.info("Mapping /images/** to " + location);
        registry.addResourceHandler("/images/**").addResourceLocations(location);
    }
}
