package com.fleishera.taskchrono.service;

import com.fleishera.taskchrono.entity.*;
import com.fleishera.taskchrono.repository.TaskRepository;
import com.fleishera.taskchrono.repository.TimerSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TaskRepository taskRepository;
    private final TimerSessionRepository timerSessionRepository;

    public List<Task> getUserTasks(User user) {
        return taskRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    public long getTotalSecondsForTask(User user, Task task) {
        return getFinishedSessionsForTask(user, task)
                .stream()
                .mapToLong(s -> s.getDurationSeconds() != null ? s.getDurationSeconds() : 0)
                .sum();
    }

    public long getSecondsSince(User user, OffsetDateTime since) {
        return timerSessionRepository.findAllByUserAndStatusAndFinishedAtAfter(user, TimerSessionStatus.FINISHED, since)
                .stream()
                .mapToLong(s -> s.getDurationSeconds() != null ? s.getDurationSeconds() : 0)
                .sum();
    }

    public long getTotalSeconds(User user) {
        return timerSessionRepository.findAllByUserAndStatus(user, TimerSessionStatus.FINISHED)
                .stream()
                .mapToLong(s -> s.getDurationSeconds() != null ? s.getDurationSeconds() : 0)
                .sum();
    }

    public List<TimerSession> getFinishedSessionsForTask(User user, Task task) {
        return timerSessionRepository.findAllByUserAndTaskAndStatusOrderByStartedAtDesc(
                user, task, TimerSessionStatus.FINISHED);
    }

    public long getCurrentDurationSeconds(TimerSession session) {
        return Math.max(0, Duration.between(session.getStartedAt(), OffsetDateTime.now(ZoneOffset.UTC)).toSeconds());
    }

    public String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return String.format("%dч %dм", hours, minutes);
        } else {
            return String.format("%dм", minutes);
        }
    }
}
