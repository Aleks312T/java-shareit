package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoInput create(Long userId, ItemRequest itemRequest);

    List<ItemRequestFullDto> getAll(Long userId, boolean filter);

    List<ItemRequestFullDto> getSort(Long userId, Integer from, Integer size);

    ItemRequestFullDto getById(Long userId, Long requestId);
}
