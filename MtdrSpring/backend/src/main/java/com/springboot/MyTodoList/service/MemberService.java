package com.springboot.MyTodoList.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.repository.MemberRepository;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public MemberDto getMemberById(int id) {
        Optional<Member> memberData = memberRepository.findById(id);
        if (memberData.isPresent()) {
            Member member = memberData.get();
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

    public boolean isManager(long telegramId) {
        MemberDto member = getMemberByTelegramId(telegramId);
        return member != null && member.isIsManager();
    }

    public List<MemberDto> getEmployeesByManagerId(long telegramId) {
        MemberDto manager = getMemberByTelegramId(telegramId);
        if (manager != null && manager.isIsManager()) {
            List<Member> employees = memberRepository.findByTeamId(manager.getTeamId());
            return employees.stream().map(this::toDto).collect(Collectors.toList());
        }
        return List.of();
    }

    private MemberDto toDto(Member member) {
        MemberDto memberDto = new MemberDto();
        memberDto.setName(member.getName());
        memberDto.setLastName(member.getLastName());
        memberDto.setEmail(member.getEmail());
        memberDto.setIsManager(member.getIsManager());
        memberDto.setTeamId(member.getTeam().getId());
        memberDto.setTelegramId(member.getTelegramId());
        memberDto.setUsername(member.getCredential().getUsername());
        memberDto.setPassword(member.getCredential().getPassword());
        return memberDto;
    }
}
