package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@Builder
public class ItemRequestDtoInput {
    private Long id;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;
}
