package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> findAllByRequest_Id(Long requestId);

    @Query("select i from Item i " +
            "where i.available = true " +
            "and (lower(i.name) like lower(concat('%', :text, '%')) " +
            "or lower(i.description) like lower(concat('%', :text, '%')))")
    List<Item> searchAvailableByText(@Param("text") String text);
}