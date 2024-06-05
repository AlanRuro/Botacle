package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.repository.MemberRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    public MemberDto getMemberById(int id) {
        Optional<Member> memberOpt = memberRepository.findById(id);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            return toDto(member);
        }
        return null;
    }

    public List<MemberDto> getEmployeesByTelegramId(long telegramId) {
        MemberDto manager = getMemberByTelegramId(telegramId);
        if (manager != null && manager.getIsManager()) {
            List<Member> employees = memberRepository.findByTeamId(manager.getTeamId());
            return employees.stream().map(this::toDto).collect(Collectors.toList());
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
