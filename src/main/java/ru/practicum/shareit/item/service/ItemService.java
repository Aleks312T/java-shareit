package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Integer userId, ItemDto itemDto);

    ItemDto get(Integer id);

    ItemDto update(Integer userId, Integer itemId, ItemDto itemDto);

    void delete(Integer itemId);

    List<ItemDto> search(String text);

    List<ItemDto> getAllItemUsers(Integer userId);
}
