package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TaskSession;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface TaskSessionRepository extends JpaRepository<TaskSession,Integer> {
    
    @Query("SELECT t FROM TaskSession t WHERE t.chatId = :id AND t.isFilled = false")
    public Optional<TaskSession> getByChatIdNotFilled(@Param("id") Long chatId);
}
