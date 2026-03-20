package vn.bxh.jobhunter.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PermissionInterceptorConfiguration implements WebMvcConfigurer {
    @Autowired
    PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] whiteList = {
                "/", "/api/v1/auth/**", "/storage/**",
                 "/api/v1/files","/api/v1/email",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html","/error","/api/v1/subscribers/**","/api/v1/companies/**","/api/v1/jobs/**",
                "/api/v1/resumes/by-user",
                "/api/v1/resumes/my-uploads",
                "/api/v1/resumes","/api/v1/resumes/count-by-job/**","/api/v1/jobs-with-applicants",
                // Job Alerts endpoints bỏ qua kiểm tra permission, vẫn yêu cầu JWT theo Security
                "/api/v1/job-alerts/**",
                // Notifications endpoints bỏ qua kiểm tra permission, vẫn yêu cầu JWT
                "/api/v1/notifications/**",
                "/api/v1/notifications"
        };
        registry.addInterceptor(permissionInterceptor)
                .excludePathPatterns(whiteList);
    }
}
