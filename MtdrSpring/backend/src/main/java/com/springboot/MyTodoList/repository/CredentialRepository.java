package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.Credential;
import com.springboot.MyTodoList.model.Member;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface CredentialRepository extends JpaRepository<Credential,Integer>  {
    Optional<Credential> findByMember(Member member);
}
