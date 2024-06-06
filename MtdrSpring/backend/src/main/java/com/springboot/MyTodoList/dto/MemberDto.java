package com.springboot.MyTodoList.dto;


public class MemberDto {

    private String name;

    private String lastName;

    private String email;
    
    private boolean isManager;

    private int teamId;

    private long telegramId;
    
    // private String username;
    
    // private String password;

    private int credentialsId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getIsManager() {
        return isManager;
    }

    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    // public String getUsername() {
    //     return username;
    // }

    // public void setUsername(String username) {
    //     this.username = username;
    // }

    // public String getPassword() {
    //     return password;
    // }

    // public void setPassword(String password) {
    //     this.password = password;
    // }

    public int getCredentialsId() {
        return credentialsId;
    }

    public void setCredentialsId(int credentialsId) {
        this.credentialsId = credentialsId;
    }

    @Override
    public String toString() {
        return "MemberDto{" + "name=" + name + ", lastName=" + lastName + ", email=" + email + ", isManager=" + isManager + ", teamId=" + teamId + ", telegramId=" + telegramId + ", credentialsId=" + credentialsId + '}';
    }
    
    
    
}
