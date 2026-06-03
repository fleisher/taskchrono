package com.fleishera.taskchrono.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private String color;
}
