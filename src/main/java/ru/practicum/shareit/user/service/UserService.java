package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto get(Integer id);

    List<UserDto> getAll();

    UserDto update(Integer id,UserDto userDto);

    void delete(Integer id);

}
