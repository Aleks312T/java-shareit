package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestFullDto create(Long userId, ItemRequestDtoInput itemRequest);

    List<ItemRequestFullDto> getAll(Long userId);

    List<ItemRequestFullDto> getSort(Long userId, Integer from, Integer size);

    ItemRequestFullDto getById(Long userId, Long requestId);
}
