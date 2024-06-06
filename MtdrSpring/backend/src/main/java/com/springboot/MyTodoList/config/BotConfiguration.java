package com.springboot.MyTodoList.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.springboot.MyTodoList.controller.ToDoItemBotController;
import com.springboot.MyTodoList.service.MemberService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.TaskSessionService;

@Configuration
public class BotConfiguration {

    @Bean
    public ToDoItemBotController toDoItemBotController(TaskService taskService, MemberService memberService, TaskSessionService taskSessionService) {
        // Use appropriate values for botToken and botName
        String botToken = "YOUR_BOT_TOKEN";
        String botName = "YOUR_BOT_NAME";
        return new ToDoItemBotController(botToken, botName, taskService, memberService, taskSessionService);
    }
}
