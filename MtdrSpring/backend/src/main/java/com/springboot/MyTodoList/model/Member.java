package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import java.util.List;


@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private int memberId;

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;
    
    @Column(name = "is_manager")
    private boolean isManager;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "telegram_id")
    private long telegramId;
    
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Credential credential;
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Task> tasks;
    
    public Member() {

    }

    public Member(int memberId, String name, String lastName, String email, Team team, long telegramId) {
        this.memberId = memberId;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.team = team;
        this.telegramId = telegramId;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }
    
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "Member{" + "memberId=" + memberId + ", name=" + name + ", lastName=" + lastName + ", email=" + email + ", isManager=" + isManager + ", team=" + team + ", telegramId=" + telegramId + '}';
    }
    
    
}
