package com.project.server.db.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Builder
@Table(name = "otps")
@NoArgsConstructor
@AllArgsConstructor
public class Otp extends TenantOwnedModel{
    private String otp;

    @Builder.Default
    private long expiration = 900;
}
