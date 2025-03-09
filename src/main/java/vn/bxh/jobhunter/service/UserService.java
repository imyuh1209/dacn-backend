package vn.bxh.jobhunter.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.bxh.jobhunter.domain.dto.Meta;
import vn.bxh.jobhunter.domain.dto.ResCreateUserDTO;
import vn.bxh.jobhunter.domain.User;
import vn.bxh.jobhunter.domain.dto.ResUserDTO;
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


    public ResultPaginationDTO HandleFindAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> usePage = this.userRepository.findAll(spec,pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        Meta meta = new Meta(usePage.getNumber()+1, usePage.getSize(), usePage.getTotalPages(), usePage.getTotalElements());
        resultPaginationDTO.setMeta(meta);

        List<ResUserDTO> resUserDTOList = usePage.getContent().stream().map(item -> new ResUserDTO(
                item.getId(),
                item.getEmail(),
                item.getName(),
                item.getGender(),
                item.getAddress(),
                item.getAge(),
                item.getUpdatedAt(),
                item.getCreatedAt())).toList();
        resultPaginationDTO.setResult(resUserDTOList);

        return resultPaginationDTO;
    }

    public User HandleUpdateUser(User user) {
        User newUser = this.userRepository.findById(user.getId()).get();
        if (newUser != null) {
            newUser.setName(user.getName());
            newUser.setEmail(user.getEmail());
            newUser.setAge(user.getAge());
            newUser.setGender(user.getGender());
            return newUser;
        }

        return null;
    }

    public User HandleSetFreshToken(String email, String refresh_token){
        User user = this.FindUserByEmail(email);
        if(user!=null){
            user.setRefreshToken(refresh_token);
            return this.HandleSaveUser(user);
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
    public ResUserDTO convertToResUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();
        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setCreatedAt(user.getCreatedAt());
        res.setGender(user.getGender());
        res.setAddress(user.getAddress());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }



    public User FindUserByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public boolean existEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public User FindByEmailAndRefreshToken(String email,String token){
        return this.userRepository.findByEmailAndRefreshToken(email, token);
    }
}
