package edu.chorn.myproject.service;

/*
    @author chorn
    @project myproject
    @class UserService
    @version 1.0.0
    @since 08.04.2025 - 17.36
*/

import edu.chorn.myproject.model.User;
import edu.chorn.myproject.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import request.UserCreateRequest;
import request.UserUpdateRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private List<User> userList = new ArrayList<>();
    {
        userList.add(new User("name1", "000001", "description1"));
        userList.add(new User("name2", "000002", "description2"));
        userList.add(new User("name3", "000003", "description3"));
    }

    @PostConstruct
    public void init() {
        userRepository.deleteAll();
        userRepository.saveAll(userList);
    }

    //  CRUD   - create read update delete

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public User create(UserCreateRequest request) {
        User user = mapToUser(request);
        user.setCreateDate(LocalDateTime.now());
        user.setUpdateDate(new ArrayList<LocalDateTime>());
        return userRepository.save(user);
    }

    private User mapToUser(UserCreateRequest request) {
        User user = new User(request.name(), request.code(), request.description());
        return user;
    }

    public User update(UserUpdateRequest request) {
        User userPersisted = userRepository.findById(request.id()).orElse(null);
        if (userPersisted != null) {
            List<LocalDateTime> updateDates = userPersisted.getUpdateDate();
            updateDates.add(LocalDateTime.now());
            User userToUpdate =
                    User.builder()
                            .id(request.id())
                            .name(request.name())
                            .code(request.code())
                            .description(request.description())
                            .createDate(userPersisted.getCreateDate())
                            .updateDate(updateDates)
                            .build();
            return userRepository.save(userToUpdate);

        }
        return null;
    }

    public void delById(String id) {
        userRepository.deleteById(id);
    }
}