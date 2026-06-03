package com.fleishera.taskchrono.service;

import com.fleishera.taskchrono.entity.*;
import com.fleishera.taskchrono.repository.TimerSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TimerService {

    private final TimerSessionRepository timerSessionRepository;

    public Optional<TimerSession> getActiveSession(User user) {
        return timerSessionRepository.findByUserAndStatus(user, TimerSessionStatus.ACTIVE);
    }

    public Optional<TimerSession> getActiveSession(User user, Task task) {
        return timerSessionRepository.findByUserAndTaskAndStatus(user, task, TimerSessionStatus.ACTIVE);
    }

    @Transactional
    public TimerSession startStopwatch(User user, Task task) {
        stopActiveSessionIfExists(user);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        TimerSession session = TimerSession.builder()
                .user(user)
                .task(task)
                .status(TimerSessionStatus.ACTIVE)
                .startedAt(now)
                .build();
        return timerSessionRepository.save(session);
    }

    @Transactional
    public void stopActiveSessionIfExists(User user) {
        getActiveSession(user).ifPresent(this::stopTimer);
    }

    @Transactional
    public void stopTimer(TimerSession session) {
        if (session.getStatus() == TimerSessionStatus.ACTIVE) {
            stopTimerAt(session, OffsetDateTime.now(ZoneOffset.UTC));
        }
    }

    @Transactional
    public void stopTimerAt(TimerSession session, OffsetDateTime finishedAt) {
        if (session.getStatus() != TimerSessionStatus.ACTIVE) {
            return;
        }

        OffsetDateTime utcFinishedAt = finishedAt.withOffsetSameInstant(ZoneOffset.UTC);
        session.setFinishedAt(utcFinishedAt);
        session.setStatus(TimerSessionStatus.FINISHED);
        session.setDurationSeconds(Math.max(0, Duration.between(session.getStartedAt(), utcFinishedAt).toSeconds()));

        timerSessionRepository.save(session);
    }
}
