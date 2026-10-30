package com.selling.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.selling.dto.get.LeadQtyDtoGet;
import com.selling.model.Lead;

public interface LeadRepo extends JpaRepository<Lead, Integer> {
    List<Lead> findAllByUserIdOrderByDateDesc(Long userId);

    List<Lead> findAllByUserIdAndDateGreaterThanEqualAndDateLessThanOrderByDateDesc(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @Query("SELECT new com.selling.dto.get.LeadQtyDtoGet(u.id, u.name, COUNT(l.id)) " +
            "FROM User u LEFT JOIN Lead l ON l.user.id = u.id " +
            "GROUP BY u.id, u.name ORDER BY u.id")
    List<LeadQtyDtoGet> findLeadQtyByUser();

    @Query("SELECT l FROM Lead l WHERE l.id = :leadId AND l.user.id = :userId")
    Optional<Lead> findByIdAndUserId(@Param("leadId") Integer leadId, @Param("userId") Long userId);

    @Query("""
    SELECT COUNT(l) > 0
    FROM Lead l
    WHERE (l.contact01 = :contact OR l.contact02 = :contact)
      AND l.date >= :startDate
      AND l.date < :endDate
    """)
    boolean existsByContactAndDate(
            @Param("contact") String contact,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
