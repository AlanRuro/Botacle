package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.util.BotState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import java.util.HashMap;
import org.springframework.stereotype.Service;

@Service
public class ChatSession {
    private final Map<Long, BotState> chatStates;
    private final Map<Long, TaskDto> chatTasks;
    private final Map<Long, MemberDto> chatCredentials;

    public ChatSession() {
        this.chatStates = new HashMap<>();
        this.chatTasks = new HashMap<>();
        this.chatCredentials = new HashMap<>();
    } 

    public void setChatState(Long chatId, BotState state) {
        chatStates.put(chatId, state);
    }

    public BotState getChatState(Long chatId) {
        return chatStates.get(chatId);
    }

    public void setUsername(Long chatId, String username) {
        MemberDto credentials = this.chatCredentials.get(chatId);
        credentials.setUsername(username);
        chatCredentials.put(chatId, credentials);
    }

    public String getUsername(Long chatId) {
        MemberDto credentials = this.chatCredentials.get(chatId);
        return credentials.getUsername();
    }
    
    public void setPassword(Long chatId, String password) {
        MemberDto credentials = this.chatCredentials.get(chatId);
        credentials.setPassword(password);
        chatCredentials.put(chatId, credentials);
    }
    
    public String getPassword(Long chatId) {
        MemberDto credentials = this.chatCredentials.get(chatId);
        return credentials.getPassword();
    }
    
    public void createEmptyTask(Long chatId, Member member) {
        TaskDto newTask = new TaskDto();
        newTask.setMember(member);
        chatTasks.put(chatId, newTask);
    }
    
    public void createEmptyCredentials(Long chatId, Long telegramId) {
        MemberDto emptyCredentials = new MemberDto();
        emptyCredentials.setTelegramId(telegramId);
        chatCredentials.put(chatId, emptyCredentials);
    }
    
    public TaskDto getTaskInCreation(Long chatId) {
        return chatTasks.get(chatId);
    }
    
    public void setTaskName(Long chatId, String name) {
        TaskDto task = chatTasks.get(chatId);
        task.setName(name);
        chatTasks.put(chatId, task);
        setChatState(chatId, BotState.AWAITING_TASK_DESCRIPTION);
    }
    
    public void setTaskDescription(Long chatId, String description) {
        TaskDto task = chatTasks.get(chatId);
        task.setDescription(description);
        chatTasks.put(chatId, task);
        setChatState(chatId, BotState.AWAITING_TASK_START_DATE);
    }
    
    public void removeState(Long chatId) {
        chatStates.remove(chatId);
    }
    
    public void removeTask(Long chatId) {
        chatTasks.remove(chatId);
    }
    
    public void removeCredentials(Long chatId) {
        chatCredentials.remove(chatId);
    }
    
    public void removeAllFromChat(Long chatId) {
        removeTask(chatId);
        removeCredentials(chatId);
        setChatState(chatId, BotState.AUTHENTICATED);
    }
    
}
