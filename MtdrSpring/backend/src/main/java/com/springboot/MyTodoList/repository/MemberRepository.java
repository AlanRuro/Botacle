package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Member;
import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface MemberRepository extends JpaRepository<Member,Integer> {
    Optional<Member> findByTelegramId(long telegramId);
    List<Member> findByTeamId(int teamId);
    
    @Query("SELECT m FROM Member m WHERE m.team.id = :teamId AND m.id <> :managerId")
    List<Member> findMembersOfManager(@Param("managerId") int managerId, @Param("teamId") int teamId);
}
