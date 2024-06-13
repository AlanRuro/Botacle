package com.springboot.MyTodoList.facade;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.service.MemberService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.TaskSessionService;
import com.springboot.MyTodoList.util.PatternChecker;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskManagementFacade {

    private final TaskService taskService;
    private final TaskSessionService taskSessionService;
    private final MemberService memberService;

    @Autowired
    public TaskManagementFacade(TaskService taskService, TaskSessionService taskSessionService, MemberService memberService) {
        this.taskService = taskService;
        this.taskSessionService = taskSessionService;
        this.memberService = memberService;
    }

    public MemberDto getMember(long telegramId) {
        return memberService.getMemberByTelegramId(telegramId);
    }
    
    public TaskDto getTaskById(int id) {
        return taskService.getTaskById(id);
    }

    public boolean confirmNewTask(long chatId) {
        try {
            TaskDto taskDto = taskSessionService.getTaskSession(chatId);
            if (taskDto != null) {
                TaskDto newTaskDto = taskService.addTask(taskDto);
                taskDto.setTaskId(newTaskDto.getTaskId());
                taskSessionService.updateTask(chatId, taskDto);
                taskSessionService.confirmTaskSession(chatId);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void confirmEditTaskSession(long chatId, TaskDto editedtask) {
        taskSessionService.confirmTaskSession(chatId);
        taskService.updateTask(editedtask);
    }

    public boolean cancelNewTask(long id) {
        try {
            taskSessionService.deleteTaskSession(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public TaskDto getActiveTaskSession(long chatId) {
        return taskSessionService.getTaskSession(chatId);
    }
    
    public void updateTaskSession(long chatId, TaskDto newTaskSession) {
        taskSessionService.updateTask(chatId, newTaskSession);
    }
    
    public void updateTask(TaskDto task) {
        taskService.updateTask(task);
    }

    public List<TaskDto> getTasksByTelegramId(long telegramId) {
        return taskService.getTasksByTelegramId(telegramId);
    }
    
    public List<TaskDto> getTasksByMember(MemberDto member) {
        return taskService.getAllByMember(member);
    }
    
    public void prepareForNewTask(long chatId, MemberDto memberDto) {
       taskSessionService.createEmptyTask(chatId, memberDto, false);
    }

    public void prepareEditTaskField(long chatId, int taskId, String fieldToEdit) {
        TaskDto taskDto = taskService.getTaskById(taskId);
        MemberDto memberDto = memberService.getMemberById(taskDto.getMemberId());
        TaskDto newTaskSession = taskSessionService.createEmptyTask(chatId, memberDto, true);
        switch (fieldToEdit) {
            case "name":
                newTaskSession.setName(null);
                newTaskSession.setDescription(taskDto.getDescription());
                break;
            case "description":
                newTaskSession.setName(taskDto.getName());
                newTaskSession.setDescription(null);
                break;
            default:
                throw new IllegalArgumentException("Invalid field to edit: " + fieldToEdit);
        }
        newTaskSession.setStartDate(taskDto.getStartDate());
        newTaskSession.setEndDate(taskDto.getEndDate());
        newTaskSession.setTaskId(taskId);
        taskSessionService.updateTask(chatId, newTaskSession);
    }

    public void prepareEditNameOfTask(long chatId, int taskId) {
        prepareEditTaskField(chatId, taskId, "name");
    }

    public void prepareEditDescOfTask(long chatId, int taskId) {
        prepareEditTaskField(chatId, taskId, "description");
    }

    public void setTaskAsDone(int taskId) {
        TaskDto taskDto = taskService.getTaskById(taskId);
        if (taskDto == null) {
            return;
        }
        taskDto.setIsDone(true);
        taskService.updateTask(taskDto);
    }

    public boolean deleteTask(int taskId) {
        TaskDto task = taskService.getTaskById(taskId);
        if (task == null) {
            return false;
        }
        return taskService.deleteTask(taskId);
    }
    
    public List<MemberDto> getAllMembersOfTeam(int teamId) {
        return memberService.getEmployeesByTeamId(teamId);
    }
    
    public List<MemberDto> getAllMembersOfManager(int managerId) {
        MemberDto member = memberService.getMemberById(managerId);
        return memberService.getMembersOfManager(member);
    }

    public boolean isTaskNameValid(String name) {
        return PatternChecker.hasValidNumOfCharacters(name, 45);
    }
    
    public boolean isTaskDescriptionValid(String description) {
        return PatternChecker.hasValidNumOfCharacters(description, 65);
    }
    
    public boolean taskSessionExist(long chatId) {
        TaskDto taskSessionDto = taskSessionService.getTaskSession(chatId);
        return taskSessionDto != null;
    }
    
    public boolean isDateValid(String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public boolean isInputValid(String text) {
        return !(PatternChecker.hasSpecialCharacters(text) || !PatternChecker.isUTF8(text));
    }
}
