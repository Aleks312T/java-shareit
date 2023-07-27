package ru.practicum.shareit.item.service;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplIntegrationTest {
    private final UserService userService;
    private final ItemService itemService;
    private UserDto userDto1;
    private UserDto userDto2;
    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private ItemDto itemDto3;

    @BeforeEach
    void setUp() {
        userDto1 = userService.create(UserDto.builder()
                .name("User 1 name")
                .email("user1@email.com")
                .build());
        userDto2 = userService.create(UserDto.builder()
                .name("User 2 name")
                .email("user2@email.com")
                .build());
        itemDto1 = itemService.create(
                userDto1.getId(),
                ItemDto.builder()
                        .name("Item 1 name")
                        .description("Item 1 description")
                        .available(true)
                        .build()
        );
        itemDto2 = itemService.create(
                userDto1.getId(),
                ItemDto.builder()
                        .name("Item 2 name")
                        .description("Item 2 description")
                        .available(false)
                        .build()
        );
        itemDto3 = itemService.create(
                userDto2.getId(),
                ItemDto.builder()
                        .name("Item 3 name")
                        .description("Item 3 description")
                        .available(true)
                        .build()
        );
    }

    @Test
    void testGetAllOwnerItems() {


        Long userId = userDto1.getId();
        List<ItemDto> actualItems = itemService.getAllUserItems(userId);

        assertThat(actualItems.size(), equalTo(2));
        assertThat(actualItems.get(0).getId(), equalTo(itemDto1.getId()));
        assertThat(actualItems.get(0).getName(), equalTo("Item 1 name"));
        assertThat(actualItems.get(0).getAvailable(), equalTo(true));
        assertThat(actualItems.get(1).getId(), equalTo(itemDto2.getId()));
        assertThat(actualItems.get(1).getName(), equalTo("Item 2 name"));
        assertThat(actualItems.get(1).getAvailable(), equalTo(false));
    }
}