/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.Credential;
import com.springboot.MyTodoList.model.Member;
import javax.transaction.Transactional;
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
