package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import java.util.Date;

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
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;
    
    @Column(name = "is_filled")
    private boolean isFilled;
    
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "is_edit")
    private boolean isEdit;

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
    }

    // Constructor con todos los campos
    public TaskSession(int id, String name, String description, boolean isDone, Date startDate, Date endDate, Long chatId, boolean isEdit ,Member member) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isDone = isDone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.chatId = chatId;
        this.isEdit = isEdit;
        this.member = member;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
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

    @Override
    public String toString() {
        return "TaskSession{" + "id=" + id + ", name=" + name + ", description=" + description + ", isDone=" + isDone + ", startDate=" + startDate + ", endDate=" + endDate + ", isFilled=" + isFilled + ", chatId=" + chatId + ", isEdit=" + isEdit +'}';
    } 
    
    
}
