package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class BookingMapper {
    public static BookingDtoInput toBookingDtoInput(Booking booking) {
        return BookingDtoInput.builder()
                .itemId(booking.getItem().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }

    public static Booking fromBookingDtoInput(BookingDtoInput bookingDto, Item item, User user, BookingStatus status) {
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
                .status(status)
                .build();
    }

    public static BookingItemDto toBookingItemDto(Booking booking) {
        return BookingItemDto.builder()
                .bookerId(booking.getBooker().getId())
                .id(booking.getId())
                .build();
    }

    public static BookingUserDto toBookingUserDto(Booking booking) {
        return BookingUserDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .item(ItemMapper.toItemDto(booking.getItem()))
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .build();
    }

    public static Booking fromBookingUserDto(BookingUserDto bookingDto) {
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
                .item(ItemMapper.fromItemDto(bookingDto.getItem()))
                .booker(UserMapper.fromUserDto(bookingDto.getBooker()))
                .status(BookingStatus.WAITING)
                .build();
    }

    public static List<BookingUserDto> fromListBooking(List<Booking> input) {
        return input.stream()
                .map(BookingMapper::toBookingUserDto).collect(Collectors.toList());
    }
}
