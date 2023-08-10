package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.comments.repository.CommentRepository;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    private ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    private User user2;
    private ItemRequest itemRequest1;
    private Item item1;
    private Item item2;

    @BeforeEach
    void beforeEach() {
        itemService = new ItemServiceImpl(
                itemRepository, userRepository, commentRepository, bookingRepository, itemRequestRepository);
        User user1 = User.builder()
                .id(1L)
                .name("User1 name")
                .email("user1@email.ru")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("User2 name")
                .email("user2@email.ru")
                .build();
        itemRequest1 = ItemRequest.builder()
                .id(1L)
                .description("ItemRequest description")
                .requestor(user1)
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("Item 1 name")
                .description("Item 1 description")
                .available(true)
                .owner(user1)
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
    void testAddNew() {
        Long newItemId = 5L;
        ItemDto createItemDto = ItemMapper.toItemDto(item1);
        Long userId = item1.getOwner().getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(item1.getOwner()));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocationOnMock -> {
                    Item item = invocationOnMock.getArgument(0, Item.class);
                    item.setId(newItemId);
                    return item;
                });

        ItemDto actualItemDto = itemService.create(userId, createItemDto);

        assertThat(actualItemDto.getId(), equalTo(newItemId));
        assertThat(actualItemDto.getName(), equalTo(item1.getName()));
        assertThat(actualItemDto.getRequestId(), equalTo(null));
        verify(userRepository, times(1))
                .findById(userId);
        verify(itemRepository, times(1))
                .save(Mockito.any(Item.class));
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void testAddNewToItemRequest() {
        Long newItemId = 5L;
        ItemDto createItemDto = ItemMapper.toItemDto(item2);
        Long userId = item2.getOwner().getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(item2.getOwner()));
        when(itemRequestRepository.findById(createItemDto.getRequestId()))
                .thenReturn(Optional.of(itemRequest1));
        when(itemRepository.save(Mockito.any(Item.class)))
                .thenAnswer(invocationOnMock -> {
                    Item item = invocationOnMock.getArgument(0, Item.class);
                    item.setId(newItemId);
                    return item;
                });

        ItemDto actualItemDto = itemService.create(userId, createItemDto);

        assertThat(actualItemDto.getId(), equalTo(newItemId));
        assertThat(actualItemDto.getName(), equalTo(item2.getName()));
        assertThat(actualItemDto.getRequestId(), equalTo(item2.getRequest().getId()));
        verify(userRepository, times(1))
                .findById(userId);
        verify(itemRequestRepository, times(1))
                .findById(createItemDto.getRequestId());
        verify(itemRepository, times(1))
                .save(Mockito.any(Item.class));
        Mockito.verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void testPatchUpdate() {
        String newUserName = "New user name";
        String expectedDesc = item1.getDescription();
        Long itemId = item1.getId();
        Long userId = item1.getOwner().getId();
        ItemDto itemDto = ItemDto.builder()
                .name(newUserName)
                .description(null)
                .available(false)
                .build();
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        when(itemRepository.save(item1))
                .thenReturn(item1);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(item1.getOwner()));

        ItemDto actualItemDto = itemService.update(userId, itemId, itemDto);

        assertThat(actualItemDto.getId(), equalTo(itemId));
        assertThat(actualItemDto.getName(), equalTo(newUserName));
        assertThat(actualItemDto.getDescription(), equalTo(expectedDesc));
        verify(itemRepository, times(1))
                .findById(itemId);
        verify(itemRepository, times(1))
                .save(item1);
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testPatchUpdateNoOwner() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ItemDto itemDto = ItemMapper.toItemDto(item1);

        Exception exception = assertThrows(
                ObjectNotFoundException.class,
                () -> itemService.create(10L, itemDto));

        String expectedMessage = "Пользователь с id = " + 10L + " не найден";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testPatchUpdateWrongItemId() {
        String newUserName = "New user name";
        Long itemId = 99L;
        Long userId = item1.getOwner().getId();
        ItemDto itemDto = ItemDto.builder()
                .name(newUserName)
                .description(null)
                .available(false)
                .build();

        ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
                () -> itemService.update(userId, itemId, itemDto));
        assertThat(e.getMessage(), equalTo("Пользователь с id = " + userId + " не найден"));
    }

    @Test
    void testGetByIdWithoutCommentsAndWithBookingGettingByNoOwner() {
        Long itemId = item1.getId();
        Long userId = user2.getId();
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user2));
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        when(commentRepository.findAllByItemId(itemId))
                .thenReturn(Collections.emptyList());

        ItemDto actualItemDto = itemService.get(itemId, userId);

        assertThat(actualItemDto.getId(), equalTo(itemId));
        assertThat(actualItemDto.getName(), equalTo(item1.getName()));
        assertThat(actualItemDto.getLastBooking(), equalTo(null));
        assertThat(actualItemDto.getNextBooking(), equalTo(null));
        assertThat(actualItemDto.getComments().size(), equalTo(0));
        verify(itemRepository, times(1))
                .findById(itemId);
        verify(commentRepository, times(1))
                .findAllByItemId(itemId);
        Mockito.verifyNoMoreInteractions(itemRepository, commentRepository);
    }

    @Test
    void testAddNewComment_whenValid() {
        Long itemId = item1.getId();
        Long userId = user2.getId();
        CommentDto requestComment = CommentDto.builder()
                .text("text")
                .build();
        Comment expectedComment = Comment.builder()
                .id(1L)
                .text(requestComment.getText())
                .item(item1)
                .authorName(user2)
                .created(LocalDateTime.now().minusHours(1))
                .build();
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item1));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user2));
        when(commentRepository.save(Mockito.any()))
                .thenAnswer(invocationOnMock -> {
                    Comment comment = invocationOnMock.getArgument(0, Comment.class);
                    comment.setId(expectedComment.getId());
                    comment.setCreated(expectedComment.getCreated());
                    return comment;
                });

        CommentDto actualComment = itemService.createComment(
                requestComment,
                userId,
                itemId);

        assertThat(actualComment.getId(), equalTo(expectedComment.getId()));
        assertThat(actualComment.getText(), equalTo(expectedComment.getText()));
        assertThat(actualComment.getAuthorName(), equalTo(expectedComment.getAuthorName().getName()));
        assertThat(actualComment.getCreated(), equalTo(expectedComment.getCreated()));
    }

    @Test
    void searchItemsTest() throws Exception {
        List<ItemDto> items = Collections.singletonList(ItemMapper.toItemDto(item1));
        when(itemRepository.search("item"))
                .thenReturn(Collections.singletonList(item1));

        List<ItemDto> result = itemService.search("item");
        assertThat(result, equalTo(items));
    }

    @Test
    void searchItemsTextIsNullTest() throws Exception {
        Exception exception = assertThrows(
                NullPointerException.class,
                () -> itemService.search(null));
    }

    @Test
    void searchItemsWithoutTextTest() throws Exception {
        List<ItemDto> result = new ArrayList<>();
        assertThat(result, equalTo(itemService.search("")));
    }
}