package com.example.whucs_mentorguide.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // 允许的前端地址列表（推荐使用模式匹配）
                        .allowedOriginPatterns(
                                "http://localhost:9876"
//                                "http://127.0.0.1:8080",
//                                "https://your-production-domain.com"
                        )
                        // 允许的 HTTP 方法
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        // 允许的请求头（包含自定义头部）
                        .allowedHeaders("*")
                        // 是否允许携带凭证（cookies等）
                        .allowCredentials(true)
                        // 预检请求缓存时间（秒）
                        .maxAge(3600);
            }
        };
    }
}