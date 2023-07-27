package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId,
                                                                LocalDateTime endBefore,
                                                                Pageable pageable);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId,
                                                                 LocalDateTime startAfter,
                                                                 Pageable pageable);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                             LocalDateTime startBefore,
                                                                             LocalDateTime endAfter,
                                                                             Pageable pageable);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId,
                                                             BookingStatus bookingStatus,
                                                             Pageable pageable);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long bookerId,
                                                       Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long bookerId,
                                                                   LocalDateTime endBefore,
                                                                   Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long bookerId,
                                                                    LocalDateTime startAfter,
                                                                    Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                LocalDateTime startBefore,
                                                                                LocalDateTime endAfter,
                                                                                Pageable pageable);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId,
                                                                BookingStatus bookingStatus,
                                                                Pageable pageable);

    @Query(value = "select * " +
            "FROM bookings " +
            "WHERE item_id  = :idItem " +
            "AND start_date < now() " +
            "ORDER BY end_date desc " +
            "limit 1", nativeQuery = true)
    Booking getLastBooking(@Param("idItem") Long id);

    @Query (value = "select * " +
            "FROM bookings " +
            "WHERE item_id  = :idItem " +
            "AND start_date > :time " +
            "ORDER BY start_date asc " +
            "limit 1", nativeQuery = true)
    Booking getNextBooking(@Param("idItem") Long id,
                           @Param("time") LocalDateTime time);

    @Query(value = "SELECT b1.* " +
            "FROM bookings b1 " +
            "JOIN (" +
                "SELECT item_id, MAX(start_date) as max_start_date " +
                "FROM BOOKINGS b2 " +
                "WHERE b2.ID in(" +
                    "SELECT b3.id FROM BOOKINGS b3 " +
                    "JOIN ITEMS i ON I.ID = B3.ITEM_ID " +
                    "WHERE i.OWNER_ID = ? " +
                    "AND b3.STATUS = 'APPROVED' " +
                    "AND b3.START_DATE < ?) " +
                "GROUP BY ITEM_ID) b2 " +
            "ON b1.item_id = b2.item_id AND b1.start_date = b2.max_start_date", nativeQuery = true)
    List<Booking> getLastBookings(Long ownerId, LocalDateTime startDate);

    @Query(value = "SELECT b1.* " +
            "FROM bookings b1 " +
            "JOIN (" +
                "SELECT item_id, MIN(start_date) as min_start_date " +
                "FROM BOOKINGS b2 " +
                "WHERE b2.ID in(" +
                    "SELECT b3.id FROM BOOKINGS b3 " +
                    "JOIN ITEMS i ON I.ID = B3.ITEM_ID " +
                    "WHERE i.OWNER_ID = ? " +
                    "AND b3.STATUS = 'APPROVED' " +
                    "AND b3.START_DATE >= ?) " +
                "GROUP BY ITEM_ID) b2 " +
            "ON b1.item_id = b2.item_id AND b1.start_date = b2.min_start_date", nativeQuery = true)
    List<Booking> getNextBookings(Long ownerId, LocalDateTime startDate);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId, Long userId, BookingStatus status, LocalDateTime endBefore);
}
