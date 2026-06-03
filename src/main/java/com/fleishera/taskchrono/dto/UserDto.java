package com.fleishera.taskchrono.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private Long telegramId;
    private String username;
}
