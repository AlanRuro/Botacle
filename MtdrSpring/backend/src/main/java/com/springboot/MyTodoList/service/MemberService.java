/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.springboot.MyTodoList.service;


import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.repository.MemberRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    public Member getMemberById(int id) {
        Optional<Member> taskData = memberRepository.findById(id);
        if (taskData.isPresent()){
            return taskData.get();
        } else{
            return null;
        }
    }
    
    public Member getMemberByTelegramId(long id) {
        Optional<Member> memberData = memberRepository.findByTelegramId(id);
        if (memberData.isPresent()){
            return memberData.get();
        } else{
            return null;
        }
    }
}
