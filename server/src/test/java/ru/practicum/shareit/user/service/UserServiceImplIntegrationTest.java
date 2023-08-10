package java.ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserServiceImpl userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .name("user1")
                .email("user1@mail.ru")
                .build();
        user2 = User.builder()
                .name("user2")
                .email("user2@mail.ru")
                .build();
    }

    @Test
    void updateUser() {
        userRepository.save(user1);
        userRepository.save(user2);

        String newName = "updatedUser1";
        String newEmail = "updatedUser1@mail.ru";

        UserDto userDto = UserDto.builder()
                .name("updatedUser1")
                .email("updatedUser1@mail.ru")
                .build();

        UserDto updatedUser = userService.update(user1.getId(), userDto);

        assertNotNull(updatedUser);
        assertEquals(newName, updatedUser.getName());
        assertEquals(newEmail, updatedUser.getEmail());

        UserDto nonUpdatedUser = userService.get(user2.getId());

        assertNotNull(nonUpdatedUser);
        assertNotEquals(newName, nonUpdatedUser.getName());
        assertNotEquals(newEmail, nonUpdatedUser.getEmail());
    }
}