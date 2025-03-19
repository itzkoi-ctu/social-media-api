package com.ct113h.social_media_api.controller;


import com.ct113h.social_media_api.model.LoginDto;
import com.ct113h.social_media_api.model.LoginResponse;
import com.ct113h.social_media_api.model.User;
import com.ct113h.social_media_api.model.UserDTO;
import com.ct113h.social_media_api.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("${api.prefix}/user")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<User> register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false)MultipartFile profilePhoto
            ) throws IOException {
        UserDTO registerUserDto = new UserDTO(name, email, password, profilePhoto);
        User registerUser = userService.register(registerUserDto);
        return ResponseEntity.ok(registerUser);
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginDto loginUserDto){
        var loginResponse = userService.authenticate(loginUserDto);
        return ResponseEntity.ok(loginResponse);
    }
}
