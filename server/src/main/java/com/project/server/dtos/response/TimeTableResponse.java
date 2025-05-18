package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TimeTableResponse {
    private String code;

    private String fileName;

    private String status;

    private boolean canCreateSchedules;

    private String semester;

    private boolean active;

    private String session;

    private LocalDate creationStartDate;

    private LocalDate creationEndDate;
}
