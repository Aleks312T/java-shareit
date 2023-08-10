package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> newBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
											 @RequestBody @Valid BookItemRequestDto bookingDtoIn) {
		log.info("Создание бронирования с userId = {}", userId);
		if (bookingDtoIn.getStart().isAfter(bookingDtoIn.getEnd()) ||
				bookingDtoIn.getStart().isEqual(bookingDtoIn.getEnd())) {
			throw new ValidationException("время бронирования указано некорректно");
		}
		return bookingClient.newBooking(userId, bookingDtoIn);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> changeStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
											   @PathVariable Long bookingId,
											   @RequestParam(value = "approved") String approved) {
		log.info("смена статуса бронирования Id = {}",bookingId);
		return bookingClient.changeStatus(userId, bookingId, approved);
	}

	@GetMapping
	public ResponseEntity<Object> getBookings(@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId = {}, from = {}, size = {}", stateParam, userId, from, size);
		return bookingClient.getBookings(userId, state, from, size);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
											 @PathVariable Long bookingId) {
		log.info("Get booking {}, userId = {}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getBookingOwner(@RequestHeader("X-Sharer-User-Id") Long userId,
												  @RequestParam(name = "state", defaultValue = "all")
												  String stateParam,
												  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0")
												  Integer from,
												  @Positive @RequestParam(name = "size", defaultValue = "10")
												  Integer size) {
		log.info("Получение бронирования владельца с id = {}", userId);
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
		return bookingClient.getBookingOwner(userId, state, from, size);
	}
}
