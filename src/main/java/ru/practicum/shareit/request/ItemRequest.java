package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@AllArgsConstructor
@Builder
public class ItemRequest {
    private Integer id;
    @NotBlank
    private String description;
    @NotNull
    private User requestor;
    @NotNull
    private LocalDateTime created;
}
