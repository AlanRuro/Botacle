package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.TaskSession;
import com.springboot.MyTodoList.repository.MemberRepository;
import com.springboot.MyTodoList.repository.TaskSessionRepository;
import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskSessionService {

    @Autowired
    private TaskSessionRepository taskSessionRepository;
    
    @Autowired
    private MemberRepository memberRepository;

    public TaskSessionService() {

    }

    public void updateTask(Long chatId, TaskDto taskDto) {
        Optional<TaskSession> taskSessionOpt = taskSessionRepository.getByChatIdNotFilled(chatId);
        if (taskSessionOpt.isPresent()) {
            TaskSession taskSession = taskSessionOpt.get();
            taskSession.setName(taskDto.getName());
            taskSession.setDescription(taskDto.getDescription());
            taskSession.setStartDate(taskDto.getStartDate());
            taskSession.setEndDate(taskDto.getEndDate());
            taskSessionRepository.save(taskSession);
        }
    }

    public TaskDto createEmptyTask(Long chatId, MemberDto memberDto, boolean isEdit) {
        TaskSession newTask = new TaskSession();
        TaskDto newTaskDto = new TaskDto();
        newTask.setChatId(chatId);
        Optional<Member> memberOpt = memberRepository.findByTelegramId(memberDto.getTelegramId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            newTask.setMember(member);
            taskSessionRepository.save(newTask);
            newTaskDto.setIsEdit(isEdit);
            newTaskDto.setMemberId(member.getId());
        }
        return newTaskDto;
    }

    public TaskDto getTaskSession(Long chatId) {
        TaskDto taskDto = new TaskDto();
        Optional<TaskSession> taskSessionOpt = taskSessionRepository.getByChatIdNotFilled(chatId);
        if (taskSessionOpt.isPresent()) {
            TaskSession taskSession = taskSessionOpt.get();
            taskDto.setName(taskSession.getName());
            taskDto.setDescription(taskSession.getDescription());
            taskDto.setStartDate(taskSession.getStartDate());
            taskDto.setEndDate(taskSession.getEndDate());
            taskDto.setMemberId(taskSession.getMember().getId());
            return taskDto;
        }
        return null;

    }

    public void confirmTaskSession(Long chatId) {
        Optional<TaskSession> taskSessionOpt = taskSessionRepository.getByChatIdNotFilled(chatId);
        if (taskSessionOpt.isPresent()) {
            TaskSession taskSession = taskSessionOpt.get();
            taskSession.setIsFilled(true);
            taskSessionRepository.save(taskSession);
        }
    }

    public void deleteTaskSession(Long chatId) {
        Optional<TaskSession> taskSessionOpt = taskSessionRepository.getByChatIdNotFilled(chatId);
        if (taskSessionOpt.isPresent()) {
            TaskSession taskSession = taskSessionOpt.get();
            Integer id = taskSession.getId();
            taskSessionRepository.deleteById(id);
        }
    }
    
    
}
