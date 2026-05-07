package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public CommentDto toDto(Comment comment) {
        if (comment == null) return null;
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}