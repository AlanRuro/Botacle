package com.springboot.MyTodoList.service;


import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllByMember(Member member){
        List<Task> tasks = taskRepository.findAllByMember(member);
        return tasks;
    }

    public Task getTaskById(int id){
        Optional<Task> taskData = taskRepository.findById(id);
        if (taskData.isPresent()){
            return taskData.get();
        } else{
            return null;
        }
    }

    public Task addTask(TaskDto newTask){
        Task task = new Task();
        task.setName(newTask.getName());
        task.setDescription(newTask.getDescription());
        task.setIsDone(false);
        task.setMember(newTask.getMember());

        return taskRepository.save(task);
    }

    public boolean deleteTask(int id){
        try{
            taskRepository.deleteById(id);
            return true;
        } catch(Exception e){
            return false;
        }
    }
    
    public Task updateTask(int id, TaskDto td){
        Optional<Task> taskData = taskRepository.findById(id);
        if(taskData.isPresent()){
            Task task = taskData.get();
            task.setId(id);
            task.setName(td.getName());
            task.setDescription(td.getDescription());
            task.setStartDate(td.getStartDate());
            task.setEndDate(td.getEndDate());
            task.setMember(td.getMember());
            task.setIsDone(td.getIsDone());
            return taskRepository.save(task);
        }else{
            return null;
        }
    }
}
