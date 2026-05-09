package com.example.demo.controller;

import com.example.demo.Entitty.Users;
import com.example.demo.enums.Role;
import com.example.demo.service.AuthService.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/Api/Auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @GetMapping("/health")
    public String threadInfo() {
        Thread t = Thread.currentThread();
        return "Thread: " + t.getName() + " | Virtual: " + t.isVirtual();
    }

    @PostMapping("/User/register")
    public ResponseEntity<Users> registerUser(@RequestBody Users user) {
        user.setRole(Role.USER);
        Users registeredUser = authService.registerUser(user);
        return ResponseEntity.status(201).body(registeredUser);
    }
    @PostMapping("/Admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Users> registerAdmin(@RequestBody Users user) {
        user.setRole(Role.ADMIN);
        Users registeredUser = authService.registerUser(user);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody Users user) {
        String authToken = authService.verify(user);
        return ResponseEntity.ok(authToken);
    }
}