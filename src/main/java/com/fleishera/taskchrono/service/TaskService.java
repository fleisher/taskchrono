package com.fleishera.taskchrono.service;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.TimerSession;
import com.fleishera.taskchrono.entity.User;
import com.fleishera.taskchrono.repository.TaskRepository;
import com.fleishera.taskchrono.repository.TimerSessionRepository;
import com.fleishera.taskchrono.validation.TaskValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TimerSessionRepository timerSessionRepository;
    private final TaskValidator taskValidator;

    public List<Task> getTasks(User user) {
        return taskRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional
    public Task createTask(User user, String title, String description) {
        taskValidator.validateTitle(title);
        Task task = Task.builder()
                .user(user)
                .title(title)
                .description(description)
                .build();
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Task task) {
        List<TimerSession> timerSessions = timerSessionRepository.findAllByTask(task);
        if (!timerSessions.isEmpty()) {
            timerSessionRepository.deleteAll(timerSessions);
        }
        taskRepository.delete(task);
    }
}
