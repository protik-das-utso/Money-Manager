package dev.protik.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncomeDTO {
    private Long id;
    private String icon;
    private String note;

    private String categoryName;
    private Long categoryId;
    private BigDecimal amount;
    // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime addedDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
