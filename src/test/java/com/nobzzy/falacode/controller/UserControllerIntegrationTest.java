package com.nobzzy.falacode.controller;

import tools.jackson.databind.ObjectMapper;
import com.nobzzy.falacode.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/users - Should create user successfully")
    void shouldCreateUser() throws Exception {
        UserDto request = UserDto.builder()
                .name("alex_dev")
                .email("alex@falacode.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("alex_dev"))
                .andExpect(jsonPath("$.email").value("alex@falacode.com"));
    }

    @Test
    @DisplayName("POST /api/users - Should return error when email already exists")
    void shouldReturnError_WhenEmailAlreadyExists() throws Exception {
        UserDto user1 = UserDto.builder()
                .name("user_one")
                .email("duplicate@falacode.com")
                .password("Password123!")
                .build();

        // Create an initial user
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated());

        // Attempt duplicate creation
        UserDto user2 = UserDto.builder()
                .name("user_two")
                .email("duplicate@falacode.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return 404 when user not found")
    void shouldReturn404_WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users - Should return 400 when validation fails")
    void shouldReturn400_WhenUserDtoIsInvalid() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .name("")
                .email("not-an-email")
                .password("123")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should delete user successfully")
    void shouldDeleteUser() throws Exception {
        UserDto request = UserDto.builder()
                .name("user_to_delete")
                .email("delete_me@falacode.com")
                .password("Password123!")
                .build();

        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long createdUserId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/users/{id}", createdUserId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", createdUserId))
                .andExpect(status().isNotFound());
    }
}