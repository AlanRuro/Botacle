package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.repository.MemberRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public MemberDto getMemberById(int id) {
        Optional<Member> taskData = memberRepository.findById(id);
        if (taskData.isPresent()) {
            Member member = taskData.get();
            return toDto(member);
        }
        return null;
    }

    public MemberDto getMemberByTelegramId(long id) {
        Optional<Member> memberData = memberRepository.findByTelegramId(id);
        if (memberData.isPresent()) {
            Member member = memberData.get();
            return toDto(member);
        } else {
            return null;
        }
    }

    private MemberDto toDto(Member member) {
        MemberDto memberDto = new MemberDto();
        memberDto.setName(member.getName());
        memberDto.setLastName(member.getLastName());
        memberDto.setEmail(member.getEmail());
        memberDto.setIsManager(member.getIsManager());
        memberDto.setTeamId(member.getTeam().getId());
        memberDto.setTelegramId(member.getTelegramId());
        return memberDto;
    }
}
