package com.inventrik.digitalestore.api;

import com.inventrik.digitalestore.dto.response.TaxResponse;
import com.inventrik.digitalestore.service.tax.TaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/taxes")
@RequiredArgsConstructor
@Tag(name = "Global Tax Management", description = "Global APIs for managing taxes across all tenants")
@SecurityRequirement(name = "oauth2")
public class GlobalTaxController {

    private final TaxService taxService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get all taxes across all tenants")
    public ResponseEntity<List<TaxResponse>> getAllTaxes() {
        return ResponseEntity.ok(taxService.getAllTaxes());
    }
}
