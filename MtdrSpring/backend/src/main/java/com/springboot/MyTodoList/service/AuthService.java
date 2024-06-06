package com.springboot.MyTodoList.service;


import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.model.Credential;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.repository.CredentialRepository;
import com.springboot.MyTodoList.repository.MemberRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private CredentialRepository credentialRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

//    public boolean areCredentialsValid(MemberDto memberDto) {        
//         Optional<Member> member = memberRepository.findByTelegramId(memberDto.getTelegramId());
//         if(member.isPresent()) {
//             Member registeredMember = member.get();
//             Optional<Credential> memberCredentials = credentialRepository.findByMember(registeredMember); 
//             if (memberCredentials.isPresent()) {
//                 Credential credentials = memberCredentials.get();
//                 return passwordEncoder.matches(memberDto.getPassword(), credentials.getPassword()) 
//                         && (memberDto.getUsername().equals(credentials.getUsername()));
//             }
//         }
//         return false;        
//     }

//    public void registerMember(String username, String password) {
//        String sql = "INSERT INTO members (username, password) VALUES (?, ?)";
//        jdbcTemplate.update(sql, username, passwordEncoder.encode(password));
//    }

    public boolean isMemberRegistered(long telegramId) {
        Optional<Member> member = memberRepository.findByTelegramId(telegramId);
        return member.isPresent();
    }
    
    // public boolean isMemberAuthenticated(MemberDto memberDto) {
    //     if (isMemberRegistered(memberDto.getTelegramId())) {
    //         return areCredentialsValid(memberDto);        
    //     } 
    //     return false;
    // }

    public void updateJwtToken(long telegramId, String token) {
        Member member = memberRepository.findByTelegramId(telegramId).get();
        Credential credential = credentialRepository.findByMember(member).get();

        credential.setJwt(token);
        credentialRepository.save(credential);
    }
    
}