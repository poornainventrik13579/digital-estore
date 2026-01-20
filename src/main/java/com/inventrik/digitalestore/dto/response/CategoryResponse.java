package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String categoryId;
    private Integer tenantId;
    private String categoryName;
    private String description;
    private String status;
    private LocalDateTime created;
    private LocalDateTime updated;
}