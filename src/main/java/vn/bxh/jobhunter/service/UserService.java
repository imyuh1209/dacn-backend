package vn.bxh.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.bxh.jobhunter.domain.dto.Meta;
import vn.bxh.jobhunter.domain.dto.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.dto.ResultPaginationDTO;
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

    public ResultPaginationDTO HandleFindAllUsers(Pageable pageable) {
        Page<User> companyPage = this.userRepository.findAll(pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta(companyPage.getNumber(), companyPage.getSize(), companyPage.getTotalPages(), companyPage.getTotalElements());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(companyPage.getContent());
        return resultPaginationDTO;
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

    public ResCreateUserDTO convertToResCreateUserDTO(User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());

        return res;
    }


    public User FindUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public boolean existEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

}
