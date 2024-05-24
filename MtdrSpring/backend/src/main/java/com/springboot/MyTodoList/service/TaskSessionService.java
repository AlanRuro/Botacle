package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.TaskSession;
import com.springboot.MyTodoList.repository.TaskRepository;
import com.springboot.MyTodoList.repository.TaskSessionRepository;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskSessionService {

    private static final Logger logger = LoggerFactory.getLogger(TaskSessionService.class); // Eliminar

    @Autowired
    private TaskSessionRepository taskSessionRepository;

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

    public TaskDto createEmptyTask(Long chatId, Member member) {
        TaskDto newTaskDto = new TaskDto();
        TaskSession newTask = new TaskSession();
        newTaskDto.setMember(member);
        newTask.setChatId(chatId);
        newTask.setMember(member);
        taskSessionRepository.save(newTask);
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
            taskDto.setMember(taskSession.getMember());
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
            logger.info("Session: " + taskSession.getId());
            Integer id = taskSession.getId();
            taskSessionRepository.deleteById(id);
        }
    }
    
    
}
