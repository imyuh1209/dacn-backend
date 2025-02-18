package vn.bxh.jobhunter.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.service.UserService;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public User createNewUser(@RequestBody User user) {
        User newUser = this.userService.HandleSaveUser(user);
        return newUser;
    }

    @PutMapping("/user")
    public User updateNewUser(@RequestBody User user) {
        return this.userService.HandleUpdateUser(user);
    }

    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        this.userService.HandleDeleteUser(id);
        return "hao- hungmin";
    }

    @GetMapping("/user/{id}")
    public User FetchUserById(@PathVariable Long id) {
        return this.userService.HandleFetchUserById(id).get();
    }

    @GetMapping("/user")
    public List<User> FetchAllUser() {
        return this.userService.HandleFindAllUsers();
    }

}
