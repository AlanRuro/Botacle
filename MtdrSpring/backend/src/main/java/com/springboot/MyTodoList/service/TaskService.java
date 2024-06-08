package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.controller.ToDoItemBotController;
import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.model.TaskSession;
import com.springboot.MyTodoList.repository.MemberRepository;
import com.springboot.MyTodoList.repository.TaskRepository;
import com.springboot.MyTodoList.repository.TaskSessionRepository;

import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TaskSessionRepository taskSessionRepository;

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);



    public List<TaskDto> getAllByMember(MemberDto memberDto) {
        Optional<Member> memberOpt = memberRepository.findByTelegramId(memberDto.getTelegramId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            List<Task> tasks = taskRepository.findAllByMember(member);
            List<TaskDto> tasksDto = new ArrayList<>();
            for (Task task : tasks) {
                TaskDto taskDto = toDto(task);
                tasksDto.add(taskDto);
            }
            return tasksDto;
        }
        return null;

    }

    public TaskDto getTaskById(int id) {
        Optional<Task> taskData = taskRepository.findById(id);
        if (taskData.isPresent()) {
            Task task = taskData.get();
            return toDto(task);
        } else {
            return null;
        }
    }

    public List<TaskDto> getTasksByTelegramId(long telegramId) {
        Optional<Member> memberOpt = memberRepository.findByTelegramId(telegramId);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            List<Task> tasks = taskRepository.findAllByMember(member);
            return tasks.stream().map(this::toDto).collect(Collectors.toList());
        }
        return null;
    }

    public TaskDto addTask(TaskDto newTaskDto) {
        Task task = toEntity(newTaskDto);
        Task newTask = taskRepository.save(task);
        TaskDto taskDto = toDto(newTask);
        return taskDto;
    }

    // public boolean deleteTask(int taskId) {
    //     try {
    //         // Ensure the task exists before attempting to delete it
    //         if (taskRepository.existsById(taskId)) {
    //             taskRepository.deleteById(taskId);
    //             logger.info("Task deleted from repository: " + taskId);
    //             // Verify if the task still exists
    //             boolean stillExists = taskRepository.existsById(taskId);
    //             logger.info("Task still exists after deletion: " + stillExists);
    //             return !stillExists;
    //         } else {
    //             logger.warn("Task with ID " + taskId + " does not exist.");
    //             return false;
    //         }
    //     } catch (Exception e) {
    //         logger.error("Error deleting task: " + e.getMessage(), e);
    //         return false;
    //     }
    // }

    @Transactional
    public boolean deleteTask(int id) {
        try {
            // Fetch the task by ID
            Optional<Task> taskOptional = taskRepository.findById(id);
            if (taskOptional.isPresent()) {
                Task task = taskOptional.get();
                
                // Delete all related task sessions
                taskSessionRepository.deleteAllByTask(task);
                taskRepository.deleteTaskById(id);
                
                return true;
            } else {
                logger.error("Task not found with id: " + id);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error deleting task: " + e.getMessage(), e);
            return false;
        }
    }

    

    public void updateTask(TaskDto taskDto) {
        Optional<Task> taskData = taskRepository.findById(taskDto.getTaskId());
        if (taskData.isPresent()) {
            Task task = toEntity(taskDto);
            task.setId(taskDto.getTaskId());
            taskRepository.save(task);
        }
    }

    private TaskDto toDto(Task task) {
        TaskDto taskDto = new TaskDto();
        taskDto.setTaskId(task.getId());
        taskDto.setName(task.getName());
        taskDto.setDescription(task.getDescription());
        taskDto.setStartDate(task.getStartDate());
        taskDto.setEndDate(task.getEndDate());
        taskDto.setMemberId(task.getMember().getId());
        taskDto.setIsDone(task.getIsDone());
        return taskDto;
    } 

    private Task toEntity(TaskDto taskDto) {
        Task task = new Task();
        task.setName(taskDto.getName());
        task.setDescription(taskDto.getDescription());
        task.setStartDate(taskDto.getStartDate());
        task.setEndDate(taskDto.getEndDate());
        task.setIsDone(taskDto.getIsDone());
        task.setMember(memberRepository.getById(taskDto.getMemberId()));
        return task;
    }
}
