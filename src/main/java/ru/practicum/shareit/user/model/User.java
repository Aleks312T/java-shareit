package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@Builder
public class User {
    private Integer id;
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
}
