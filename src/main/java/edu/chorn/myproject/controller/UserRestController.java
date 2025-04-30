package edu.chorn.myproject.controller;

/*
    @author chorn
    @project myproject
    @class UserRestController
    @version 1.0.0
    @since 08.04.2025 - 18.09
*/

import edu.chorn.myproject.model.User;
import edu.chorn.myproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import request.UserCreateRequest;
import request.UserUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("api/v1/users/")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;


    // CRUD   create read update delete

    // read all
    @GetMapping
    public List<User> showAll() {
        return userService.getAll();
    }

    // read one
    @GetMapping("{id}")
    public User showOneById(@PathVariable String id) {
        return userService.getById(id);
    }

    @PostMapping
    public User insert(@RequestBody UserCreateRequest user) {
        return userService.create(user);
    }

    @PutMapping
    public User edit(@RequestBody UserUpdateRequest user) {
        return userService.update(user);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        userService.delById(id);
    }
}