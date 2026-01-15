package dev.protik.moneymanager.repository;

import dev.protik.moneymanager.entity.IncomeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {
    // Select * from tbl_incomes where profile_id = ?1 order by date desc
    List<IncomeEntity> findByProfileIdOrderByAddedDateDesc(Long profileId);

    // Select * from tbl_incomes where profile_id = ?1 order by date desc limit 5
    List<IncomeEntity> findTop5ByProfileIdOrderByAddedDateDesc(Long profileId);

    @Query("SELECT SUM(e.amount) FROM IncomeEntity e WHERE e.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    // Select * from tbl_incomes where profile_id = ?1 AND date BETWEEN ?2 AND ?3 AND name LIKE %?4%
    List<IncomeEntity> findByProfileIdAndAddedDateBetweenAndNoteContainingIgnoreCase(
            Long profileId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String keyword,
            Sort sort);

    // Select * from tbl_incomes where profile_id = ?1 AND date BETWEEN ?2 AND ?3
    List<IncomeEntity> findByProfileIdAndAddedDateBetween(Long profileId, LocalDateTime startDate, LocalDateTime endDate);
}
