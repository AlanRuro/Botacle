package com.springboot.MyTodoList.controller;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.List;

@RestController
public class ToDoItemController {
//    @Autowired
//    private TaskService toDoItemService;
//    //@CrossOrigin
//    @GetMapping(value = "/todolist")
//    public List<Task> getAllTasks(){
//        // return taskService.findAll();
//
//        return null;
//    }
//    //@CrossOrigin
//    @GetMapping(value = "/todolist/{id}")
//    public ResponseEntity<Task> getToDoItemById(@PathVariable int id){
//        try{
//            ResponseEntity<Task> responseEntity = toDoItemService.getTaskById(id);
//            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
//        }catch (Exception e){
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//    //@CrossOrigin
//    @PostMapping(value = "/todolist")
//    public ResponseEntity addToDoItem(@RequestBody TaskDto task) throws Exception{
//        Task td = toDoItemService.addTask(task);
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.set("location",""+td.getTaskId());
//        responseHeaders.set("Access-Control-Expose-Headers","location");
//        //URI location = URI.create(""+td.getID())
//
//        return ResponseEntity.ok()
//                .headers(responseHeaders).build();
//    }
//    //@CrossOrigin
//    @PutMapping(value = "todolist/{id}")
//    public ResponseEntity updateToDoItem(@RequestBody TaskDto task, @PathVariable int id){
//        try{
//            Task toDoItem1 = toDoItemService.updateTask(id, task);
//            System.out.println(toDoItem1.toString());
//            return new ResponseEntity<>(toDoItem1,HttpStatus.OK);
//        }catch (Exception e){
//            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//        }
//    }
//    //@CrossOrigin
//    @DeleteMapping(value = "todolist/{id}")
//    public ResponseEntity<Boolean> deleteTask(@PathVariable("id") int id){
//        Boolean flag = false;
//        try{
//            flag = toDoItemService.deleteTask(id);
//            return new ResponseEntity<>(flag, HttpStatus.OK);
//        }catch (Exception e){
//            return new ResponseEntity<>(flag,HttpStatus.NOT_FOUND);
//        }
//    }



}
