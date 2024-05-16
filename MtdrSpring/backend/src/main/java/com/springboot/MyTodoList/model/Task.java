package com.springboot.MyTodoList.model;


import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int taskId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "isDone")
    private boolean isDone;

    @Column(name = "startDate")
    private Date startDate;

    @Column(name = "endDate")
    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    // Constructor vacio
    public Task() {
    }

    // Constructor con todos los campos
    public Task(int taskId, String name, String description, boolean isDone, Date startDate, Date endDate, Member member) {
        this.taskId = taskId;
        this.name = name;
        this.description = description;
        this.isDone = isDone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.member = member;
    }

    // Getters y Setters
    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
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

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
    return "Tarea {" + 
           "\n\t-Nombre: " + name + 
           "\n\t-Descripción: " + description + 
           "\n\t-¿Tarea realizada? " + isDone + 
           "\n}";
}

   
}
