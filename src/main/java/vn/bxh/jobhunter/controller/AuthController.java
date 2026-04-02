package vn.bxh.jobhunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.request.ReqLoginDTO;
import vn.bxh.jobhunter.domain.request.ReqRefreshTokenDTO;
import vn.bxh.jobhunter.domain.request.ReqForgotPassword;
import vn.bxh.jobhunter.domain.request.ReqRegisterDTO;
import vn.bxh.jobhunter.domain.request.ReqResetPassword;
import vn.bxh.jobhunter.domain.response.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.response.ResLoginDTO;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.anotation.ApiMessage;
import vn.bxh.jobhunter.util.error.IdInvalidException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    @Value("${hao.jwt.refresh-token-validity-in-seconds}")
    private long RefreshTokenExpiration;
    @Value("${hao.cookie.secure:false}")
    private boolean cookieSecure;
    @Value("${google.client-id:}")
    private String googleClientId;
    @Value("${google.allowed-client-ids:}")
    private String googleAllowedClientIds;
    @Value("${google.require-email-verified:true}")
    private boolean googleRequireEmailVerified;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,SecurityUtil securityUtil,
                          UserService userService,PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody ReqRegisterDTO postManUser) {

        boolean existsEmail = this.userService.existEmail(postManUser.getEmail());
        if (existsEmail == true) {
            throw new IdInvalidException("Email " + postManUser.getEmail() + " đã tồn tại!");
        }
        User user = new User();
        user.setName(postManUser.getName());
        user.setEmail(postManUser.getEmail());
        user.setPassword(passwordEncoder.encode(postManUser.getPassword()));
        user.setAddress("Unknown"); // Default address to avoid validation error
        user.setAge(18); // Default age to avoid validation error

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.HandleSaveUser(user));
    }

    @PostMapping("/auth/forgot-password")
    @ApiMessage("Yêu cầu đặt lại mật khẩu")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ReqForgotPassword req) {
        this.userService.initiateForgotPassword(req.getEmail());
        return ResponseEntity.ok(null);
    }

    @PostMapping("/auth/reset-password")
    @ApiMessage("Đặt lại mật khẩu")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ReqResetPassword req) {
        String encodedPassword = passwordEncoder.encode(req.getNewPassword());
        User user = this.userService.verifyAndResetPassword(req.getToken(), encodedPassword);
        if (user == null) {
            throw new IdInvalidException("Token không hợp lệ hoặc đã hết hạn");
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                reqLoginDTO.getUsername(), reqLoginDTO.getPassword());
        // xác thực người dùng => cần viết hàm loadUserByUsername
        User userDB = this.userService.FindUserByEmail(reqLoginDTO.getUsername());
        if(userDB == null){
            throw new IdInvalidException("Email không hợp lệ!");
        }
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);//set thong tin ngươi dùng
        ResLoginDTO.UserLogin  userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDB.getId());
        userLogin.setName(userDB.getName());
        userLogin.setEmail(userDB.getEmail());
        userLogin.setGender(userDB.getGender());
        userLogin.setAddress(userDB.getAddress());
        userLogin.setAge(userDB.getAge());
        userLogin.setRole(userDB.getRole());
        userLogin.setCompany(userDB.getCompany());
        //

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userLogin);
        String access_token = this.securityUtil.createAccessToken(authentication.getName(),resLoginDTO);
        resLoginDTO.setAccessToken(access_token);
        String refresh_token = this.securityUtil.createRefreshToken(reqLoginDTO.getUsername(),resLoginDTO);
        this.userService.HandleSetFreshToken(reqLoginDTO.getUsername(), refresh_token);
        ResponseCookie springCookie = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(RefreshTokenExpiration)
                .build();
        resLoginDTO.setRefreshToken(refresh_token);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).body(resLoginDTO);
    }

    @PostMapping("/auth/google")
    @ApiMessage("Đăng nhập bằng Google")
    public ResponseEntity<ResLoginDTO> loginWithGoogle(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            throw new IdInvalidException("idToken is required");
        }
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" +
                    URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                String resBody = response.body();
                throw new IdInvalidException("Invalid Google token: " + (resBody == null ? "" : resBody));
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.body());

            String aud = node.has("aud") ? node.get("aud").asText() : null;
            // Hỗ trợ nhiều client-id (web/android/ios) qua cấu hình comma-separated
            if (aud == null || aud.isBlank()) {
                throw new IdInvalidException("Token audience not found");
            }
            java.util.Set<String> allowSet = new java.util.HashSet<>();
            if (googleAllowedClientIds != null && !googleAllowedClientIds.isBlank()) {
                for (String s : googleAllowedClientIds.split(",")) {
                    String v = s.trim();
                    if (!v.isEmpty()) allowSet.add(v);
                }
            }
            if (googleClientId != null && !googleClientId.isBlank()) {
                allowSet.add(googleClientId.trim());
            }
            if (!allowSet.isEmpty() && !allowSet.contains(aud)) {
                throw new IdInvalidException("Token audience mismatch: " + aud);
            }
            String email = node.has("email") ? node.get("email").asText() : null;
            if (email == null || email.isBlank()) {
                throw new IdInvalidException("Email not found in Google token");
            }
            String emailVerified = node.has("email_verified") ? node.get("email_verified").asText() : null;
            if (googleRequireEmailVerified && emailVerified != null && !emailVerified.equalsIgnoreCase("true")) {
                throw new IdInvalidException("Email not verified by Google");
            }
            String name = node.has("name") ? node.get("name").asText() : email;

            User userDB = this.userService.FindUserByEmail(email);
            if (userDB == null) {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(name);
                // Set default address to avoid validation error
                newUser.setAddress("Unknown");
                newUser.setAge(18);
                // tạo mật khẩu ngẫu nhiên vì đăng nhập bằng Google không dùng password
                String randomPassword = java.util.UUID.randomUUID().toString();
                newUser.setPassword(passwordEncoder.encode(randomPassword));
                this.userService.HandleSaveUser(newUser);
                userDB = this.userService.FindUserByEmail(email);
            }

            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
            userLogin.setId(userDB.getId());
            userLogin.setName(userDB.getName());
            userLogin.setEmail(userDB.getEmail());
            userLogin.setGender(userDB.getGender());
            userLogin.setAddress(userDB.getAddress());
            userLogin.setAge(userDB.getAge());
            userLogin.setRole(userDB.getRole());
            userLogin.setCompany(userDB.getCompany());

            ResLoginDTO resLoginDTO = new ResLoginDTO();
            resLoginDTO.setUser(userLogin);
            String access_token = this.securityUtil.createAccessToken(email, resLoginDTO);
            resLoginDTO.setAccessToken(access_token);
            String refresh_token = this.securityUtil.createRefreshToken(email, resLoginDTO);
            this.userService.HandleSetFreshToken(email, refresh_token);
            ResponseCookie springCookie = ResponseCookie.from("refresh_token", refresh_token)
                    .httpOnly(true)
                    .secure(cookieSecure)
                    .path("/")
                    .maxAge(RefreshTokenExpiration)
                    .build();
            resLoginDTO.setRefreshToken(refresh_token);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).body(resLoginDTO);
        } catch (IdInvalidException e) {
            throw e;
        } catch (Exception e) {
            throw new IdInvalidException("Google login failed: " + e.getMessage());
        }
    }

    @PostMapping("/auth/google/inspect")
    @ApiMessage("Inspect Google idToken")
    public ResponseEntity<Map<String, Object>> inspectGoogleToken(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            throw new IdInvalidException("idToken is required");
        }
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" +
                    URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> res = new java.util.HashMap<>();
            res.put("status", response.statusCode());
            if (response.statusCode() == 200) {
                JsonNode node = mapper.readTree(response.body());
                res.put("aud", node.has("aud") ? node.get("aud").asText() : null);
                res.put("email", node.has("email") ? node.get("email").asText() : null);
                res.put("email_verified", node.has("email_verified") ? node.get("email_verified").asText() : null);
                res.put("exp", node.has("exp") ? node.get("exp").asText() : null);
            } else {
                res.put("error", response.body());
            }
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            throw new IdInvalidException("Inspect failed: " + e.getMessage());
        }
    }

    @GetMapping("/auth/account")
    @ApiMessage("Fetch Account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount(){
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new vn.bxh.jobhunter.util.error.IdInvalidException("Not authenticated"));

        User userDB = this.userService.FindUserByEmail(email);
        if (userDB == null) {
            throw new vn.bxh.jobhunter.util.error.IdInvalidException("User not found");
        }

        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDB.getId());
        userLogin.setName(userDB.getName());
        userLogin.setEmail(userDB.getEmail());
        userLogin.setGender(userDB.getGender());
        userLogin.setAddress(userDB.getAddress());
        userLogin.setAge(userDB.getAge());
        userLogin.setRole(userDB.getRole());
        userLogin.setCompany(userDB.getCompany());
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();
        userGetAccount.setUser(userLogin);
        return ResponseEntity.ok(userGetAccount);
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name="refresh_token", required = false) String refreshCookie,
            @RequestHeader(name = "X-Refresh-Token", required = false) String refreshHeader
    ){
        String refresh_token = (refreshCookie != null && !refreshCookie.isBlank()) ? refreshCookie : refreshHeader;
        if (refresh_token == null || refresh_token.isBlank()) {
            throw new IdInvalidException("Refresh token is required");
        }
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();
        User userDB = this.userService.FindByEmailAndRefreshToken(email,refresh_token);
        if(userDB == null){
            throw new IdInvalidException("User not found by token and email");
        }

        ResLoginDTO.UserLogin  userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDB.getId());
        userLogin.setName(userDB.getName());
        userLogin.setEmail(userDB.getEmail());
        userLogin.setGender(userDB.getGender());
        userLogin.setAddress(userDB.getAddress());
        userLogin.setAge(userDB.getAge());
        userLogin.setRole(userDB.getRole());
        userLogin.setCompany(userDB.getCompany());

        //


        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userLogin);
        String access_token = this.securityUtil.createAccessToken(email,resLoginDTO);
        resLoginDTO.setAccessToken(access_token);
        String new_refresh_token = this.securityUtil.createRefreshToken(email,resLoginDTO);
        this.userService.HandleSetFreshToken(email, new_refresh_token);
        ResponseCookie springCookie = ResponseCookie.from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(RefreshTokenExpiration)
                .build();
        resLoginDTO.setAccessToken(access_token);
        resLoginDTO.setRefreshToken(new_refresh_token);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).body(resLoginDTO);
    }

    @PostMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token (body)")
    public ResponseEntity<ResLoginDTO> refreshTokenByBody(@Valid @RequestBody ReqRefreshTokenDTO req) {
        String refresh_token = req.getRefreshToken();
        if (refresh_token == null || refresh_token.isBlank()) {
            throw new IdInvalidException("Refresh token is required");
        }
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);
        String email = decodedToken.getSubject();
        User userDB = this.userService.FindByEmailAndRefreshToken(email, refresh_token);
        if (userDB == null) {
            throw new IdInvalidException("User not found by token and email");
        }

        ResLoginDTO.UserLogin  userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDB.getId());
        userLogin.setName(userDB.getName());
        userLogin.setEmail(userDB.getEmail());
        userLogin.setGender(userDB.getGender());
        userLogin.setAddress(userDB.getAddress());
        userLogin.setAge(userDB.getAge());
        userLogin.setRole(userDB.getRole());
        userLogin.setCompany(userDB.getCompany());

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userLogin);
        String access_token = this.securityUtil.createAccessToken(email,resLoginDTO);
        String new_refresh_token = this.securityUtil.createRefreshToken(email,resLoginDTO);
        this.userService.HandleSetFreshToken(email, new_refresh_token);
        ResponseCookie springCookie = ResponseCookie.from("refresh_token", new_refresh_token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(RefreshTokenExpiration)
                .build();
        resLoginDTO.setAccessToken(access_token);
        resLoginDTO.setRefreshToken(new_refresh_token);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, springCookie.toString()).body(resLoginDTO);
    }

    @PostMapping("/auth/logout")
    @ApiMessage("Đăng xuất thành công!")
    public ResponseEntity<Void> logoutOut(){
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        String email = authentication.getName();
        this.userService.HandleSetFreshToken(email,null);
        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .build();
    }

    @PostMapping("/auth/change-password")
    @ApiMessage("Đổi mật khẩu thành công")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody vn.bxh.jobhunter.domain.request.ReqChangePasswordDTO req) {
        String email = vn.bxh.jobhunter.util.SecurityUtil.getCurrentUserLogin().orElse("");
        if (email.isBlank()) {
            throw new vn.bxh.jobhunter.util.error.IdInvalidException("Không xác định được người dùng hiện tại");
        }
        vn.bxh.jobhunter.domain.User userDB = this.userService.FindUserByEmail(email);
        if (userDB == null) {
            throw new vn.bxh.jobhunter.util.error.IdInvalidException("Không tìm thấy người dùng");
        }
        if (!passwordEncoder.matches(req.getCurrentPassword(), userDB.getPassword())) {
            throw new vn.bxh.jobhunter.util.error.IdInvalidException("Mật khẩu hiện tại không chính xác");
        }
        if (req.getNewPassword().equals(req.getCurrentPassword())) {
            throw new vn.bxh.jobhunter.util.error.IdInvalidException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }

        String encoded = passwordEncoder.encode(req.getNewPassword());
        this.userService.HandleChangePassword(email, encoded);
        return ResponseEntity.ok().build();
    }


}
