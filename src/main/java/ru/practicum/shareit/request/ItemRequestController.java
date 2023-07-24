package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDtoInput createRequest(@Valid @RequestBody ItemRequestDtoInput itemRequestDto,
                                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestFullDto> getOwnerRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.getAll(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestFullDto> getUserRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                    @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                    @Positive @RequestParam(defaultValue = "10") Integer size) {
        return itemRequestService.getSort(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestFullDto getRequestById(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable Long requestId) {
        return itemRequestService.getById(requestId, userId);
    }
}
