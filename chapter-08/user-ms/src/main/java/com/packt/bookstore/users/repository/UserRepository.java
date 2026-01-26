package com.packt.bookstore.users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.packt.bookstore.users.entity.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String keyword);

    @Query("{ 'preferences.language': ?0 }")
    List<User> findByLanguagePreference(String language);

    Optional<User> findByKeycloakId(String keycloakId);
    boolean existsByEmail(String email);
}
