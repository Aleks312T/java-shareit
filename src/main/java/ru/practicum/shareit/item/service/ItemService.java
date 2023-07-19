package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comments.dto.CommentDto;
import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto get(Long itemId, Long userId);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    void delete(Long itemId);

    List<ItemDto> search(String text);

    List<ItemDto> getAllUserItems(Long userId);

    CommentDto createComment(CommentDto comment, Long userId, Long itemId);
}
