package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.ConflictException;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    private final User user1 = User.builder()
            .id(1L)
            .name("user1")
            .email("user1@mail.ru")
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .name("user2")
            .email("user2@mail.ru")
            .build();
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void getAllUsers() {
        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> userDtos = userService.getAll();

        assertEquals(2, userDtos.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsersWithEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAll();
        Assertions.assertEquals(result, new ArrayList<>());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));

        UserDto userDto = userService.get(user1.getId());

        assertEquals(user1.getId(), userDto.getId());
        assertEquals(user1.getName(), userDto.getName());
        assertEquals(user1.getEmail(), userDto.getEmail());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void getUserByIdWithNonExistentUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                ObjectNotFoundException.class, () -> userService.get(10L));

        String expectedMessage = String.format("Пользователь с id = %s не найден", 10L);
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void saveUser() {
        UserDto userDto = UserMapper.toUserDto(user1);
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserDto savedUserDto = userService.create(userDto);

        assertEquals(user1.getId(), savedUserDto.getId());
        assertEquals(user1.getName(), savedUserDto.getName());
        assertEquals(user1.getEmail(), savedUserDto.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser() {
        UserDto userDto = UserMapper.toUserDto(user1);
        userDto.setName("updatedName");
        userDto.setEmail("updatedEmail@mail.ru");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.save(any())).thenReturn(user1);

        UserDto updatedUserDto = userService.update(user1.getId(), userDto);

        assertEquals(userDto.getName(), updatedUserDto.getName());
        assertEquals(userDto.getEmail(), updatedUserDto.getEmail());

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserWithNonExistentUser() {
        UserDto userDto = UserMapper.toUserDto(user1);

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                ObjectNotFoundException.class, () -> userService.update(10L, userDto));

        String expectedMessage = String.format("Пользователь с id = %s не найден", 10L);
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void updateUserWithBlankName() {
        UserDto userDto = UserMapper.toUserDto(User.builder()
                        .name("")
                        .email(user2.getEmail())
                .build());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.save(user1)).thenReturn(user1);

        UserDto updatedUserDto = userService.update(user1.getId(), userDto);

        //Имя старое, email - новый
        assertEquals(user1.getName(), updatedUserDto.getName());
        assertEquals(userDto.getEmail(), updatedUserDto.getEmail());

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void updateUserWithExistingEmail() {
        UserDto userDto = UserMapper.toUserDto(user1);
        userDto.setEmail(user2.getEmail());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(userRepository.findByEmailContainingIgnoreCase(any())).thenReturn(List.of(user1, user2));

        Exception exception = assertThrows(
                ConflictException.class, () -> userService.update(user1.getId(), userDto));

        String expectedMessage = "Электронная почта уже занята";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
    }

    @Test
    void deleteUser() {
        doNothing().when(userRepository).deleteById(anyLong());

        userService.delete(user1.getId());

        verify(userRepository, times(1)).deleteById(anyLong());
    }
}