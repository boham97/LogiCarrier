package com.example.robotservice.jpa;

import com.example.robotservice.dto.CandidateDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Objects;

public interface ShelfStockRepository extends JpaRepository<ShelfStock, Long> {
    @Query(value = "SELECT t.stock_id, t.shelf_id id, shelf.x, shelf.y FROM (SELECT i.shelf_id, i.stock_id, i.created_time FROM shelf_stock i WHERE i.stock_id = :id ORDER BY i.created_time DESC LIMIT 3) AS t JOIN shelf ON t.shelf_id = shelf.id where shelf.status = true",
            nativeQuery = true)
    List<Object[]> findCandidate(@Param(value = "id") Long id);
}