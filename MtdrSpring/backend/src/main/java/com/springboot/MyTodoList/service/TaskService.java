package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.repository.MemberRepository;
import com.springboot.MyTodoList.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MemberRepository memberRepository;

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

    public void addTask(TaskDto newTaskDto) {
        Task task = toEntity(newTaskDto);
        taskRepository.save(task);
    }

    public boolean deleteTask(int id) {
        try {
            taskRepository.deleteById(id);
            return true;
        } catch (Exception e) {
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
