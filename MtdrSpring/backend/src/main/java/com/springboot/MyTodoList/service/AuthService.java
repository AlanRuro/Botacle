package com.springboot.MyTodoList.service;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.model.Credential;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.repository.CredentialRepository;
import com.springboot.MyTodoList.repository.MemberRepository;

@Service
public class AuthService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CredentialRepository credentialRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean areCredentialsValid(MemberDto memberDto) {        
        Optional<Member> member = memberRepository.findByTelegramId(memberDto.getTelegramId());
        if(member.isPresent()) {
            Member registeredMember = member.get();
            Optional<Credential> memberCredentials = credentialRepository.findByMember(registeredMember); 
            if (memberCredentials.isPresent()) {
                Credential credentials = memberCredentials.get();
                return passwordEncoder.matches(memberDto.getPassword(), credentials.getPassword()) 
                        && (memberDto.getUsername().equals(credentials.getUsername()));
            }
        }
        return false;        
    }

    public boolean isMemberRegistered(long telegramId) {
        Optional<Member> member = memberRepository.findByTelegramId(telegramId);
        return member.isPresent();
    }
    
    public boolean isMemberAuthenticated(MemberDto memberDto) {
        if (isMemberRegistered(memberDto.getTelegramId())) {
            return areCredentialsValid(memberDto);        
        } 
        return false;
    }

    public void updateLoginStatus(Member member, boolean isLogged) {
        Optional<Credential> memberCredentials = credentialRepository.findByMember(member);
        if (memberCredentials.isPresent()) {
            Credential credentials = memberCredentials.get();
            credentials.setIs_logged(isLogged ? 1 : 0);
            credentialRepository.save(credentials);
        }
    }

    public boolean login(MemberDto memberDto) {
        if (isMemberAuthenticated(memberDto)) {
            Member member = memberRepository.findByTelegramId(memberDto.getTelegramId()).get();
            updateLoginStatus(member, true);
            return true;
        }
        return false;
    }

    public void logout(Member member) {
        updateLoginStatus(member, false);
    }

    public boolean isUserLoggedIn(long telegramId) {
        Optional<Member> member = memberRepository.findByTelegramId(telegramId);
        if (member.isPresent()) {
            Optional<Credential> credentials = credentialRepository.findByMember(member.get());
            return credentials.isPresent() && credentials.get().getIs_logged() == 1;
        }
        return false;
    }
}
