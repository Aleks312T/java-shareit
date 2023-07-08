package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.ValidationException;
import java.util.*;

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
    public Item update(Integer userId, Integer itemId, Item item) {
        log.info("Обновление вещи");
        if (!isValidId(itemId)) {
            Item itemOrig = items.get(itemId);
            if (itemOrig.getOwner().getId().equals(userId)) {
                if (item.getName() != null) {
                    itemOrig.setName(item.getName());
                }

                if (item.getDescription() != null) {
                    itemOrig.setDescription(item.getDescription());
                }

                if (item.getAvailable() != null) {
                    itemOrig.setAvailable(item.getAvailable());
                }
            } else {
                throw new IllegalArgumentException("пользователь не является собственником указанной вещи");
            }
        } else {
            throw new ValidationException("вещь с данным id не существует");
        }
        Item itemUpd = items.get(itemId);
        log.trace("Вещь с id = {} обновлена", itemId);
        return  itemUpd;
    }

    private boolean isValidId(Integer id) {
        if (id == null || id == 0) {
            throw new ValidationException("У вещи некорректный id");
        } else return !items.containsKey(id);
    }
}
