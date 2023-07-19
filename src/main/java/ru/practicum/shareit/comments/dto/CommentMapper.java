package ru.practicum.shareit.comments.dto;

import ru.practicum.shareit.comments.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .author(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static Comment fromCommentDto(CommentDto commentDto, Item item, User user) {
        return new Comment(commentDto.getText(), item, user, commentDto.getCreated());
    }
}
