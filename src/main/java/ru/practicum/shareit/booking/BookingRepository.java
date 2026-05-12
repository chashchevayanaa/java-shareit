package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemIdAndStatusOrderByStartAsc(Long itemId, Booking.BookingStatus status);

    long countByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime now);

    @Query("select b from Booking b join b.item i where i.id = :itemId " +
            "and b.start < :now and b.status = 'APPROVED' order by b.start desc")
    List<Booking> findPastApprovedBookings(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b join b.item i where i.id = :itemId " +
            "and b.start > :now and b.status = 'APPROVED' order by b.start asc")
    List<Booking> findFutureApprovedBookings(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = :userId and b.status = :status")
    List<Booking> findByBookerIdAndStatus(@Param("userId") Long userId,
                                          @Param("status") Booking.BookingStatus status,
                                          Sort sort);

    @Query("select b from Booking b where b.item.owner.id = :ownerId and b.status = :status")
    List<Booking> findByOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                         @Param("status") Booking.BookingStatus status,
                                         Sort sort);

    @Query("select b from Booking b where b.booker.id = :bookerId " +
            "and b.start < :now and b.end > :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                        @Param("now") LocalDateTime now,
                                        Sort sort);

    @Query("select b from Booking b where b.booker.id = :bookerId and b.end < :now")
    List<Booking> findPastByBookerId(@Param("bookerId") Long bookerId,
                                     @Param("now") LocalDateTime now,
                                     Sort sort);

    @Query("select b from Booking b where b.booker.id = :bookerId and b.start > :now")
    List<Booking> findFutureByBookerId(@Param("bookerId") Long bookerId,
                                       @Param("now") LocalDateTime now,
                                       Sort sort);

    @Query("select b from Booking b where b.item.owner.id = :ownerId " +
            "and b.start < :now and b.end > :now")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId,
                                       @Param("now") LocalDateTime now,
                                       Sort sort);

    @Query("select b from Booking b where b.item.owner.id = :ownerId and b.end < :now")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId,
                                    @Param("now") LocalDateTime now,
                                    Sort sort);

    @Query("select b from Booking b where b.item.owner.id = :ownerId and b.start > :now")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId,
                                      @Param("now") LocalDateTime now,
                                      Sort sort);
}