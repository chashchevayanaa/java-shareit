package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentDto toDto(Comment comment) {
        if (comment == null) return null;

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor() != null
                        ? comment.getAuthor().getName()
                        : null)
                .created(comment.getCreated())
                .build();
    }
}