package com.vku.lanmonitor.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ URL /images/** tới thư mục images/ (lưu ảnh chụp thủ công)
        String imagesPath = new File("images").getAbsolutePath();
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesPath + "/");

        // Ánh xạ URL /temp_images/** tới thư mục temp_images/ (lưu ảnh tự động)
        String tempImagesPath = new File("temp_images").getAbsolutePath();
        registry.addResourceHandler("/temp_images/**")
                .addResourceLocations("file:" + tempImagesPath + "/");
    }
}