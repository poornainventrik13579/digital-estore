package com.inventrik.digitalestore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventrik.digitalestore.filter.CertificateSignatureFilter;
import com.inventrik.digitalestore.repository.UserRepository;
import com.inventrik.digitalestore.service.certificate.CertificateService;
import com.inventrik.digitalestore.util.CryptoUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CertificateAuthConfig {

    @Bean
    public CertificateSignatureFilter certificateSignatureFilter(
            CertificateService certificateService, CryptoUtil cryptoUtil,
            UserRepository userRepository, ObjectMapper objectMapper) {
        return new CertificateSignatureFilter(certificateService, cryptoUtil, userRepository, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<CertificateSignatureFilter> disableCertFilterAutoRegistration(
            CertificateSignatureFilter filter) {
        // Prevent Spring Boot from auto-registering this as a servlet filter.
        // It is already added as a Spring Security filter via addFilterBefore in AuthServerConfig.
        FilterRegistrationBean<CertificateSignatureFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
