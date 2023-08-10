package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.IncorrectParameterException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

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

    @Transactional
    @Override
    public BookingUserDto create(Long userId, BookingDtoInput bookingDto) {
        log.debug("Вызов метода create");
        User user = checkUser(userId);
        checkBooking(bookingDto);
        if (bookingDto.getItemId() == null)
            throw new ObjectNotFoundException("Предмет с id = " + bookingDto.getItemId() + " не найден");
        Optional<Item> item = itemRepository.findById(bookingDto.getItemId());
        if (item.isEmpty())
            throw new ObjectNotFoundException("Предмет с id = " + bookingDto.getItemId() + " не найден");
        else {
            if (!item.get().getAvailable()) {
                throw new IncorrectParameterException("Статус данной вещи недоступен.");
            }
            if (item.get().getOwner().getId().equals(userId)) {
                throw new ObjectNotFoundException("Пользователь не может арендовать свою же вещь.");
            }

            Booking booking = BookingMapper.fromBookingDtoInput(bookingDto, item.get(), user, BookingStatus.WAITING);
            booking = bookingRepository.save(booking);
            log.trace("Создана бронь с id = {}", booking.getId());
            return BookingMapper.toBookingUserDto(booking);
        }
    }

    @Transactional
    @Override
    public BookingUserDto get(Long bookingId, Long userId) {
        log.debug("Вызов метода get с bookingId = {}, userId = {}", bookingId, userId);
        checkUser(userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        if (booking.isEmpty()) {
            throw new ObjectNotFoundException("Бронирование с id = " + bookingId + " не найдено.");
        } else {
            //checkBooking(booking.get());
            if (!booking.get().getBooker().getId().equals(userId)
                    && !booking.get().getItem().getOwner().getId().equals(userId)) {
                // UnauthorizedAccessException логичнее, но тесты считают иначе
                throw new ObjectNotFoundException("Пользователь с id = " + userId + " не может одобрить бронирование");
            }
            log.trace("Завершение вызова метода get");
            return BookingMapper.toBookingUserDto(booking.get());
        }
    }

    @Transactional
    @Override
    public BookingUserDto confirm(Long bookingId, Long userId, Boolean approved) {
        log.debug("Вызов метода confirm с bookingId = {}, userId = {}", bookingId, userId);
        checkUser(userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
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
            Booking result = bookingRepository.save(booking.get());
            log.trace("Завершение вызова метода confirm");
            return BookingMapper.toBookingUserDto(result);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingUserDto> getAllOwnerBookings(Long ownerId, String state, Integer fromElement, Integer size) {
        log.debug("Вызов метода getAllOwnerBookings с ownerId = {}, state = {}", ownerId, state);
        checkUser(ownerId);
        List<Booking> result;

        checkPages(fromElement, size);
        int fromPage = fromElement / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        try {
            BookingState status = BookingState.valueOf(state);
            switch (status) {
                case ALL:
                    result = bookingRepository
                            .findAllByItemOwnerIdOrderByStartDesc(ownerId, pageable);
                    break;
                case PAST:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
                                    ownerId, LocalDateTime.now(), pageable);
                    break;
                case FUTURE:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
                                    ownerId, LocalDateTime.now(), pageable);
                    break;
                case CURRENT:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                    ownerId, LocalDateTime.now(), LocalDateTime.now(), pageable);
                    break;
                case WAITING:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                                    ownerId, BookingStatus.WAITING, pageable);
                    break;
                case REJECTED:
                    result = bookingRepository
                            .findAllByItemOwnerIdAndStatusOrderByStartDesc(
                                    ownerId, BookingStatus.REJECTED, pageable);
                    break;
                default:
                    throw new IncorrectParameterException("Unknown state: " + state);
            }
            log.trace("Завершение вызова метода getAllOwnerBookings");
            return BookingMapper.fromListBooking(result);
        } catch (Exception e) {
            throw new IncorrectParameterException("Unknown state: " + state);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingUserDto> getAllBookerBookings(Long bookerId, String state, Integer fromElement, Integer size) {
        log.debug("Вызов метода getAllBookerBookings с bookerId = {}, state = {}", bookerId, state);
        checkUser(bookerId);
        List<Booking> result;

        checkPages(fromElement, size);
        int fromPage = fromElement / size;
        Pageable pageable = PageRequest.of(fromPage, size);
        switch (state.toUpperCase()) {
            case "ALL":
                result = bookingRepository.findAllByBookerIdOrderByStartDesc(bookerId, pageable);
                break;
            case "PAST":
                result = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                        bookerId, LocalDateTime.now(), pageable);
                break;
            case "FUTURE":
                result = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(
                        bookerId, LocalDateTime.now(), pageable);
                break;
            case "CURRENT":
                result = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        bookerId, LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case "WAITING":
                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.WAITING, pageable);
                break;
            case "REJECTED":
                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        bookerId, BookingStatus.REJECTED, pageable);
                break;
            default:
                throw new ValidationException(String.format("Unknown state: %s", state.toUpperCase()));
        }
        log.trace("Завершение вызова метода getAllOwnerBookings");
        return BookingMapper.fromListBooking(result);
    }

    public User checkUser(Long userId) {
        log.trace("Вызов метода checkUser с userId = {}", userId);
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new ObjectNotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

//    public void checkBooking(Booking booking) {
//        log.trace("Вызов метода checkBooking");
//        if (booking.getEnd().isBefore(LocalDateTime.now()))
//            throw new ValidationException("Ошибка во времени бронирования: " +
//                    "оно должно закончиться в будущем времени.");
//        else
//        if (booking.getEnd().isBefore(booking.getStart()))
//            throw new ValidationException("Ошибка во времени бронирования: " +
//                    "конец бронирования должен быть после его начала.");
//        else
//        if (booking.getEnd().isEqual(booking.getStart()))
//            throw new ValidationException("Ошибка во времени бронирования: " +
//                    "время начала не может совпадать с временем окончания. ");
//    }

    public void checkBooking(BookingDtoInput booking) {
        log.trace("Вызов метода checkBooking");
        if (booking.getEnd().isBefore(LocalDateTime.now()))
            throw new ValidationException("Ошибка во времени бронирования: " +
                    "оно должно закончиться в будущем времени.");
        else
        if (booking.getEnd().isBefore(booking.getStart()))
            throw new ValidationException("Ошибка во времени бронирования: " +
                    "конец бронирования должен быть после его начала.");
        else
        if (booking.getEnd().isEqual(booking.getStart()))
            throw new ValidationException("Ошибка во времени бронирования: " +
                    "время начала не может совпадать с временем окончания. ");
    }

    public void checkPages(Integer fromElement, Integer size) {
        log.trace("Вызов метода checkPages");
        if (fromElement % size != 0) {
            throw new ValidationException("Некорректный ввод страниц и размеров");
        }
    }

}
