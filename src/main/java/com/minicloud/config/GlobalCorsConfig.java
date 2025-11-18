package com.minicloud.config;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GlobalCorsConfig {
    // Disabled - using CorsFilter instead to avoid duplicate headers
    // @Bean
    // public WebMvcConfigurer corsConfigurer() {
    //     return new WebMvcConfigurer() {
    //         @Override
    //         public void addCorsMappings(CorsRegistry registry) {
    //             registry.addMapping("/**")
    //                 .allowedOrigins("*")
    //                 .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS", "PATCH")
    //                 .allowedHeaders("*")
    //                 .exposedHeaders("*")
    //                 .allowCredentials(false)
    //                 .maxAge(3600);
    //         }
    //     };
    // }
}
