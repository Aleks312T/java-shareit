package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ItemRequestFullDto {
    private Long id;

    @NotBlank
    private String description;

//    @NotNull
//    private User requestor;

    @NotNull
    private LocalDateTime created;

    private List<ItemDto> items;
}
