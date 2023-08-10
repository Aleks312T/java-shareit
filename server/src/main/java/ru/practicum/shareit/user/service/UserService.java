package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto get(Long id);

    List<UserDto> getAll();

    UserDto update(Long id,UserDto userDto);

    void delete(Long id);

}
