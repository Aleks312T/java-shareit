package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingUserDto;

import java.util.List;

public interface BookingService {
    BookingUserDto create(Long userId, BookingUserDto bookingDto);

    BookingUserDto get(Long bookingId, Long userId);

    BookingUserDto approveBooking(Long bookingId, long userId, boolean approved);

    List<BookingUserDto> getAllOwnerBookings(Long userId, String state);

    List<BookingUserDto> getAllBookerBookings(Long userId, String state);

}
