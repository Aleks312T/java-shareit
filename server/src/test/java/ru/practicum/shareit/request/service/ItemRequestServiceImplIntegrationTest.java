package java.ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplIntegrationTest {
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRequestService itemRequestService;

    @Test
    void testGetById() {
        UserDto userDto1 = userService.create(UserDto.builder()
                .name("User 1 name")
                .email("user1@email.com")
                .build());
        UserDto userDto2 = userService.create(UserDto.builder()
                .name("User 2 name")
                .email("user2@email.com")
                .build());
        ItemRequestFullDto itemRequest = itemRequestService.create(
                userDto1.getId(),
                ItemRequestDtoInput.builder()
                        .description("ItemRequest 1 description")
                        .build()
                );
        itemService.create(
                userDto2.getId(),
                ItemDto.builder()
                        .name("Item 1 name")
                        .description("Item 1 description")
                        .available(true)
                        .requestId(itemRequest.getId())
                        .build()
                );
        itemService.create(
                userDto2.getId(),
                ItemDto.builder()
                        .name("Item 2 name")
                        .description("Item 2 description")
                        .available(true)
                        .requestId(itemRequest.getId())
                        .build()
                );

        ItemRequestFullDto actualRequest = itemRequestService.getById(
                itemRequest.getRequestor().getId(),
                itemRequest.getId());

        assertThat(actualRequest.getId(), equalTo(itemRequest.getId()));
        assertThat(actualRequest.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(actualRequest.getCreated(), equalTo(itemRequest.getCreated()));
        assertThat(actualRequest.getItems().size(), equalTo(2));
    }
}