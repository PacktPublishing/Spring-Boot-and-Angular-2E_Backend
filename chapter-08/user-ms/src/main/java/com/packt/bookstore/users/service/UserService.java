package com.packt.bookstore.users.service;

import java.util.Collections;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.packt.bookstore.users.dto.RefreshTokenRequest;
import com.packt.bookstore.users.dto.SignInRequest;
import com.packt.bookstore.users.dto.SignInResponse;
import com.packt.bookstore.users.dto.SignUpRequest;
import com.packt.bookstore.users.dto.UpdateProfileRequest;
import com.packt.bookstore.users.dto.UserProfileDTO;
import com.packt.bookstore.users.entity.Address;
import com.packt.bookstore.users.entity.Profile;
import com.packt.bookstore.users.entity.User;
import com.packt.bookstore.users.repository.UserRepository;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final Keycloak keycloak;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.client-id}")
    private String clientId;


     private UserProfileDTO mapToDTO(User user) {
        Profile profile = user.getProfile();
        Address address = profile.getAddress();

        return UserProfileDTO.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phone(profile.getPhone())
                .address(address != null ? address.getStreet() : null)
                .city(address != null ? address.getCity() : null)
                .state(address != null ? address.getState() : null)
                .zipCode(address != null ? address.getPostalCode() : null)
                .country(address != null ? address.getCountry() : null)
                .role("user") // Simplified for this example
                .enabled(true) // Simplified for this example
                .build();
    }

    @Transactional
    public UserProfileDTO signUp(SignUpRequest request) {
        log.info("Processing signup for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.getEmail());
        kcUser.setEmail(request.getEmail());
        kcUser.setFirstName(request.getFirstName());
        kcUser.setLastName(request.getLastName());
        kcUser.setEnabled(true);
        kcUser.setEmailVerified(false);

        Response response = usersResource.create(kcUser);

        if (response.getStatus() != 201) {
            String error = response.readEntity(String.class);
            log.error("Failed to create user in Keycloak: {}", error);
            throw new RuntimeException("Failed to create user: " + error);
        }

        String locationHeader = response.getHeaderString("Location");
        String keycloakId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        log.info("Created Keycloak user: {}", keycloakId);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        usersResource.get(keycloakId).resetPassword(credential);
        log.info("Password set for user: {}", keycloakId);

        try {
            RoleRepresentation userRole = realmResource.roles().get("user").toRepresentation();
            usersResource.get(keycloakId).roles().realmLevel()
                    .add(Collections.singletonList(userRole));
            log.info("Assigned user role to: {}", keycloakId);
        } catch (Exception e) {
            log.error("Failed to assign role: {}", e.getMessage());
        }
        Address address = Address.builder()
                .street(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getZipCode())
                .country(request.getCountry())
                .build();
        Profile profile = Profile.builder()
                .address(address)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthDate(request.getDateOfBirth())
                .build();

        User user = User.builder()
                .username(request.getEmail())
                .keycloakId(keycloakId)
                .email(request.getEmail())
                .profile(profile)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User profile saved with ID: {}", savedUser.getId());

        return mapToDTO(savedUser);
    }


    public SignInResponse signIn(SignInRequest request) {
    log.info("Processing signin for email: {}", request.getEmail());

    String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "password");
    body.add("client_id", clientId);
    body.add("username", request.getEmail());
    body.add("password", request.getPassword());

    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

    try {
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
        Map<String, Object> tokenResponse = response.getBody();

        if (tokenResponse == null) {
            throw new RuntimeException("Failed to obtain tokens from Keycloak");
        }

        log.info("Tokens obtained from Keycloak");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User profile not found in database"));

        return SignInResponse.builder()
                .accessToken((String) tokenResponse.get("access_token"))
                .refreshToken((String) tokenResponse.get("refresh_token"))
                .tokenType("Bearer")
                .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                .user(mapToDTO(user))
                .build();

    } catch (Exception e) {
        log.error("Signin failed: {}", e.getMessage());
        throw new RuntimeException("Invalid email or password");
    }
}

    @Transactional
public UserProfileDTO 
updateProfile(String keycloakId, UpdateProfileRequest request) {
    log.info("Updating profile for keycloakId: {}", keycloakId);

    User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (request.getFirstName() != null) {
        user.getProfile().setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null) {
        user.getProfile().setLastName(request.getLastName());
    }
    if (request.getPhone() != null) {
        user.getProfile().setPhone(request.getPhone());
    }
    if (request.getAddress() != null) {
        user.getProfile().getAddress().setStreet(request.getAddress());
    }
    if (request.getCity() != null) {
        user.getProfile().getAddress().setCity(request.getCity());
    }
    if (request.getState() != null) {
        user.getProfile().getAddress().setState(request.getState());
    }
    if (request.getZipCode() != null) {
        user.getProfile().getAddress().setPostalCode(request.getZipCode());
    }
    if (request.getCountry() != null) {
        user.getProfile().getAddress().setCountry(request.getCountry());
    }
    if (request.getProfilePictureUrl() != null) {
        user.getProfile().setProfilePictureUrl(request.getProfilePictureUrl());
    }

    User updatedUser = userRepository.save(user);
    log.info("Profile updated successfully");

    return mapToDTO(updatedUser);
}

    public UserProfileDTO getProfile(String keycloakId) {
    log.info("Getting profile for keycloakId: {}", keycloakId);

    User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return mapToDTO(user);
}

    public SignInResponse refreshToken(RefreshTokenRequest request) {
        log.info("Attempting to refresh token");

        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("refresh_token", request.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);
            Map<String, Object> tokenResponse = response.getBody();

            if (tokenResponse == null || tokenResponse.containsKey("error")) {
                throw new RuntimeException("Failed to refresh token");
            }

            log.info("Token refreshed successfully");

            // Return new tokens without user profile (client already has it)
            return SignInResponse.builder()
                    .accessToken((String) tokenResponse.get("access_token"))
                    .refreshToken((String) tokenResponse.get("refresh_token"))
                    .tokenType("Bearer")
                    .expiresIn(((Number) tokenResponse.get("expires_in")).longValue())
                    .user(null) // No need to fetch user again
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Invalid or expired refresh token");
        }
    }

    public void logout(String keycloakId) {
        log.info("Logging out user: {}", keycloakId);
        // In a real scenario, you would:
        // 1. Invalidate the refresh token on Keycloak
        // 2. Log the logout event
        // 3. Clear any server-side sessions
        
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            
            // Get user resource and logout (invalidate refresh tokens)
            usersResource.get(keycloakId).logout();
            log.info("User logged out successfully");
        } catch (Exception e) {
            log.warn("Logout cleanup failed: {}", e.getMessage());
            // Don't throw exception - logout should succeed even if cleanup fails
        }
    }
   
}
