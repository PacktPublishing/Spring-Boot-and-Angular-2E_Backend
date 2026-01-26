package com.packt.bookstore.users.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.packt.bookstore.users.dto.SignInRequest;
import com.packt.bookstore.users.dto.SignInResponse;
import com.packt.bookstore.users.dto.SignUpRequest;
import com.packt.bookstore.users.dto.UpdateProfileRequest;
import com.packt.bookstore.users.dto.UserProfileDTO;
import com.packt.bookstore.users.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from User Service");
    }


    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            log.info("Signup request for: {}", request.getEmail());
            UserProfileDTO user = userService.signUp(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                        "success", true,
                        "message", "User registered successfully",
                        "data", user
                    ));

        } catch (RuntimeException e) {
            log.error("Signup failed: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                    ));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        try {
            log.info("Signin request for: {}", request.getEmail());
            SignInResponse response = userService.signIn(request);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Signin failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "message", "Invalid email or password"
                    ));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "X-User-Id", required = false) String keycloakId) {

        if (keycloakId == null || keycloakId.isEmpty()) {
            log.warn("X-User-Id header missing - request should have been blocked by gateway");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User ID not found"));
        }

        try {
            log.info("Get profile for user: {}", keycloakId);
            UserProfileDTO profile = userService.getProfile(keycloakId);
            return ResponseEntity.ok(profile);

        } catch (RuntimeException e) {
            log.error("Get profile failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "User profile not found"
                    ));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) String keycloakId,
            @Valid @RequestBody UpdateProfileRequest request) {

        if (keycloakId == null || keycloakId.isEmpty()) {
            log.warn("X-User-Id header missing - request should have been blocked by gateway");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "User ID not found"));
        }

        try {
            log.info("Update profile for user: {}", keycloakId);
            UserProfileDTO updatedProfile = userService.updateProfile(keycloakId, request);

            return ResponseEntity.ok(updatedProfile);

        } catch (RuntimeException e) {
            log.error("Update profile failed: {}", e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", e.getMessage()
                    ));
        }
    }

}

