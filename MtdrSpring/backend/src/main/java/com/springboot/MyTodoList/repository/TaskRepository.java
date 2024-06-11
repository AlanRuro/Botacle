package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.Task;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface TaskRepository extends JpaRepository<Task,Integer>{
    List<Task> findAllByMember(Member member);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.id = :id")
    public void deleteTaskById(@Param("id") int id);
}
