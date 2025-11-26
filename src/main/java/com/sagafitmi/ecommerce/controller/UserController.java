package com.sagafitmi.ecommerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sagafitmi.ecommerce.dto.AuthRequestDTO;
import com.sagafitmi.ecommerce.dto.UserCreateDTO;
import com.sagafitmi.ecommerce.dto.UserDTO;
import com.sagafitmi.ecommerce.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO dto = userService.getUserById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateDTO createDTO) {
        UserDTO created = userService.createUser(createDTO);
        if (created == null) {
            // could be conflict (email exists) or bad request
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUser(id, userDTO);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> getByEmail(@RequestParam String email) {
        UserDTO dto = userService.findByEmail(email);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    // @PostMapping("/authenticate")
    // public ResponseEntity<Void> authenticate(@RequestBody AuthRequestDTO auth) {
    //     if (auth == null || auth.getEmail() == null || auth.getPassword() == null) {
    //         return ResponseEntity.badRequest().build();
    //     }
    //     boolean ok = userService.authenticate(auth.getEmail(), auth.getPassword());
    //     if (!ok) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    //     return ResponseEntity.ok().build();
    // }

}
