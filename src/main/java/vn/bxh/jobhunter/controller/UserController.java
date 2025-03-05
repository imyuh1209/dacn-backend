package vn.bxh.jobhunter.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.dto.ResCreateUserDTO;
import vn.bxh.jobhunter.repository.UserRepository;
import vn.bxh.jobhunter.service.UserService;
import vn.bxh.jobhunter.util.error.IdInvalidException;

@RestController
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserController(UserService userService, PasswordEncoder passwordEncoder,UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public ResponseEntity<User> createNewUser(@RequestBody User user) {

        boolean existsEmail = this.userService.existEmail(user.getEmail());
        if (existsEmail == true) {
            throw new IdInvalidException("Email not found!" + user.getEmail());
        }
        String passwordEncode = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordEncode);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.HandleSaveUser(user));
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateNewUser(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.HandleUpdateUser(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if(!user.isPresent()){
            throw new IdInvalidException("Id not exists!");
        }
        this.userService.HandleDeleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ResCreateUserDTO> FetchUserById(@PathVariable Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if(!user.isPresent()){
            throw new IdInvalidException("Id not exists!");
        }
        return ResponseEntity.ok(this.userService.convertToResCreateUserDTO(user.get()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> FetchAllUser(@RequestParam("current") Optional<String> currentOptional,
                                                   @RequestParam("pageSize") Optional<String> pageSizeOptional) {
        String sCurrent = currentOptional.orElse("");
        String sPageSize = pageSizeOptional.orElse("");
        int start = Integer.parseInt(sCurrent);
        int end = Integer.parseInt(sPageSize);
        Pageable pageable = PageRequest.of(start -1 , end );
        return ResponseEntity.ok(this.userService.HandleFindAllUsers());
    }

}
