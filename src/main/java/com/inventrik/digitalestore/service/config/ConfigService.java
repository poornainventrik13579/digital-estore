package com.inventrik.digitalestore.service.config;

import com.inventrik.digitalestore.domain.config.Config;
import com.inventrik.digitalestore.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;

    public Optional<String> getValue(Integer tenantId, String param) {
        return configRepository.findByParamAndTenantId(param, tenantId)
                .map(Config::getValue);
    }

    public String getPaymentProvider(Integer tenantId) {
        return getValue(tenantId, "PAYMENT_PROVIDER")
                .orElse("STRIPE_EMBED");
    }
}
