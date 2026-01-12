package dev.protik.moneymanager.controller;

import dev.protik.moneymanager.dto.ExpenseDTO;
import dev.protik.moneymanager.dto.FilterDTO;
import dev.protik.moneymanager.dto.IncomeDTO;
import dev.protik.moneymanager.service.ExpenseService;
import dev.protik.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filter) {
        LocalDateTime startDateTime = filter.getStartDate() != null
                ? filter.getStartDate().atStartOfDay()
                : LocalDateTime.MIN;

        LocalDateTime endDateTime = filter.getEndDate() != null
                ? filter.getEndDate().atTime(LocalTime.MAX)
                : LocalDateTime.now();

        String keyword = filter.getKeyword() != null ? filter.getKeyword() : "";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "addedDate";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortField()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);

        if("income".equalsIgnoreCase(filter.getType())){
            List<IncomeDTO> incomes =  incomeService.filterIncomes(startDateTime, endDateTime, keyword, sort);
            return ResponseEntity.ok(incomes);
        } else if ("expense".equalsIgnoreCase(filter.getType())){
            List<ExpenseDTO> expenses =  expenseService.filterExpenses(startDateTime, endDateTime, keyword, sort);
            return ResponseEntity.ok(expenses);
        } else {
            return ResponseEntity.badRequest().body("Invalid Type. Must be 'income' or 'expense'");
        }
    }
}
