package com.fleishera.taskchrono.mapper;

import com.fleishera.taskchrono.dto.TaskDto;
import com.fleishera.taskchrono.entity.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskDto toDto(Task task);
    Task toEntity(TaskDto taskDto);
}
