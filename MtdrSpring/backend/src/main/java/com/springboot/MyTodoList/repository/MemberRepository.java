package com.springboot.MyTodoList.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.springboot.MyTodoList.model.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findByTelegramId(long telegramId);
    List<Member> findByTeamId(int teamId); // Ajustamos para obtener empleados por team_id
}
