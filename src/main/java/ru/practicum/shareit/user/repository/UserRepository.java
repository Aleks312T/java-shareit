package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    User create(User user);

    User get(Integer id);

    List<User> getAll();

    User update(Integer id,User user);
}
