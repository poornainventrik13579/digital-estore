package com.inventrik.digitalestore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicStoreInfoResponse {

    private String shopName;
    private String shopLogo;
    private String subdomain;
    private String countryRegion;
    private String baseCurrency;
    private Boolean multiCurrency;
    private String timezone;
}
