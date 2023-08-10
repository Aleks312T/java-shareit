package java.ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    private final LocalDateTime start = LocalDateTime.now().plusMinutes(30);
    private final LocalDateTime end = LocalDateTime.now().plusHours(1);
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    private User user1;
    private User user2;
    private Item item1;
    private Booking booking1;

    @BeforeEach
    void beforeEach() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
        user1 = User.builder()
                .id(1L)
                .name("User 1 name")
                .email("user1@email.ru")
                .build();
        user2 = User.builder()
                .id(2L)
                .name("User 2 name")
                .email("user2@email.ru")
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .owner(user1)
                .build();
        booking1 = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item1)
                .booker(user2)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void testAddNew() {
        BookingDtoInput bookingDto = BookingDtoInput.builder()
                .itemId(item1.getId())
                .start(start)
                .end(end)
                .build();
        Long user2Id = user2.getId();

        Mockito
                .when(itemRepository.findById(user1.getId()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(user2Id))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocationOnMock -> {
                    Booking booking = invocationOnMock.getArgument(0, Booking.class);
                    booking.setId(1L);
                    return booking;
                });

        BookingUserDto actualBooking = bookingService.create(user2Id, bookingDto);

        assertThat(actualBooking.getId(), notNullValue());
        assertThat(actualBooking.getStart(), equalTo(start));
        assertThat(actualBooking.getEnd(), equalTo(end));
        assertThat(actualBooking.getItem(), equalTo(ItemMapper.toItemDto(item1)));
        assertThat(actualBooking.getBooker(), equalTo(UserMapper.toUserDto(user2)));
        assertThat(actualBooking.getStatus(), equalTo(BookingStatus.WAITING));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(item1.getId());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(user2Id);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(Mockito.any(Booking.class));
        Mockito.verifyNoMoreInteractions(itemRepository, userRepository, bookingRepository);
    }

    @Test
    void testAddNewWrongItemId() {
        BookingDtoInput bookingDto = BookingDtoInput.builder()
                .itemId(99L)
                .start(start)
                .end(end)
                .build();

        Mockito
                .when(userRepository.findById(user2.getId()))
                .thenReturn(Optional.of(user2));

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.create(user2.getId(), bookingDto));
    }

    @Test
    void testAddNewSameOwner() {
        BookingDtoInput input = BookingDtoInput.builder()
                .itemId(item1.getId())
                .start(start)
                .end(end)
                .build();
        Mockito
                .when(itemRepository.findById(item1.getId()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.create(user1.getId(), input));
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testAddNewNotAvailItem() {
        item1.setAvailable(false);
        BookingDtoInput bookingDto = BookingDtoInput.builder()
                .itemId(item1.getId())
                .start(start)
                .end(end)
                .build();
        Long user2Id = user2.getId();

        Mockito
                .when(itemRepository.findById(user1.getId()))
                .thenReturn(Optional.of(item1));
        Mockito
                .when(userRepository.findById(user2Id))
                .thenReturn(Optional.of(user2));

        Assertions.assertThrows(IncorrectParameterException.class,
                () -> bookingService.create(user2Id, bookingDto));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findById(item1.getId());
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testAddNewEndBeforeStart() {
        BookingDtoInput bookingDto = BookingDtoInput.builder()
                .itemId(item1.getId())
                .start(start.plusHours(1))
                .end(end)
                .build();
        Long user2Id = user2.getId();

        Mockito
                .when(userRepository.findById(user2Id))
                .thenReturn(Optional.of(user2));

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.create(user2Id, bookingDto));
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testAddNewStartSameAsEnd() {
        BookingDtoInput bookingDto = BookingDtoInput.builder()
                .itemId(item1.getId())
                .start(start.plusHours(1))
                .end(start.plusHours(1))
                .build();
        Long user2Id = user2.getId();

        Mockito
                .when(userRepository.findById(user2Id))
                .thenReturn(Optional.of(user2));

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.create(user2Id, bookingDto));
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testAddNewEndBeforeNow() {
        BookingDtoInput bookingDto = BookingDtoInput.builder()
                .itemId(item1.getId())
                .start(start.minusYears(1))
                .end(end.minusYears(1))
                .build();
        Long user2Id = user2.getId();

        Mockito
                .when(userRepository.findById(user2Id))
                .thenReturn(Optional.of(user2));

        Assertions.assertThrows(ValidationException.class,
                () -> bookingService.create(user2Id, bookingDto));
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testAddNewForOwnItem() {
        BookingDtoInput bookingDto = BookingDtoInput.builder()
                .itemId(item1.getId())
                .start(start)
                .end(end)
                .build();
        Long user2Id = user1.getId();

        Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.create(user2Id, bookingDto));
        Mockito.verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void testConfirmBooking() {
        Long bookingId = booking1.getId();
        boolean approved = true;
        Long itemOwnerId = booking1.getItem().getOwner().getId();
        Mockito
                .when(userRepository.findById(itemOwnerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking1));
        Mockito
                .when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0, Booking.class));

        BookingUserDto actualBooking = bookingService.confirm(bookingId, itemOwnerId, approved);

        assertThat(actualBooking.getId(), equalTo(bookingId));
        assertThat(actualBooking.getStatus(), equalTo(BookingStatus.APPROVED));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(bookingId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .save(booking1);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testGetBooking() {
        Long bookingId = booking1.getId();
        Long requesterId = booking1.getBooker().getId();
        Mockito
                .when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking1));
        Mockito
                .when(userRepository.findById(requesterId))
                .thenReturn(Optional.of(booking1.getBooker()));

        BookingUserDto actualBooking = bookingService.get(bookingId, requesterId);

        assertThat(actualBooking.getId(), equalTo(bookingId));
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findById(bookingId);
        Mockito.verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void testGetAllByBookerIdAndStateDefaultParams() {
        Long bookerId = 1L;
        String defaultState = "ALL";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByBookerIdOrderByStartDesc(
                        Mockito.anyLong(),
                        Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllBookerBookings(bookerId, defaultState, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(bookerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByBookerIdAndStateALL() {
        Long bookerId = 1L;
        String state = "ALL";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByBookerIdOrderByStartDesc(
                        Mockito.anyLong(),
                        Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(bookerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByBookerIdAndStatePAST() {
        Long bookerId = 1L;
        String state = "PAST";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(bookerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndEndBeforeOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByBookerIdAndStateCURRENT() {
        Long bookerId = 1L;
        String state = "CuRrEnT";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(bookerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        Mockito.anyLong(),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(LocalDateTime.class),
                        Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByBookerIdAndStateFUTURE() {
        Long bookerId = 1L;
        String state = "FUTURE";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(bookerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStartAfterOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(LocalDateTime.class), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByBookerIdAndStateWAITING() {
        Long bookerId = 1L;
        String state = "WAITING";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(BookingStatus.class), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(bookerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByBookerIdAndStatusOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(BookingStatus.class), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByBookerIdAndStateWrongState() {
        Long bookerId = 1L;
        String state = "ALLY";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(bookerId))
                .thenReturn(Optional.of(user1));

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize));
        assertThat(e.getMessage(), equalTo("Unknown state: " + state.toUpperCase()));
    }

    @Test
    void testGetAllByBookerIdAndStateWrongBookerId() {
        Long bookerId = 99L;
        String state = "ALL";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        ObjectNotFoundException e = Assertions.assertThrows(ObjectNotFoundException.class,
                () -> bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize));
        assertThat(e.getMessage(), equalTo("Пользователь с id = " + bookerId + " не найден"));
    }

    @Test
    void testGetAllByBookerIdAndStateWithInvalidPageParams() {
        Long bookerId = 1L;
        String state = "ALL";
        Integer defaultFromElement = 10;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(user1));

        ValidationException e = Assertions.assertThrows(ValidationException.class,
                () -> bookingService.getAllBookerBookings(bookerId, state, defaultFromElement, defaultSize));
        assertThat(e.getMessage(), equalTo("Некорректный ввод страниц и размеров"));
    }

    @Test
    void testGetAllByOwnerIdAndStateDefaultParams() {
        Long ownerId = 1L;
        String defaultState = "ALL";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllOwnerBookings(ownerId, defaultState, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(ownerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdOrderByStartDesc(Mockito.anyLong(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByOwnerIdAndStatePast() {
        Long ownerId = 1L;
        String defaultState = "PAST";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllOwnerBookings(ownerId, defaultState, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(ownerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByOwnerIdAndStateFuture() {
        Long ownerId = 1L;
        String defaultState = "FUTURE";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllOwnerBookings(ownerId, defaultState, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(ownerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByOwnerIdAndStateCurrent() {
        Long ownerId = 1L;
        String defaultState = "CURRENT";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllOwnerBookings(ownerId, defaultState, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(ownerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByOwnerIdAndStateWaiting() {
        Long ownerId = 1L;
        String defaultState = "WAITING";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllOwnerBookings(ownerId, defaultState, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(ownerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void testGetAllByOwnerIdAndStateRejected() {
        Long ownerId = 1L;
        String defaultState = "REJECTED";
        Integer defaultFromElement = 0;
        Integer defaultSize = 20;
        Mockito
                .when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(user1));
        Mockito
                .when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(new ArrayList<>());

        bookingService.getAllOwnerBookings(ownerId, defaultState, defaultFromElement, defaultSize);

        Mockito.verify(userRepository, Mockito.times(1))
                .findById(ownerId);
        Mockito.verify(bookingRepository, Mockito.times(1))
                .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        Mockito.anyLong(), Mockito.any(), Mockito.any(Pageable.class));
        Mockito.verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void bookingItemDtoTest() {
        BookingDtoInput input = BookingDtoInput.builder()
                .start(start)
                .end(end)
                .itemId(item1.getId())
                .build();

        Booking between = BookingMapper.fromBookingDtoInput(input, item1, user1, BookingStatus.APPROVED);

        assertThat(between.getStart(), equalTo(input.getStart()));
        assertThat(between.getEnd(), equalTo(input.getEnd()));
        assertThat(between.getStatus(), equalTo(BookingStatus.APPROVED));
        assertThat(between.getItem().getId(), equalTo(input.getItemId()));

        BookingItemDto betweenDto = BookingMapper.toBookingItemDto(between);

        assertThat(betweenDto.getStart(), equalTo(between.getStart()));
        assertThat(betweenDto.getEnd(), equalTo(between.getEnd()));
        assertThat(betweenDto.getBookerId(), equalTo(user1.getId()));
    }
}