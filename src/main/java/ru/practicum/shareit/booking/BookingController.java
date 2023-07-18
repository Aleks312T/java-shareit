package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public Booking addNew(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody @Valid BookingUserDto bookingDto) {
        return null;
    }

    @PatchMapping("/{bookingId}")
    public Booking confirmBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable Long bookingId,
                                  @RequestParam boolean approved) {
        return null;
    }

    @GetMapping("/{bookingId}")
    public Booking getBooking(@PathVariable Long bookingId,
                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        return null;
    }

    @GetMapping
    public List<Booking> getAllByBookerIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @RequestParam(defaultValue = "ALL") String state) {
        return null;
    }

    @GetMapping("/owner")
    public List<Booking> getAllByOwnerIdAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        return null;
    }
}
