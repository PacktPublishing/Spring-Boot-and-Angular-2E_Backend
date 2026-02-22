package com.packt.bookstore.users;
import com.packt.bookstore.users.entity.User;
import com.packt.bookstore.users.repository.UserRepository;
import com.packt.bookstore.users.entity.Profile;
import com.packt.bookstore.users.entity.Preferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = UserMsApplication.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user = User.builder()
                .id("1")
                .email("agohar@packt.com")
                .username("agohar")
                .dateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0))
                .status("ACTIVE")
                .profile(Profile.builder().fullName("Ahmad Gohar").build())
                .preferences(Preferences.builder().language("en").build())
                .createdAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();
        userRepository.save(user);
    }

    @Test
    void testFindByEmail() {
        Optional<User> found = userRepository.findByEmail("agohar@packt.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("agohar");
    }

    @Test
    void testFindByUsernameContainingIgnoreCase() {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase("gohar");
        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getEmail()).isEqualTo("agohar@packt.com");
    }
    @Test
    void testFindByLanguagePreference() {
        List<User> users = userRepository.findByLanguagePreference("en");
        assertThat(users).isNotEmpty();
        assertThat(users.get(0).getPreferences().getLanguage()).isEqualTo("en");
    }   
}