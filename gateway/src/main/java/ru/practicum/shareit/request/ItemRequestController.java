package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addNew(@RequestBody @Valid ItemRequestDto itemRequestDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.addNew(itemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllByPages(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                                @RequestParam(defaultValue = "20") @Min(1) Integer size) {
        return itemRequestClient.getAllByPages(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@PathVariable Long requestId,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getById(requestId, userId);
    }
}