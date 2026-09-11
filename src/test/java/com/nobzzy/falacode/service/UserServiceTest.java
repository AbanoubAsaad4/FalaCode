package com.nobzzy.falacode.service;

import com.nobzzy.falacode.dto.UserDto;
import com.nobzzy.falacode.entity.User;
import com.nobzzy.falacode.exception.EmailAlreadyExistsException;
import com.nobzzy.falacode.exception.ResourceNotFoundException;
import com.nobzzy.falacode.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully when email is unique")
        void shouldCreateUser_Successfully() {
            UserDto userDto = UserDto.builder()
                    .name("Alex")
                    .email("alex@falacode.com")
                    .password("Password123!")
                    .build();

            User savedUser = User.builder()
                    .id(1L)
                    .name("Alex")
                    .email("alex@falacode.com")
                    .password("Password123!")
                    .build();

            when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserDto result = userService.createUser(userDto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Alex");
            assertThat(result.getEmail()).isEqualTo("alex@falacode.com");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email exists")
        void shouldThrowException_WhenEmailAlreadyExists() {
            UserDto dto = UserDto.builder()
                    .name("Alex")
                    .email("existing@falacode.com")
                    .password("Password123!")
                    .build();

            when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("Email is already registered.");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Read User Tests")
    class ReadUserTests {

        @Test
        @DisplayName("Should return user DTO when ID exists")
        void shouldGetUserById_Successfully() {
            User user = User.builder()
                    .id(1L)
                    .name("John")
                    .email("john@falacode.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserDto result = userService.getUserById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("john@falacode.com");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user ID not found")
        void shouldThrowException_WhenUserByIdNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id: 99");
        }

        @Test
        @DisplayName("Should return list of all users")
        void shouldGetAllUsers() {
            User user1 = User.builder()
                    .id(1L).name("User 1")
                    .email("u1@test.com")
                    .build();

            User user2 = User.builder()
                    .id(2L).name("User 2")
                    .email("u2@test.com")
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(user1, user2));

            List<UserDto> results = userService.getAllUsers();

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getName()).isEqualTo("User 1");
            assertThat(results.get(1).getName()).isEqualTo("User 2");
        }
    }

    @Nested
    @DisplayName("Update & Delete User Tests")
    class UpdateDeleteTests {

        @Test
        @DisplayName("Should update existing user")
        void shouldUpdateUser_Successfully() {
            User existingUser = User.builder()
                    .id(1L)
                    .name("Old Name")
                    .email("old@test.com")
                    .build();

            UserDto updateDto = UserDto.builder()
                    .name("New Name")
                    .email("new@test.com")
                    .build();

            User savedUser = User.builder()
                    .id(1L)
                    .name("New Name")
                    .email("new@test.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            UserDto result = userService.updateUser(1L, updateDto);

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getEmail()).isEqualTo("new@test.com");
        }

        @Test
        @DisplayName("Should delete user when ID exists")
        void shouldDeleteUser_Successfully() {
            when(userRepository.existsById(1L)).thenReturn(true);

            userService.deleteUser(1L);

            verify(userRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
        void shouldThrowException_WhenDeletingNonExistentUser() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).deleteById(anyLong());
        }
    }
}