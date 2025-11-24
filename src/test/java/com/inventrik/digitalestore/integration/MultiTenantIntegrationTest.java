package com.inventrik.digitalestore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventrik.digitalestore.dto.request.TenantUserSignupRequest;
import com.inventrik.digitalestore.dto.request.ProductRequest;
import com.inventrik.digitalestore.dto.response.UserResponse;
import com.inventrik.digitalestore.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class MultiTenantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldCreateAndRetrieveUserInMultiTenantEnvironment() throws Exception {
        TenantUserSignupRequest userRequest = new TenantUserSignupRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("test@example.com");
        userRequest.setFirstName("Test");
        userRequest.setLastName("User");
        userRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        mockMvc.perform(get("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'testuser')]").exists());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldCreateAndRetrieveProductInMultiTenantEnvironment() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName("Test Product");
        productRequest.setDescription("Test product description");
        productRequest.setDefaultPrice(new BigDecimal("99.99"));
        productRequest.setDefaultCurrency("USD");
        productRequest.setCategoryId(1L);

        mockMvc.perform(post("/api/v1/products")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.defaultPrice").value(99.99));

        mockMvc.perform(get("/api/v1/products")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.productName == 'Test Product')]").exists());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldMaintainTenantIsolation() throws Exception {
        TenantUserSignupRequest tenant1User = new TenantUserSignupRequest();
        tenant1User.setUsername("tenant1user");
        tenant1User.setEmail("tenant1@example.com");
        tenant1User.setFirstName("Tenant1");
        tenant1User.setLastName("User");
        tenant1User.setPassword("password123");

        TenantUserSignupRequest tenant2User = new TenantUserSignupRequest();
        tenant2User.setUsername("tenant2user");
        tenant2User.setEmail("tenant2@example.com");
        tenant2User.setFirstName("Tenant2");
        tenant2User.setLastName("User");
        tenant2User.setPassword("password123");

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tenant1User)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 2).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tenant2User)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'tenant1user')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'tenant2user')]").doesNotExist());

        mockMvc.perform(get("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 2)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'tenant2user')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'tenant1user')]").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void shouldEnforceRoleBasedAccess() throws Exception {
        TenantUserSignupRequest userRequest = new TenantUserSignupRequest();
        userRequest.setUsername("normaluser");
        userRequest.setEmail("normal@example.com");
        userRequest.setPassword("password123");

        mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldAccessPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/public/categories"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/public/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldHandleCompleteUserLifecycle() throws Exception {
        TenantUserSignupRequest createRequest = new TenantUserSignupRequest();
        createRequest.setUsername("lifecycleuser");
        createRequest.setEmail("lifecycle@example.com");
        createRequest.setFirstName("Lifecycle");
        createRequest.setLastName("User");
        createRequest.setPassword("password123");

        String createResponse = mockMvc.perform(post("/api/v1/users")
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1).claim("sub", "admin")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("lifecycleuser"))
                .andReturn().getResponse().getContentAsString();

        UserResponse createdUser = objectMapper.readValue(createResponse, UserResponse.class);
        Long userId = createdUser.getUserId();

        mockMvc.perform(get("/api/v1/users/" + userId)
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("lifecycleuser"));

        mockMvc.perform(delete("/api/v1/users/" + userId)
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/" + userId)
                .with(jwt().jwt(jwt -> jwt.claim("tenant_id", 1)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}