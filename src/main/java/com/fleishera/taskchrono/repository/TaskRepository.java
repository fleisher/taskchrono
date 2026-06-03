package com.fleishera.taskchrono.repository;

import com.fleishera.taskchrono.entity.Task;
import com.fleishera.taskchrono.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByUserOrderByCreatedAtDesc(User user);

    List<Task> findAllByUser(User user);
}
