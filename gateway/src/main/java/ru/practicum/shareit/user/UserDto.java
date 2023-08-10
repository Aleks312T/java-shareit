package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    @NotNull(message = "Имя пользователя не может быть пустым")
    private String name;
    @NotBlank
    @Email(message = "Некорректная электронная почта")
    private String email;
}
