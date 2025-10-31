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
import vn.bxh.jobhunter.domain.response.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.response.ResLoginDTO;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.util.SecurityUtil;
import vn.bxh.jobhunter.util.anotation.ApiMessage;
import vn.bxh.jobhunter.util.error.IdInvalidException;

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

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,SecurityUtil securityUtil,
                          UserService userService,PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@RequestBody User user) {

        boolean existsEmail = this.userService.existEmail(user.getEmail());
        if (existsEmail == true) {
            throw new IdInvalidException("Email not found!" + user.getEmail());
        }
        String passwordEncode = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncode);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.HandleSaveUser(user));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                reqLoginDTO.getUsername(), reqLoginDTO.getPassword());
        // xác thực người dùng => cần viết hàm loadUserByUsername
        User userDB = this.userService.FindUserByEmail(reqLoginDTO.getUsername());
        if(userDB == null){
            throw new IdInvalidException("Email is not valid!");
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

    @GetMapping("/auth/account")
    @ApiMessage("Fetch Account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount(){
        String email = SecurityUtil.getCurrentUserLogin().isPresent()? SecurityUtil.getCurrentUserLogin().get():"";
        User userDB = this.userService.FindUserByEmail(email);
        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin();

        if(userDB != null){
            userLogin.setId(userDB.getId());
            userLogin.setName(userDB.getName());
            userLogin.setEmail(userDB.getEmail());
            userLogin.setGender(userDB.getGender());
            userLogin.setAddress(userDB.getAddress());
            userLogin.setAge(userDB.getAge());
            userLogin.setRole(userDB.getRole());
            userLogin.setCompany(userDB.getCompany());
        }
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


}
