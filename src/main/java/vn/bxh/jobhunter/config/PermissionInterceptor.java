package vn.bxh.jobhunter.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import vn.bxh.jobhunter.domain.Permission;
import vn.bxh.jobhunter.domain.Role;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.error.IdInvalidException;

import java.util.List;

public class PermissionInterceptor implements HandlerInterceptor {
    @Autowired
    UserRepository userRepository;
    @Override
    @Transactional
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response, Object handler)
            throws Exception {
        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        String email = SecurityUtil.getCurrentUserLogin().isPresent()?SecurityUtil.getCurrentUserLogin().get():"";
        User user = this.userRepository.findByEmail(email);
        if(user != null){
            Role role = user.getRole();
            if(role != null){
                List<Permission> permissions = role.getPermissions();
                boolean isAllow = permissions.stream().anyMatch(item -> item.getApiPath().equals(path)
                        && item.getMethod().equals(httpMethod));
                return isAllow;
            }
            throw new IdInvalidException("Bạn không có quyền truy cập");
        }else{
            throw new IdInvalidException("Bạn không có quyền truy cập");
        }
    }
}