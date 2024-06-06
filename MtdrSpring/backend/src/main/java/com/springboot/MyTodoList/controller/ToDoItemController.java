package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.service.AuthService;
import com.springboot.MyTodoList.service.EmailService;
import com.springboot.MyTodoList.service.MemberService;
import com.springboot.MyTodoList.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ToDoItemController {

    @Autowired
    MemberService memberService;

    @Autowired
    AuthService authService;

    @Autowired
    EmailService emailService;

    @Autowired
    private ToDoItemBotController toDoItemBotController;

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemController.class);

    @GetMapping(value = "/update-token")
    public ResponseEntity<String> updateToken(
        @RequestParam(value = "telegramId", required = false) String telegramId,
        @RequestParam(value = "chatId", required = false) String chatId) {
        
        if (telegramId == null || telegramId.isEmpty()) {
            logger.warn("telegramId is required but not provided");
            return new ResponseEntity<>("telegramId is required", HttpStatus.BAD_REQUEST);
        }

        if (chatId == null || chatId.isEmpty()) {
            logger.warn("chatId is required but not provided");
            return new ResponseEntity<>("chatId is required", HttpStatus.BAD_REQUEST);
        }

        long telegramIdLong;
        long chatIdLong;
        try {
            telegramIdLong = Long.parseLong(telegramId);
            chatIdLong = Long.parseLong(chatId);
        } catch (NumberFormatException e) {
            logger.error("Invalid id format: telegramId={}, chatId={}", telegramId, chatId);
            return new ResponseEntity<>("Invalid id format", HttpStatus.BAD_REQUEST);
        }

        String token = JwtUtil.updateToken(telegramId);

        // Update the token in the database
        authService.updateJwtToken(telegramIdLong, token);

        // Send "Logged In" message to the user
        toDoItemBotController.sendLoggedInMessage(chatIdLong);

        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @GetMapping(value = "/send-email")
    public ResponseEntity<String> sendEmail(
        @RequestParam(value = "telegramId", required = false) String telegramId,
        @RequestParam(value = "chatId", required = false) String chatId) {
        
        if (telegramId == null || telegramId.isEmpty()) {
            logger.warn("telegramId is required but not provided");
            return new ResponseEntity<>("telegramId is required", HttpStatus.BAD_REQUEST);
        }
    
        if (chatId == null || chatId.isEmpty()) {
            logger.warn("chatId is required but not provided");
            return new ResponseEntity<>("chatId is required", HttpStatus.BAD_REQUEST);
        }
    
        long telegramIdLong = Long.parseLong(telegramId);
    
        // Send email with token link
        MemberDto member = memberService.getMemberByTelegramId(telegramIdLong);
        String email = member.getEmail();
    
        // Send email with token link
        String subject = "Login to Botacle";
        String emailText = "Hello,\n\nClick the link below to login into Botacle:\n" +
                           "http://localhost:8005/update-token?telegramId=" + telegramId + "&chatId=" + chatId;
    
        emailService.sendEmail(email, subject, emailText);
    
        return new ResponseEntity<>("Email sent", HttpStatus.OK);
    }
}
