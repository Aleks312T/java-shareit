package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(" SELECT it " +
            "FROM Item it " +
            "JOIN FETCH it.owner ow " +
            "WHERE ow.id = ?1 " +
            "ORDER BY it.id")
    List<Item> findAllByOwnerId(Long ownerId);

    @Query(" select it from Item it " +
            "where it.available = true " +
            "AND( upper(it.name) like upper(concat('%', ?1, '%')) " +
            " or upper(it.description) like upper(concat('%', ?1, '%')))")
    List<Item> search(String text);


}