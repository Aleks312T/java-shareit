package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer,String> emails = new HashMap<>();

    private int idUser = 1;

    @Override
    public User create(User user) {
        log.info("Добавление пользователя");
        if (isValidEmail(null, user.getEmail()) || isValidId(user.getId())) {
            user.setId(idUser++);
            users.put(user.getId(),user);
            emails.put(user.getId(),user.getEmail());
        } else {
            throw new ValidationException("Пользователь с данным id уже существует");
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(Integer id, User user) {
        log.info("Обновление пользователя");
        if (!isValidId(id)) {
            User userOrig = users.get(id);
            if (user.getEmail() != null && isValidEmail(id, user.getEmail())) {
                userOrig.setEmail(user.getEmail());
                emails.put(userOrig.getId(), userOrig.getEmail());
            }
            if (user.getName() != null) {
                userOrig.setName(user.getName());
            }
        } else {
            throw new ValidationException("пользователь с данным id не существует");
        }
        User userUpd = users.get(id);
        log.trace("Пользователь с id = {} обновлен", id);
        return userUpd;
    }

    @Override
    public void delete(Integer id) {
        log.info("Удаление пользователя с id = {}", id);
        users.remove(id);
        emails.remove(id);
    }

    @Override
    public User get(Integer id) {
        log.info("Получения пользователя с id = {}", id);
        if (id == null) {
            throw new NullPointerException("Id пользователя указан неверно");
        } else if (!users.containsKey(id)) {
            throw new IllegalArgumentException("Пользователь с Id = " + id + " не найден");
        }
        User user = users.get(id);
        log.trace("Пользователь с id = {} получен", id);
        return  user;
    }

    private boolean isValidEmail(Integer id,String email) {
        if ((id != null && emails.containsValue(email) && !emails.get(id).equals(email))
                || (id == null && emails.containsValue(email))) {
            throw new ValidationException("Пользователь с данным email уже существует");
        }
        return true;
    }

    private boolean isValidId(Integer id) {
        if (id == null || id == 0) {
            throw new ValidationException("У пользователя некорректный id");
        } else return !users.containsKey(id);
    }
}
