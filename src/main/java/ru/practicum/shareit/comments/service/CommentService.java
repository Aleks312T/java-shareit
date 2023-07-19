package ru.practicum.shareit.comments.service;

import ru.practicum.shareit.comments.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto create(CommentDto commentDto);

    CommentDto get(Long id);

    List<CommentDto> getAll();

    CommentDto update(Long id, CommentDto userDto);

    void delete(Long id);

}
