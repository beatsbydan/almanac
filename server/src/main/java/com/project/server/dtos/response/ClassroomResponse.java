package com.project.server.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassroomResponse {
    private String code;

    private int classSize;

    private String className;

    private String college;
}
