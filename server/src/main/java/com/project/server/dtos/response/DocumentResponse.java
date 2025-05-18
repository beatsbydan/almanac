package com.project.server.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class DocumentResponse {
    byte[] bytesArr;

    String fileName;
}
