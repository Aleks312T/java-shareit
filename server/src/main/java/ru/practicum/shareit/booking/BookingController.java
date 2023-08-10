package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoInput;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingUserDto newBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestBody @Valid BookingDtoInput bookingDto) {
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingUserDto confirmBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long bookingId,
                                         @RequestParam boolean approved) {
        return bookingService.confirm(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingUserDto getBooking(@PathVariable Long bookingId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.get(bookingId, userId);
    }

    @GetMapping
    public List<BookingUserDto> getAllByBookerIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestParam(defaultValue = "ALL") String state,
                                                         @Min(0) @RequestParam(defaultValue = "0") Integer from,
                                                         @Min(1) @RequestParam(defaultValue = "20") Integer size) {
        return bookingService.getAllBookerBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingUserDto> getAllByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @RequestParam(defaultValue = "ALL") String state,
                                                        @Min(0) @RequestParam(defaultValue = "0") Integer from,
                                                        @Min(1) @RequestParam(defaultValue = "20") Integer size) {
        return bookingService.getAllOwnerBookings(userId, state, from, size);
    }
}
