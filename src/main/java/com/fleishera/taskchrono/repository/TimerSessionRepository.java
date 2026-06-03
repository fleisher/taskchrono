package com.fleishera.taskchrono.repository;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.TimerSession;
import com.fleishera.taskchrono.entity.TimerSessionStatus;
import com.fleishera.taskchrono.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimerSessionRepository extends JpaRepository<TimerSession, Long> {
    @EntityGraph(attributePaths = "task")
    Optional<TimerSession> findByUserAndStatus(User user, TimerSessionStatus status);

    @EntityGraph(attributePaths = "task")
    Optional<TimerSession> findByUserAndTaskAndStatus(User user, Task task, TimerSessionStatus status);

    @EntityGraph(attributePaths = "task")
    @Query("select s from TimerSession s where s.id = :id")
    Optional<TimerSession> findByIdWithTask(@Param("id") Long id);

    List<TimerSession> findAllByUserAndStatusAndFinishedAtAfter(User user, TimerSessionStatus status, OffsetDateTime finishedAt);

    List<TimerSession> findAllByUserAndTaskAndStatusOrderByStartedAtDesc(User user, Task task, TimerSessionStatus status);

    List<TimerSession> findAllByUserAndStatus(User user, TimerSessionStatus status);

    List<TimerSession> findAllByTask(Task task);
}
