package dev.protik.moneymanager.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterDTO {
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; // date, amount
    private String sortOrder; // asec, desc
}
