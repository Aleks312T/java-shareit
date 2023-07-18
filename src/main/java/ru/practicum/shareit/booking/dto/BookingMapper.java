package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class BookingMapper {
    public static BookingItemDto toBookingItemDto(Booking booking) {
        return BookingItemDto.builder()
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static BookingUserDto toBookingUserDto(Booking booking) {
        return BookingUserDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .build();
    }

    public static Booking fromBookingDto(BookingUserDto bookingDto, Item item, User user) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();

        //Проверка времени
        if (start == null || end == null) {
            throw new IncorrectParameterException("Время не может быть null");
        }
        if (start.isAfter(end) || start.isEqual(end) || start.isBefore(LocalDateTime.now())) {
            throw new IncorrectParameterException("Некорректное время");
        }

        return Booking.builder()
                .id(null)
                .start(start)
                .end(end)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }
}
