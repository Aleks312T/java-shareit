package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    //TODO добавить логирование
    @Transactional
    @Override
    public BookingUserDto create(Long userId, BookingDtoInput bookingDto) {
        User user = checkUser(userId);
        checkBooking(bookingDto);
        if(bookingDto.getItemId() == null)
            throw new ObjectNotFoundException("Предмет с id = " + bookingDto.getItemId() + " не найден");
        Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
        if(item.isEmpty())
            throw new ObjectNotFoundException("Предмет с id = " + bookingDto.getItemId() + " не найден");
        else {
            if (!item.get().getAvailable()) {
                throw new IncorrectParameterException("Статус данной вещи недоступен.");
            }
            if (item.get().getOwner().getId().equals(userId)) {
                throw new ObjectNotFoundException("Пользователь не может арендовать свою же вещь.");
            }

            Booking booking = BookingMapper.fromBookingDtoInput(bookingDto, item.get(), user, BookingStatus.WAITING);
            return BookingMapper.toBookingUserDto(bookingRepository.save(booking));
        }
    }

    @Transactional
    @Override
    public BookingUserDto get(Long bookingId, Long userId) {
        checkUser(userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        if(booking.isEmpty()) {
            throw new ObjectNotFoundException("Бронирование с id = " + bookingId + " не найдено.");
        } else {
            checkBooking(booking.get());
            if (!booking.get().getBooker().getId().equals(userId)
                    && !booking.get().getItem().getOwner().getId().equals(userId)) {
                //UnauthorizedAccessException
                throw new ObjectNotFoundException(
                        "Пользователь с id = " + userId + " не может одобрить бронирование");
            }
            return BookingMapper.toBookingUserDto(booking.get());
        }
    }

    @Override
    public BookingUserDto confirm(Long bookingId, long userId, boolean approved) {
        checkUser(userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if(booking.isEmpty()) {
            throw new ObjectNotFoundException("Бронирование с id = " + bookingId + " не найдено.");
        } else {
            if (booking.get().getItem().getOwner().getId().equals(userId)
                    && booking.get().getStatus().equals(BookingStatus.APPROVED)) {
                throw new ValidationException("Вещь уже забронирована.");
            }

            if (!booking.get().getItem().getOwner().getId().equals(userId)) {
                throw new ObjectNotFoundException(
                        "Пользователь с id = " + userId + " не является владельцем бронирование");
            }

            booking.get().setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
            return BookingMapper.toBookingUserDto(bookingRepository.save(booking.get()));
        }
    }

    @Override
    public List<BookingUserDto> getAllOwnerBookings(Long ownerId, String state) {
        checkUser(ownerId);
        List<Booking> result;
        try {
            BookingState status = BookingState.valueOf(state);
            switch (status) {
                case ALL:
                    result = bookingRepository
                            .findAllByItemOwnerIdOrderByStartDesc(ownerId);
                    return BookingMapper.fromListBooking(result);
                case PAST:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, LocalDateTime.now());
                    return BookingMapper.fromListBooking(result);
                case FUTURE:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, LocalDateTime.now());
                    return BookingMapper.fromListBooking(result);
                case CURRENT:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId,
                            LocalDateTime.now(), LocalDateTime.now());
                    return BookingMapper.fromListBooking(result);
                case WAITING:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
                    return BookingMapper.fromListBooking(result);
                case REJECTED:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
                    return BookingMapper.fromListBooking(result);
                default:
                    throw new IncorrectParameterException("Unknown state: " + state);
            }
        } catch (Exception e) {
            throw new IncorrectParameterException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingUserDto> getAllBookerBookings(Long bookerId, String state) {
        checkUser(bookerId);
        List<Booking> result;
        try {
            BookingState status = BookingState.valueOf(state);
            switch (status) {
                case ALL:
                    result = bookingRepository
                            .findAllByBookerIdOrderByStartDesc(bookerId);
                    return BookingMapper.fromListBooking(result);
                case PAST:
                    result = bookingRepository
                            .findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now());
                    return BookingMapper.fromListBooking(result);
                case FUTURE:
                    result = bookingRepository
                            .findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now());
                    return BookingMapper.fromListBooking(result);
                case CURRENT:
                    result = bookingRepository
                            .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId,
                            LocalDateTime.now(), LocalDateTime.now());
                    return BookingMapper.fromListBooking(result);
                case WAITING:
                    result = bookingRepository
                            .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
                    return BookingMapper.fromListBooking(result);
                case REJECTED:
                    result = bookingRepository
                            .findAllByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
                    return BookingMapper.fromListBooking(result);
                default:
                    throw new IncorrectParameterException("Unknown state: " + state);
            }
        } catch (Exception e) {
            throw new IncorrectParameterException("Unknown state: " + state);
        }
    }

    public User checkUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()) {
            return user.get();
        }
        else {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public void checkBooking(Booking booking) {
        if(booking.getEnd().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(booking.getStart())
                || booking.getEnd().isEqual(booking.getStart())) {
            throw new IncorrectParameterException("Ошибка во времени бронирования");
        }
    }

    public void checkBooking(BookingUserDto booking) {
        if(booking.getEnd().isBefore(LocalDateTime.now())
            || booking.getEnd().isBefore(booking.getStart())
            || booking.getEnd().isEqual(booking.getStart())) {
            throw new IncorrectParameterException("Ошибка во времени бронирования");
        }
    }

    public void checkBooking(BookingDtoInput booking) {
        if(booking.getEnd().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(booking.getStart())
                || booking.getEnd().isEqual(booking.getStart())) {
            throw new IncorrectParameterException("Ошибка во времени бронирования");
        }
    }

}
