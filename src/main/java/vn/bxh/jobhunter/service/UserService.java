package vn.bxh.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User HandleSaveUser(User user) {
        return this.userRepository.save(user);
    }

    public void HandleDeleteUser(Long id) {
        this.userRepository.deleteById(id);
    }

    public Optional<User> HandleFetchUserById(Long id) {
        return this.userRepository.findById(id);
    }

    public List<User> HandleFindAllUsers() {
        return this.userRepository.findAll();
    }

    public User HandleUpdateUser(User user) {
        User newUser = this.userRepository.findById(user.getId()).get();
        if (newUser != null) {
            newUser.setName(user.getName());
            newUser.setEmail(user.getEmail());
            newUser.setPassword(user.getPassword());
            return newUser;
        }
        return null;
    }

    public User FindUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }
}
