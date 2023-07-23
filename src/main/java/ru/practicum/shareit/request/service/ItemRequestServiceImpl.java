package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoInput;
import ru.practicum.shareit.request.dto.ItemRequestFullDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemRequestServiceImpl implements ItemRequestService{
    @Override
    public ItemRequestDtoInput create(Long userId, ItemRequest itemRequest) {
        return null;
    }

    @Override
    public List<ItemRequestFullDto> getAll(Long userId, boolean filter) {
        return null;
    }

    @Override
    public List<ItemRequestFullDto> getSort(Long userId, Integer from, Integer size) {
        return null;
    }

    @Override
    public ItemRequestFullDto getById(Long userId, Long requestId) {
        return null;
    }
}
