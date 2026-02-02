package com.inventrik.digitalestore.config;

import com.inventrik.digitalestore.filter.CertificateSignatureFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CertificateAuthConfig {

    @Bean
    public FilterRegistrationBean<CertificateSignatureFilter> certificateSignatureFilterRegistration(
            CertificateSignatureFilter filter) {

        FilterRegistrationBean<CertificateSignatureFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/v1/cert-auth/*");
        registration.setOrder(1);
        return registration;
    }
}
