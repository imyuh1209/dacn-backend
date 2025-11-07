package vn.bxh.jobhunter.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import vn.bxh.jobhunter.domain.Permission;
import vn.bxh.jobhunter.domain.Role;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.PermissionRepository;
import vn.bxh.jobhunter.repository.RoleRepository;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.util.Constant.GenderEnum;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> START INIT DATABASE");

        long countPermissions = this.permissionRepository.count();
        long countRoles = this.roleRepository.count();
        long countUsers = this.userRepository.count();

        if (countPermissions == 0) {
            List<Permission> arr = new ArrayList<>();

            arr.add(new Permission("Create a company", "/api/v1/companies", "POST", "COMPANIES"));
            arr.add(new Permission("Update a company", "/api/v1/companies", "PUT", "COMPANIES"));
            arr.add(new Permission("Delete a company", "/api/v1/companies/{id}", "DELETE", "COMPANIES"));
            arr.add(new Permission("Get a company by id", "/api/v1/companies/{id}", "GET", "COMPANIES"));
            arr.add(new Permission("Get companies with pagination", "/api/v1/companies", "GET", "COMPANIES"));

            arr.add(new Permission("Create a job", "/api/v1/jobs", "POST", "JOBS"));
            arr.add(new Permission("Update a job", "/api/v1/jobs", "PUT", "JOBS"));
            arr.add(new Permission("Get a job", "/api/v1/jobs/{id}", "GET", "JOBS"));
            arr.add(new Permission("Delete a job", "/api/v1/jobs/{id}", "DELETE", "JOBS"));
            arr.add(new Permission("Get jobs with pagination", "/api/v1/jobs", "GET", "JOBS"));
            arr.add(new Permission("Get jobs by current company", "/api/v1/jobs/by-company", "GET", "JOBS"));

            arr.add(new Permission("Create a permission", "/api/v1/permissions", "POST", "PERMISSIONS"));
            arr.add(new Permission("Update a permission", "/api/v1/permissions", "PUT", "PERMISSIONS"));
            arr.add(new Permission("Delete a permission", "/api/v1/permissions/{id}", "DELETE", "PERMISSIONS"));
            arr.add(new Permission("Get a permission by id", "/api/v1/permissions/{id}", "GET", "PERMISSIONS"));
            arr.add(new Permission("Get permissions with pagination", "/api/v1/permissions", "GET", "PERMISSIONS"));

            arr.add(new Permission("Create a resume", "/api/v1/resumes", "POST", "RESUMES"));
            arr.add(new Permission("Update a resume", "/api/v1/resumes", "PUT", "RESUMES"));
            arr.add(new Permission("Delete a resume", "/api/v1/resumes/{id}", "DELETE", "RESUMES"));
            arr.add(new Permission("Get a resume by id", "/api/v1/resumes/{id}", "GET", "RESUMES"));
            arr.add(new Permission("Get resumes with pagination", "/api/v1/resumes", "GET", "RESUMES"));
            arr.add(new Permission("Send resume status email", "/api/v1/resumes/status-email", "POST", "RESUMES"));

            arr.add(new Permission("Create a role", "/api/v1/roles", "POST", "ROLES"));
            arr.add(new Permission("Update a role", "/api/v1/roles", "PUT", "ROLES"));
            arr.add(new Permission("Delete a role", "/api/v1/roles/{id}", "DELETE", "ROLES"));
            arr.add(new Permission("Get a role by id", "/api/v1/roles/{id}", "GET", "ROLES"));
            arr.add(new Permission("Get roles with pagination", "/api/v1/roles", "GET", "ROLES"));

            arr.add(new Permission("Create a user", "/api/v1/users", "POST", "USERS"));
            arr.add(new Permission("Update a user", "/api/v1/users", "PUT", "USERS"));
            arr.add(new Permission("Delete a user", "/api/v1/users/{id}", "DELETE", "USERS"));
            arr.add(new Permission("Get a user by id", "/api/v1/users/{id}", "GET", "USERS"));
            arr.add(new Permission("Get users with pagination", "/api/v1/users", "GET", "USERS"));
            // Current user info
            arr.add(new Permission("Get current user", "/api/v1/users/me", "GET", "USERS"));

            arr.add(new Permission("Create a subscriber", "/api/v1/subscribers", "POST", "SUBSCRIBERS"));
            arr.add(new Permission("Update a subscriber", "/api/v1/subscribers", "PUT", "SUBSCRIBERS"));
            arr.add(new Permission("Delete a subscriber", "/api/v1/subscribers/{id}", "DELETE", "SUBSCRIBERS"));
            arr.add(new Permission("Get a subscriber by id", "/api/v1/subscribers/{id}", "GET", "SUBSCRIBERS"));
            arr.add(new Permission("Get subscribers with pagination", "/api/v1/subscribers", "GET", "SUBSCRIBERS"));

            arr.add(new Permission("Download a file", "/api/v1/files", "POST", "FILES"));
            arr.add(new Permission("Upload a file", "/api/v1/files", "GET", "FILES"));

            // Saved Job permissions
            arr.add(new Permission("Save a job", "/api/v1/saved-jobs", "POST", "SAVED_JOBS"));
            arr.add(new Permission("List my saved jobs", "/api/v1/saved-jobs", "GET", "SAVED_JOBS"));
            arr.add(new Permission("Delete a saved job by id", "/api/v1/saved-jobs/{id}", "DELETE", "SAVED_JOBS"));
            arr.add(new Permission("Check job saved status", "/api/v1/saved-jobs/is-saved", "GET", "SAVED_JOBS"));

            // Banner permissions (CRUD + listing)
            arr.add(new Permission("Create a banner", "/api/v1/banners", "POST", "BANNERS"));
            arr.add(new Permission("Update a banner", "/api/v1/banners", "PUT", "BANNERS"));
            arr.add(new Permission("Delete a banner", "/api/v1/banners/{id}", "DELETE", "BANNERS"));
            arr.add(new Permission("Get a banner by id", "/api/v1/banners/{id}", "GET", "BANNERS"));
            arr.add(new Permission("Get banners with pagination", "/api/v1/banners", "GET", "BANNERS"));

            permissionRepository.saveAll(arr);
        }

        // Ensure Saved Job permissions exist even if permissions were seeded before
        Permission saveJobPerm = ensurePermission("Save a job", "/api/v1/saved-jobs", "POST", "SAVED_JOBS");
        Permission listSavedPerm = ensurePermission("List my saved jobs", "/api/v1/saved-jobs", "GET", "SAVED_JOBS");
        Permission deleteSavedPerm = ensurePermission("Delete a saved job by id", "/api/v1/saved-jobs/{id}", "DELETE", "SAVED_JOBS");
        Permission isSavedPerm = ensurePermission("Check job saved status", "/api/v1/saved-jobs/is-saved", "GET", "SAVED_JOBS");
        Permission resumeStatusEmailPerm = ensurePermission("Send resume status email", "/api/v1/resumes/status-email", "POST", "RESUMES");
        Permission jobsByCompanyPerm = ensurePermission("Get jobs by current company", "/api/v1/jobs/by-company", "GET", "JOBS");
        // Ensure Banner permissions exist
        Permission bannerCreatePerm = ensurePermission("Create a banner", "/api/v1/banners", "POST", "BANNERS");
        Permission bannerUpdatePerm = ensurePermission("Update a banner", "/api/v1/banners", "PUT", "BANNERS");
        Permission bannerDeletePerm = ensurePermission("Delete a banner", "/api/v1/banners/{id}", "DELETE", "BANNERS");
        Permission bannerGetByIdPerm = ensurePermission("Get a banner by id", "/api/v1/banners/{id}", "GET", "BANNERS");
        Permission bannerListPerm = ensurePermission("Get banners with pagination", "/api/v1/banners", "GET", "BANNERS");
        // Ensure Update User permission exists
        Permission updateUserPerm = ensurePermission("Update a user", "/api/v1/users", "PUT", "USERS");
        // Ensure Get current user permission exists
        Permission getCurrentUserPerm = ensurePermission("Get current user", "/api/v1/users/me", "GET", "USERS");
        if (countRoles == 0) {
            List<Permission> allPermissions = this.permissionRepository.findAll();

            Role adminRole = new Role();
            adminRole.setName("SUPER_ADMIN");
            adminRole.setDescription("Admin thì full permissions");
            adminRole.setActive(true);
            adminRole.setPermissions(allPermissions);

            this.roleRepository.save(adminRole);

            // Default USER role with basic permissions (saved jobs + resumes)
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Quyền cơ bản: lưu job và quản lý resume của mình");
            userRole.setActive(true);
            List<Permission> userPerms = allPermissions.stream()
                    .filter(p -> p.getApiPath().startsWith("/api/v1/saved-jobs")
                            || p.getApiPath().startsWith("/api/v1/resumes"))
                    .collect(Collectors.toList());
            // Add Update User permission so USER can update account
            boolean hasUpdateUser = userPerms.stream().anyMatch(p -> 
                    "/api/v1/users".equals(p.getApiPath()) && "PUT".equalsIgnoreCase(p.getMethod()));
            if (!hasUpdateUser) {
                userPerms.add(updateUserPerm);
            }
            // Add Get current user permission so USER can view own account
            boolean hasGetCurrentUser = userPerms.stream().anyMatch(p ->
                    "/api/v1/users/me".equals(p.getApiPath()) && "GET".equalsIgnoreCase(p.getMethod()));
            if (!hasGetCurrentUser) {
                userPerms.add(getCurrentUserPerm);
            }
            // Allow USER to view banners list and detail
            boolean hasBannerList = userPerms.stream().anyMatch(p ->
                    "/api/v1/banners".equals(p.getApiPath()) && "GET".equalsIgnoreCase(p.getMethod()));
            if (!hasBannerList) {
                userPerms.add(bannerListPerm);
            }
            boolean hasBannerGetById = userPerms.stream().anyMatch(p ->
                    "/api/v1/banners/{id}".equals(p.getApiPath()) && "GET".equalsIgnoreCase(p.getMethod()));
            if (!hasBannerGetById) {
                userPerms.add(bannerGetByIdPerm);
            }
            userRole.setPermissions(userPerms);

            this.roleRepository.save(userRole);
        }

        // Ensure USER role exists and has Saved Job + Resume permissions
        Optional<Role> userRoleOpt = this.roleRepository.findByName("USER");
        List<Permission> basicPerms = this.permissionRepository.findAll().stream()
                .filter(p -> p.getApiPath().startsWith("/api/v1/saved-jobs")
                        || p.getApiPath().startsWith("/api/v1/resumes"))
                .collect(Collectors.toList());
        // Ensure Update User permission is included in USER basic permissions
        boolean basicHasUpdateUser = basicPerms.stream().anyMatch(p -> 
                "/api/v1/users".equals(p.getApiPath()) && "PUT".equalsIgnoreCase(p.getMethod()));
        if (!basicHasUpdateUser) {
            basicPerms.add(updateUserPerm);
        }
        // Ensure Get current user permission is included in USER basic permissions
        boolean basicHasGetCurrentUser = basicPerms.stream().anyMatch(p ->
                "/api/v1/users/me".equals(p.getApiPath()) && "GET".equalsIgnoreCase(p.getMethod()));
        if (!basicHasGetCurrentUser) {
            basicPerms.add(getCurrentUserPerm);
        }
        // Ensure USER basic permissions include viewing banners
        boolean basicHasBannerList = basicPerms.stream().anyMatch(p ->
                "/api/v1/banners".equals(p.getApiPath()) && "GET".equalsIgnoreCase(p.getMethod()));
        if (!basicHasBannerList) {
            basicPerms.add(bannerListPerm);
        }
        boolean basicHasBannerGetById = basicPerms.stream().anyMatch(p ->
                "/api/v1/banners/{id}".equals(p.getApiPath()) && "GET".equalsIgnoreCase(p.getMethod()));
        if (!basicHasBannerGetById) {
            basicPerms.add(bannerGetByIdPerm);
        }
        if (userRoleOpt.isPresent()) {
            Role userRole = userRoleOpt.get();
            List<Permission> current = userRole.getPermissions() != null ? userRole.getPermissions() : new ArrayList<>();
            for (Permission p : basicPerms) {
                boolean exists = current.stream().anyMatch(cp -> cp.getId() == p.getId());
                if (!exists) current.add(p);
            }
            userRole.setPermissions(current);
            this.roleRepository.save(userRole);
        } else {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Quyền cơ bản: lưu job và quản lý resume của mình");
            userRole.setActive(true);
            userRole.setPermissions(basicPerms);
            this.roleRepository.save(userRole);
        }

        // Ensure SUPER_ADMIN has all permissions (including newly added)
        Optional<Role> adminRoleOpt = this.roleRepository.findByName("SUPER_ADMIN");
        if (adminRoleOpt.isPresent()) {
            Role adminRole = adminRoleOpt.get();
            adminRole.setPermissions(this.permissionRepository.findAll());
            this.roleRepository.save(adminRole);
        }

        // Ensure COMPANY role (or Vietnamese 'Công ty') has permission to get jobs by current company
        Optional<Role> companyRoleOpt = this.roleRepository.findByName("COMPANY");
        if (!companyRoleOpt.isPresent()) {
            companyRoleOpt = this.roleRepository.findByName("Công ty");
        }
        if (companyRoleOpt.isPresent()) {
            Role companyRole = companyRoleOpt.get();
            List<Permission> current = companyRole.getPermissions() != null ? companyRole.getPermissions() : new ArrayList<>();
            boolean exists = current.stream().anyMatch(p -> 
                    "/api/v1/jobs/by-company".equals(p.getApiPath()) && "GET".equalsIgnoreCase(p.getMethod()));
            if (!exists) current.add(jobsByCompanyPerm);
            companyRole.setPermissions(current);
            this.roleRepository.save(companyRole);
        }

        // Ensure ALL roles have Saved Job permissions
        List<Permission> savedJobPerms = this.permissionRepository.findAll().stream()
                .filter(p -> p.getApiPath().startsWith("/api/v1/saved-jobs"))
                .collect(Collectors.toList());
        List<Role> allRoles = this.roleRepository.findAll();
        for (Role role : allRoles) {
            List<Permission> current = role.getPermissions() != null ? role.getPermissions() : new ArrayList<>();
            for (Permission p : savedJobPerms) {
                boolean exists = current.stream().anyMatch(cp -> cp.getId() == p.getId());
                if (!exists) current.add(p);
            }
            role.setPermissions(current);
            this.roleRepository.save(role);
        }
        if (countUsers == 0) {
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setAddress("hn");
            adminUser.setAge(21);
            adminUser.setGender(GenderEnum.MALE);
            adminUser.setName("I'm super admin");
            adminUser.setPassword(this.passwordEncoder.encode("123"));
            Optional<Role> adminRole = this.roleRepository.findByName("SUPER_ADMIN");
            adminRole.ifPresent(adminUser::setRole);
            this.userRepository.save(adminUser);
        }
    }

    private Permission ensurePermission(String name, String apiPath, String method, String module) {
        return this.permissionRepository
                .findByApiPathAndMethod(apiPath, method)
                .orElseGet(() -> this.permissionRepository.save(new Permission(name, apiPath, method, module)));
    }
}
