package dev.protik.moneymanager.service;

import dev.protik.moneymanager.dto.ExpenseDTO;
import dev.protik.moneymanager.entity.CategoryEntity;
import dev.protik.moneymanager.entity.ExpenseEntity;
import dev.protik.moneymanager.entity.ProfileEntity;
import dev.protik.moneymanager.repository.CategoryRepository;
import dev.protik.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final CategoryRepository categoryRepository;
    private final ProfileService profileService;
    private final ExpenseRepository expenseRepository;

    // adding a expense
    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(expenseDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + expenseDTO.getCategoryId()));
        ExpenseEntity expenseEntity = toEntiry(expenseDTO, profile, category);
        expenseEntity = expenseRepository.save(expenseEntity);
        return toDTO(expenseEntity);
    }

    // Retrieves all expenses for the current month: based on the startDate and endDate
    public List<ExpenseDTO> getExpensesForCurrentMonthForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return expenses.stream().map(this::toDTO).toList();
    }

    // delete expense by id for current user
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity existingExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + expenseId));
        if(!existingExpense.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized: Expense not found or accessible");
        }
        expenseRepository.delete(existingExpense);
    }

    // Get latest 5 expenses for current user
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser(){
        ProfileEntity profile =  profileService.getCurrentProfile();
        List<ExpenseEntity> list =  expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // Get Total Expenses of Current User
    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpense = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpense != null ? totalExpense : BigDecimal.ZERO;
    }

    // filter expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    // helper method
    private ExpenseEntity toEntiry(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .addedDate(dto.getAddedDate())
                .amount(dto.getAmount())
                .addedDate(dto.getAddedDate())
                .category(category)
                .profile(profile)
                .build();
    }
    private ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .addedDate(entity.getAddedDate())
                .amount(entity.getAmount())
                .categoryId(entity.getCategory().getId() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory().getName() != null ? entity.getCategory().getName() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
