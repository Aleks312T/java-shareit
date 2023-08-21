package ru.practicum.shareit.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    @Email(message = "Некорректная электронная почта")
    private String email;
}
