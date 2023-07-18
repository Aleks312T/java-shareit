package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.exceptions.UnauthorizedAccessException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
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
    public BookingUserDto create(Long userId, BookingUserDto bookingDto) {
        User user = checkUser(userId);
        Optional<Item> item = itemRepository.findById(bookingDto.getItem().getId());
        if(item.isEmpty())
            throw new ObjectNotFoundException("Предмет с id = " + bookingDto.getItem().getId() + " не найден");
        else {
            if (!item.get().getAvailable()) {
                throw new IncorrectParameterException("Статус данной вещи недоступен.");
            }
            if (item.get().getOwner().getId().equals(userId)) {
                throw new IncorrectParameterException("Пользователь не может арендовать свою же вещь.");
            }

            Booking booking = BookingMapper.fromBookingDto(bookingDto, item.get(), user);
            return BookingMapper.toBookingUserDto(bookingRepository.save(booking));
        }
    }

    @Transactional
    @Override
    public BookingUserDto get(Long bookingId, Long userId) {
        User user = checkUser(userId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if(booking.isEmpty()) {
            throw new ObjectNotFoundException("Бронирование с id = " + bookingId + " не найдено.");
        } else {
            if (!booking.get().getBooker().getId().equals(userId)
                    && !booking.get().getItem().getOwner().getId().equals(userId)) {
                throw new UnauthorizedAccessException(
                        "Пользователь с id = " + userId + " не может одобрить бронирование");
            }
            return BookingMapper.toBookingUserDto(booking.get());
        }
    }

    @Override
    public BookingUserDto approveBooking(Long bookingId, long userId, boolean approved) {
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
                throw new UnauthorizedAccessException(
                        "Пользователь с id = " + userId + " не является владельцем бронирование");
            }

            booking.get().setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
            return BookingMapper.toBookingUserDto(bookingRepository.save(booking.get()));
        }
    }

    @Override
    public List<BookingUserDto> getAllOwnerBookings(Long userId, String state) {
        return null;
    }

    @Override
    public List<BookingUserDto> getAllBookerBookings(Long userId, String state) {
        return null;
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
}
