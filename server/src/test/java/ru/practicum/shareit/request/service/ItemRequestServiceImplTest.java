package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    private User user1;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;
    private ItemRequestDtoInput itemRequestDtoInput;
    private Item item1;
    private Item item2;

    @BeforeEach
    void beforeEach() {
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, userRepository, itemRepository);
        user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        User user2 = User.builder()
                .id(2L)
                .name("User2 name")
                .email("user2@email.ru")
                .build();
        itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("ItemRequest 1 description")
                .requestor(user1)
                .build();
        itemRequest2 = ItemRequest.builder()
                .id(2L)
                .description("ItemRequest 2 description")
                .requestor(user1)
                .build();
        itemRequestDtoInput = ItemRequestDtoInput.builder()
                .description("Input Description")
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("Item 1 name")
                .description("Item 1 description")
                .available(true)
                .owner(user1)
                .request(itemRequest1)
                .build();
        item2 = Item.builder()
                .id(2L)
                .name("Item 2 name")
                .description("Item 2 description")
                .available(true)
                .owner(user2)
                .request(itemRequest1)
                .build();

    }

    @Test
    void createRequest() {
        ItemRequest expectedItemRequest = ItemRequestMapper.fromItemRequestDtoInput(
                itemRequestDtoInput,
                user1,
                LocalDateTime.now());

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(expectedItemRequest);

        ItemRequestFullDto response = itemRequestService.create(user1.getId(), itemRequestDtoInput);

        assertEquals(expectedItemRequest.getId(), response.getId());
        assertEquals(expectedItemRequest.getRequestor().getId(), response.getRequestor().getId());
        assertEquals(expectedItemRequest.getDescription(), response.getDescription());
        assertEquals(expectedItemRequest.getCreated(), response.getCreated());

        verify(userRepository, times(1)).findById(user1.getId());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void createRequestWithNonExistingUser() {
        when(userRepository.findById(user1.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                ObjectNotFoundException.class,
                () -> itemRequestService.create(user1.getId(), itemRequestDtoInput)
        );

        String expectedMessage = "Пользователь с Id = " + user1.getId() + " не найден";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(user1.getId());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getOwnerRequests() {
        Long requesterId = user1.getId();
        Mockito
                .when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when((itemRepository.findAllByRequestId(user1.getId())))
                .thenReturn(List.of(item1, item2));
        Mockito
                .when(itemRequestRepository.findAllByRequestor_Id(requesterId))
                .thenReturn(List.of(itemRequest1, itemRequest2));

        List<ItemRequestFullDto> actualItemRequestWithItems =
                itemRequestService.getAll(requesterId);

        assertThat(actualItemRequestWithItems.size(), equalTo(2));
        assertThat(actualItemRequestWithItems.get(0).getId(), equalTo(itemRequest1.getId()));
        assertThat(actualItemRequestWithItems.get(0).getItems().size(), equalTo(2));
        assertThat(actualItemRequestWithItems.get(0).getItems().get(0).getId(), equalTo(item1.getId()));
        assertThat(actualItemRequestWithItems.get(0).getItems().get(1).getId(), equalTo(item2.getId()));
        assertThat(actualItemRequestWithItems.get(1).getId(), equalTo(itemRequest2.getId()));
        assertThat(actualItemRequestWithItems.get(1).getItems().size(), equalTo(0));
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(requesterId);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findAllByRequestor_Id(requesterId);
    }

    @Test
    void getOwnerRequestsWithUserNotFound() {
        Long badUserId = 10L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                ObjectNotFoundException.class,
                () -> itemRequestService.getAll(badUserId));

        String expectedMessage = "Пользователь с Id = " + badUserId + " не найден";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository, itemRepository, itemRequestRepository);
    }

    @Test
    void getUserRequests() {
        Page<ItemRequest> itemRequestPage = new PageImpl<>(Arrays.asList(itemRequest1, itemRequest2));
        when(itemRequestRepository.findAllByRequestor_IdNot(anyLong(), any(Pageable.class))).thenReturn(itemRequestPage);

        List<ItemRequestFullDto> response = itemRequestService.getSort(user1.getId(), 0, 2);

        assertEquals(2, response.size());
        verify(itemRequestRepository, times(1)).findAllByRequestor_IdNot(anyLong(), any(Pageable.class));
    }

    @Test
    void getRequestById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.of(itemRequest1));
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(Arrays.asList(item1, item2));

        ItemRequestFullDto response = itemRequestService.getById(user1.getId(), itemRequest1.getId());

        assertEquals(itemRequest1.getId(), response.getId());
        assertEquals(2, response.getItems().size());
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
    }

    @Test
    void getRequestByIdWithUserNotFound() {
        Long badUserId = 10L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                ObjectNotFoundException.class,
                () -> itemRequestService.getById(badUserId, itemRequest1.getId()));

        String expectedMessage = "Пользователь с Id = " + badUserId + " не найден";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findAllByRequestId(anyLong());
    }

    @Test
    void getRequestByIdWithRequestNotFound() {
        Long badRequestId = 10L;

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                ObjectNotFoundException.class,
                () -> itemRequestService.getById(user1.getId(), badRequestId));

        String expectedMessage = "Запрос на предмет с Id = " + badRequestId + " не найден.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRequestRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).findAllByRequestId(anyLong());
    }
}