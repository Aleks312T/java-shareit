package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addNew(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Создание запроса с userId = {}", userId);
        return itemRequestClient.addNew(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получение всех запросов с userId = {}", userId);
        return itemRequestClient.getAllOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllByPages(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "20") @Positive Integer size) {
        log.info("Получение всех запросов по страницам с userId = {}", userId);
        return itemRequestClient.getAllByPages(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@PathVariable Long requestId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получение конкретного запроса с userId = {}, requestId = {}", userId, requestId);
        return itemRequestClient.getById(requestId, userId);
    }
}