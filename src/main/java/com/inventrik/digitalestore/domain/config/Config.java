package com.inventrik.digitalestore.domain.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "config")
@IdClass(Config.ConfigPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    @Id
    @Column(name = "param", nullable = false)
    private String param;

    @Id
    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "modified")
    private LocalDateTime modified;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "value")
    private String value;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfigPK implements Serializable {
        private String param;
        private Integer tenantId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigPK configPK = (ConfigPK) o;
            return Objects.equals(param, configPK.param) && Objects.equals(tenantId, configPK.tenantId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(param, tenantId);
        }
    }
}
