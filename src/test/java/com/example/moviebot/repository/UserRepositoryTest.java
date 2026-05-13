package com.example.moviebot.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEntityTest {

    @Mock
    private UserEntity mockUser;

    private UserEntity realUser;

    @BeforeEach
    void setUp() {

        realUser = UserEntity.builder()
                .id(1L)
                .userTgId(123456789L)
                .name("John Doe")
                .chatId(987654321L)
                .build();
    }



    @Test
    @DisplayName("Should create UserEntity using builder pattern")
    void shouldCreateUserWithBuilder() {

        UserEntity user = UserEntity.builder()
                .id(1L)
                .userTgId(123456789L)
                .name("John Doe")
                .chatId(987654321L)
                .build();


        assertNotNull(user);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUserTgId()).isEqualTo(123456789L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getChatId()).isEqualTo(987654321L);
    }

    @Test
    @DisplayName("Should create UserEntity using no-args constructor")
    void shouldCreateUserWithNoArgsConstructor() {

        UserEntity user = new UserEntity();


        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUserTgId());
        assertNull(user.getName());
        assertNull(user.getChatId());
    }

    @Test
    @DisplayName("Should create UserEntity using all-args constructor")
    void shouldCreateUserWithAllArgsConstructor() {

        UserEntity user = new UserEntity(1L, 123456789L, "John Doe", 987654321L);


        assertNotNull(user);
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUserTgId()).isEqualTo(123456789L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getChatId()).isEqualTo(987654321L);
    }



    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetProperties() {

        UserEntity user = new UserEntity();


        user.setId(1L);
        user.setUserTgId(123456789L);
        user.setName("Jane Doe");
        user.setChatId(987654321L);


        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUserTgId()).isEqualTo(123456789L);
        assertThat(user.getName()).isEqualTo("Jane Doe");
        assertThat(user.getChatId()).isEqualTo(987654321L);
    }

    @Test
    @DisplayName("Should allow updating user properties")
    void shouldUpdateUserProperties() {

        realUser.setName("Jane Smith");
        realUser.setChatId(111111111L);


        assertThat(realUser.getName()).isEqualTo("Jane Smith");
        assertThat(realUser.getChatId()).isEqualTo(111111111L);
        assertThat(realUser.getId()).isEqualTo(1L); // Should remain unchanged
        assertThat(realUser.getUserTgId()).isEqualTo(123456789L); // Should remain unchanged
    }



    @Test
    @DisplayName("Should be equal when IDs are the same")
    void shouldBeEqualWhenIdsAreSame() {

        UserEntity user1 = UserEntity.builder()
                .id(1L)
                .userTgId(123456789L)
                .name("John Doe")
                .chatId(987654321L)
                .build();

        UserEntity user2 = UserEntity.builder()
                .id(1L)
                .userTgId(999999999L)
                .name("Different Name")
                .chatId(111111111L)
                .build();


        assertEquals(user1, user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when IDs are different")
    void shouldNotBeEqualWhenIdsAreDifferent() {

        UserEntity user1 = UserEntity.builder()
                .id(1L)
                .userTgId(123456789L)
                .name("John Doe")
                .chatId(987654321L)
                .build();

        UserEntity user2 = UserEntity.builder()
                .id(2L)
                .userTgId(123456789L)
                .name("John Doe")
                .chatId(987654321L)
                .build();


        assertNotEquals(user1, user2);
        assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {

        assertNotEquals(realUser, null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void shouldNotBeEqualToDifferentClass() {

        assertNotEquals(realUser, "Not a UserEntity");
    }

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {

        assertEquals(realUser, realUser);
    }

    @Test
    @DisplayName("Should have consistent hashCode")
    void shouldHaveConsistentHashCode() {

        UserEntity user = UserEntity.builder()
                .id(1L)
                .name("John Doe")
                .build();

        int hash1 = user.hashCode();
        int hash2 = user.hashCode();


        assertThat(hash1).isEqualTo(hash2);
    }



    @Test
    @DisplayName("Should mock UserEntity getters")
    void shouldMockUserGetters() {

        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getUserTgId()).thenReturn(123456789L);
        when(mockUser.getName()).thenReturn("Mocked User");
        when(mockUser.getChatId()).thenReturn(987654321L);


        assertThat(mockUser.getId()).isEqualTo(1L);
        assertThat(mockUser.getUserTgId()).isEqualTo(123456789L);
        assertThat(mockUser.getName()).isEqualTo("Mocked User");
        assertThat(mockUser.getChatId()).isEqualTo(987654321L);

        verify(mockUser).getId();
        verify(mockUser).getUserTgId();
        verify(mockUser).getName();
        verify(mockUser).getChatId();
    }

    @Test
    @DisplayName("Should verify setter calls on mock")
    void shouldVerifySetterCalls() {

        mockUser.setName("New Name");
        mockUser.setChatId(999999L);


        verify(mockUser).setName("New Name");
        verify(mockUser).setChatId(999999L);
    }

    @Test
    @DisplayName("Should handle null values in mock")
    void shouldHandleNullValuesInMock() {

        when(mockUser.getName()).thenReturn(null);
        when(mockUser.getChatId()).thenReturn(null);


        assertNull(mockUser.getName());
        assertNull(mockUser.getChatId());
    }



    @Test
    @DisplayName("Should create user with partial builder")
    void shouldCreateUserWithPartialBuilder() {

        UserEntity user = UserEntity.builder()
                .name("Minimal User")
                .build();


        assertNotNull(user);
        assertThat(user.getName()).isEqualTo("Minimal User");
        assertNull(user.getId());
        assertNull(user.getUserTgId());
        assertNull(user.getChatId());
    }

}