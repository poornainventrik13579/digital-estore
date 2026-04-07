package com.inventrik.digitalestore.repository;

import com.inventrik.digitalestore.domain.config.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Config, Config.ConfigPK> {

    Optional<Config> findByParamAndTenantId(String param, Integer tenantId);

    List<Config> findByTenantId(Integer tenantId);
}
