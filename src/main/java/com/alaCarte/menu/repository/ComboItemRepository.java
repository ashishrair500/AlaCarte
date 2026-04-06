package com.alaCarte.menu.repository;

import com.alaCarte.entity.ComboItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComboItemRepository extends JpaRepository<ComboItem, UUID> {

    @Query("""
        SELECT ci FROM ComboItem ci
        JOIN FETCH ci.menuItem mi
        LEFT JOIN FETCH ci.variant v
        LEFT JOIN FETCH ci.substitutionCategory sc
        WHERE ci.combo.id = :comboId
          AND ci.deleted = 0
        ORDER BY ci.displayOrder ASC
        """)
    List<ComboItem> findByComboIdWithDetails(@Param("comboId") UUID comboId);

    Optional<ComboItem> findByIdAndComboIdAndDeleted(UUID id, UUID comboId, Short deleted);

    Integer countByComboIdAndDeleted(UUID comboId, Short deleted);
}
