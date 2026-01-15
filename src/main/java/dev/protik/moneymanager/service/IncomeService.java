package dev.protik.moneymanager.service;

import dev.protik.moneymanager.dto.ExpenseDTO;
import dev.protik.moneymanager.dto.IncomeDTO;
import dev.protik.moneymanager.entity.CategoryEntity;
import dev.protik.moneymanager.entity.ExpenseEntity;
import dev.protik.moneymanager.entity.IncomeEntity;
import dev.protik.moneymanager.entity.ProfileEntity;
import dev.protik.moneymanager.repository.CategoryRepository;
import dev.protik.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final IncomeRepository incomeRepository;

    // adding a income
    public IncomeDTO addIncome(IncomeDTO incomeDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + incomeDTO.getCategoryId()));
        IncomeEntity incomeEntity = toEntiry(incomeDTO, profile, category);
        incomeEntity = incomeRepository.save(incomeEntity);
        return toDTO(incomeEntity);
    }

    // Retrieves all incomes for the current month: based on the startDate and endDate
    public List<IncomeDTO> getIncomesForCurrentMonthForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDateTime startDate = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endDate   = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndAddedDateBetween(profile.getId(), startDate, endDate);
        return incomes.stream().map(this::toDTO).toList();
    }

    // delete expense by id for current user
    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity existingIncome = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found with id: " + incomeId));
        if(!existingIncome.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized: Income not found or accessible");
        }
        incomeRepository.delete(existingIncome);
    }


    // Get latest 5 incomes for current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser(){
        ProfileEntity profile =  profileService.getCurrentProfile();
        List<IncomeEntity> list =  incomeRepository.findTop5ByProfileIdOrderByAddedDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // Get Total Incomes of Current User
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpense = incomeRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpense != null ? totalExpense : BigDecimal.ZERO;
    }

    // filter incomes
    public List<IncomeDTO> filterIncomes(LocalDateTime startDate, LocalDateTime endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndAddedDateBetweenAndNoteContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    // helper method
    private IncomeEntity toEntiry(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .note(dto.getNote())
                .icon(dto.getIcon())
                .addedDate(dto.getAddedDate())
                .amount(dto.getAmount())
                .category(category)
                .profile(profile)
                .build();
    }
    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
                .id(entity.getId())
                .note(entity.getNote())
                .icon(entity.getIcon())
                .addedDate(entity.getAddedDate())
                .amount(entity.getAmount())
                .categoryId(entity.getCategory().getId() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory().getCategoryName() != null ? entity.getCategory().getCategoryName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
