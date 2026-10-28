package com.project.server.db.models;

import com.project.server.enums.GenerationStage;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "generation_trackers")
@Entity
@EqualsAndHashCode(callSuper = false)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationTracker extends TenantOwnedModel{
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GenerationStage generationStage = GenerationStage.UWC;
}
