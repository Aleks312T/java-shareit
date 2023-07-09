package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.*;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Integer, User> users = new HashMap<>();

    private int idUser = 1;

    @Override
    public User create(User user) {
        log.info("Добавление пользователя");

        if (!isValidEmail(user.getEmail()))
            throw new ValidationException("У пользователя некорректный email");

        user.setId(idUser++);
        users.put(user.getId(), user);
        log.trace("Пользователь создан");
        return user;
    }

    @Override
    public List<User> getAll() {
        log.info("Получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(Integer id, User user) {
        log.info("Обновление пользователя");

        isValidId(id);
        if (!users.containsKey(id))
            throw new ObjectNotFoundException("Пользователь с Id = " + id + " не найден");

        User newUser = users.get(id);
        if (user.getEmail() != null) {
            if (isValidEmail(user.getEmail()) || newUser.getEmail().equals(user.getEmail()))
                newUser.setEmail(user.getEmail());
            else
                throw new ValidationException("У пользователя некорректный email");
        }
        if (user.getName() != null) {
            newUser.setName(user.getName());
        }

        users.put(id, newUser);

        User userUpd = users.get(id);
        log.trace("Пользователь с id = {} обновлен", id);
        return userUpd;
    }

    @Override
    public void delete(Integer id) {
        log.info("Удаление пользователя с id = {}", id);
        users.remove(id);
    }

    @Override
    public User get(Integer id) {
        log.info("Получения пользователя с id = {}", id);
        if (id == null) {
            throw new NullPointerException("Id пользователя указан неверно");
        } else if (!users.containsKey(id)) {
            throw new ObjectNotFoundException("Пользователь с Id = " + id + " не найден");
        }
        User user = users.get(id);
        log.trace("Пользователь с id = {} получен", id);
        return user;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.equals(""))
            throw new ValidationException("У пользователя некорректный email");
        else {
            return users.values().stream()
                    .noneMatch(user -> user.getEmail().equals(email));
        }

    }

    private boolean isValidId(Integer id) {
        if (id == null || id == 0) {
            throw new ValidationException("У пользователя некорректный id");
        } else
            return true;
    }
}
