package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Integer, Item> items = new HashMap<>();
    private int itemId = 1;

    @Override
    public Item create(User user, Item item) {
        log.info("Создание новой вещи");
        item.setId(itemId++);
        item.setOwner(user);
        items.put(item.getId(),item);
        log.trace("Вещь создана");
        return item;
    }

    @Override
    public Item get(Integer id) {
        log.info("Получение вещи с id = {}", id);
        if (id == null) {
            throw new NullPointerException("Id вещи указан неверно");
        } else if (!items.containsKey(id)) {
            throw new IllegalArgumentException("Вещь с Id № " + id + " не найдена");
        }
        Item item = items.get(id);
        log.trace("Вещь с id = {} получена", id);
        return item;
    }

    @Override
    public Item update(Integer userId, Integer itemId, Item newItem) {
        log.info("Обновление вещи");
        if (!isValidId(itemId)) {
            Item oldItem = items.get(itemId);
            if (oldItem.getOwner().getId().equals(userId)) {
                if (newItem.getName() != null) {
                    oldItem.setName(newItem.getName());
                }

                if (newItem.getDescription() != null) {
                    oldItem.setDescription(newItem.getDescription());
                }

                if (newItem.getAvailable() != null) {
                    oldItem.setAvailable(newItem.getAvailable());
                }
            } else {
                throw new ObjectNotFoundException("Пользователь не является собственником указанной вещи");
                //По логике, должен стоять IllegalArgumentException, но Postman требует код 404
                //Я чего-то не понимаю?
                //throw new IllegalArgumentException("Пользователь не является собственником указанной вещи");
            }
        } else {
            throw new ObjectNotFoundException("Вещь с данным id не существует");
        }
        Item itemUpd = items.get(itemId);
        log.trace("Вещь с id = {} обновлена", itemId);
        return itemUpd;
    }

    @Override
    public void delete(Integer id) {
        log.info("Удаление вещи с id = {}", id);
        if (!isValidId(itemId)) {
            items.remove(id);
        } else {
            throw new ObjectNotFoundException("Вещь с данным id не существует");
        }
    }

    @Override
    public List<Item> search(String text) {
        log.info("Поиск вещи по тексту");
        if (!text.isBlank()) {
            String textLow = text.toLowerCase();
            return items.values().stream()
                    .filter(u -> u.getAvailable() &&
                            (u.getName().toLowerCase().contains(textLow) ||
                                    u.getDescription().toLowerCase().contains(textLow)))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Item> getAllItemUsers(Integer userId) {
        log.info("Вывод всех вещей пользователя с id = {}", userId);
        return items.values().stream()
                .filter(u -> u.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    private boolean isValidId(Integer id) {
        if (id == null || id == 0) {
            throw new ValidationException("У вещи некорректный id");
        } else return !items.containsKey(id);
    }
}
