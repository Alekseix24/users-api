package com.chakray.users_api.controller;

import com.chakray.users_api.model.LoginRequest;
import com.chakray.users_api.model.User;
import com.chakray.users_api.service.UserService;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers(
            @RequestParam(required = false) String sortedBy,
            @RequestParam(required = false) String filter
    ) {
        return userService.getUsers(sortedBy, filter);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PatchMapping("/{id}")
    public User updateUser(
            @PathVariable UUID id,
            @RequestBody User user
    ) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest loginRequest) {

        return userService.login(
                loginRequest.getTaxId(),
                loginRequest.getPassword()
        );
    }


}
