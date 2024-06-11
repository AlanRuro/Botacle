package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Member;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface MemberRepository extends JpaRepository<Member,Integer> {
    Optional<Member> findByTelegramId(long telegramId);
    List<Member> findByTeamId(int teamId);
}
