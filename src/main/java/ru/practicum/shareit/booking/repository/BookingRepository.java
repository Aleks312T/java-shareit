package ru.practicum.shareit.booking.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime endBefore);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime startAfter);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                             LocalDateTime startBefore,
                                                                             LocalDateTime endAfter);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime endBefore);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime startAfter);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId,
                                                                                LocalDateTime startBefore,
                                                                                LocalDateTime endAfter);

    List<Booking> findAllByItemOwnerIdAndStatus(Long ownerId, BookingStatus bookingStatus);

    List<Booking> findAllByStatus(BookingStatus bookingStatus);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus bookingStatus);

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

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId, Long userId, BookingStatus status, LocalDateTime endBefore);
}
