package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ItemRequestDto {
    private Long id;

    @NotBlank
    private String description;

    @NotNull
    private User requestor;

    @NotNull
    private LocalDateTime created;
}
