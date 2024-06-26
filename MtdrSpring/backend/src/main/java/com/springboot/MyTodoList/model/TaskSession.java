package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "task_sessions")
public class TaskSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_session_id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_done")
    private boolean isDone;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "is_filled")
    private boolean isFilled;
    
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "is_edit")
    private boolean isEdit;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;
    
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // Constructor vacio
    public TaskSession() {
        this.name = null;
        this.description = null;
        this.startDate = null;
        this.endDate = null;
        this.isDone = false;
        this.isFilled = false;
        this.isEdit = false;
        this.chatId = null;
        this.task = null;
    }

    // Constructor con todos los campos
    public TaskSession(int id, String name, String description, boolean isDone, LocalDate startDate, LocalDate endDate, Long chatId, boolean isEdit ,Member member) {
        this.name = name;
        this.description = description;
        this.isDone = isDone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.chatId = chatId;
        this.isEdit = isEdit;
        this.member = member;
        this.task = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getIsDone() {
        return isDone;
    }

    public void setIsDone(boolean isDone) {
        this.isDone = isDone;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean getIsFilled() {
        return isFilled;
    }

    public void setIsFilled(boolean isFilled) {
        this.isFilled = isFilled;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public boolean getIsEdit() {
        return isEdit;
    }  
    
    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
    } 

    public Task getTask() {
        return task;
    }  
    
    public void setTask(Task task) {
        this.task = task;
    } 

    @Override
    public String toString() {
        return "TaskSession{" + "id=" + id + ", name=" + name + ", description=" + description + ", isDone=" + isDone + ", startDate=" + startDate + ", endDate=" + endDate + ", isFilled=" + isFilled + ", chatId=" + chatId + ", isEdit=" + isEdit + '}';
    } 
    
    
}
