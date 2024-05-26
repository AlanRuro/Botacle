package com.springboot.MyTodoList.dto;

import java.util.Date;


public class TaskDto {
    
    private Integer taskId;
    
    private Integer taskSessionId;
    
    private String name;

    private String description;

    private boolean isDone;

    private Date startDate;

    private Date endDate;
    
    private boolean isFilled;
    
    private Integer memberId;

    public TaskDto() {
        this.taskId = null;
        this.taskSessionId = null;
        this.name = "";
        this.description = "";
        this.isDone = false;
        this.isFilled = false;
        this.memberId = null;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getTaskSessionId() {
        return taskSessionId;
    }

    public void setTaskSessionId(Integer taskSessionId) {
        this.taskSessionId = taskSessionId;
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

    public boolean isIsFilled() {
        return isFilled;
    }

    public void setIsFilled(boolean isFilled) {
        this.isFilled = isFilled;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }
    
}
