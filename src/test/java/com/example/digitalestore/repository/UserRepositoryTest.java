package com.example.digitalestore.repository;

import com.inventrik.digitalestore.domain.user.User;
import com.inventrik.digitalestore.domain.user.UserRole;
import com.inventrik.digitalestore.domain.user.UserType;
import com.inventrik.digitalestore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@SpringBootTest(classes = com.inventrik.digitalestore.DigitalEstoreApplication.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = createTestUser(1, 1L, "testuser", "test@example.com");
        
        User saved = userRepository.save(user);
        
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindByTenantIdAndUserId() {
        User user = createTestUser(1, 1L, "testuser", "test@example.com");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByTenantIdAndUserId(1, 1L);

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldFindByTenantIdAndUsername() {
        User user = createTestUser(1, 1L, "testuser", "test@example.com");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByTenantIdAndUsername(1, "testuser");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldFindByTenantIdAndEmail() {
        User user = createTestUser(1, 1L, "testuser", "test@example.com");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByTenantIdAndEmail(1, "test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldFindAllByTenantId() {
        User user1 = createTestUser(1, 1L, "user1", "user1@example.com");
        User user2 = createTestUser(1, 2L, "user2", "user2@example.com");
        User user3 = createTestUser(2, 1L, "user3", "user3@example.com");
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        List<User> tenant1Users = userRepository.findByTenantId(1);
        List<User> tenant2Users = userRepository.findByTenantId(2);

        assertThat(tenant1Users).hasSize(2);
        assertThat(tenant2Users).hasSize(1);
        assertThat(tenant1Users).extracting("username").containsExactlyInAnyOrder("user1", "user2");
        assertThat(tenant2Users).extracting("username").containsExactlyInAnyOrder("user3");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> found = userRepository.findByTenantIdAndUserId(999, 999L);
        
        assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteUser() {
        User user = createTestUser(1, 1L, "testuser", "test@example.com");
        User saved = entityManager.persistAndFlush(user);

        userRepository.deleteById(new User.UserPK(saved.getTenantId(), saved.getUserId()));

        Optional<User> found = userRepository.findByTenantIdAndUserId(1, 1L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateUser() {
        User user = createTestUser(1, 1L, "testuser", "test@example.com");
        User saved = entityManager.persistAndFlush(user);

        saved.setFirstName("Updated");
        saved.setLastName("Name");
        saved.setPhone("1234567890");
        
        User updated = userRepository.save(saved);

        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getLastName()).isEqualTo("Name");
        assertThat(updated.getPhone()).isEqualTo("1234567890");
    }

    @Test
    void shouldHandleMultipleTenantsCorrectly() {
        User tenant1User = createTestUser(1, 1L, "user1", "user1@example.com");
        User tenant2User = createTestUser(2, 1L, "user1", "user1@example.com");
        
        entityManager.persistAndFlush(tenant1User);
        entityManager.persistAndFlush(tenant2User);

        Optional<User> found1 = userRepository.findByTenantIdAndUsername(1, "user1");
        Optional<User> found2 = userRepository.findByTenantIdAndUsername(2, "user1");

        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found1.get().getTenantId()).isEqualTo(1);
        assertThat(found2.get().getTenantId()).isEqualTo(2);
    }

    private User createTestUser(Integer tenantId, Long userId, String username, String email) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setUserId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUserType(UserType.INDIVIDUAL);
        user.setUserRole(UserRole.USER);
        user.setCreated(LocalDateTime.now());
        user.setUpdated(LocalDateTime.now());
        return user;
    }
}