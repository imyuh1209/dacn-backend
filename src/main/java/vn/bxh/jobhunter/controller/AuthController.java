package vn.bxh.jobhunter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.dto.LoginDTO;
import vn.bxh.jobhunter.domain.dto.ResLoginDTO;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.util.SecurityUtil;

@RestController
public class AuthController {
    private final SecurityUtil securityUtil;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,SecurityUtil securityUtil,UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDTO.getUsername(), loginDTO.getPassword());
        // xác thực người dùng => cần viết hàm loadUserByUsername
        User userDB = this.userService.FindUserByEmail(loginDTO.getUsername());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);//set thong tin ngươi dùng
        String access_token = this.securityUtil.createToken(authentication);
        ResLoginDTO.UserLogin  userLogin = new ResLoginDTO.UserLogin();
        userLogin.setId(userDB.getId());
        userLogin.setName(userDB.getName());
        userLogin.setEmail(userDB.getEmail());
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        resLoginDTO.setUser(userLogin);
        resLoginDTO.setAccessToken(access_token);
        return ResponseEntity.ok().body(resLoginDTO);
    }
}
