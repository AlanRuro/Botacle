package com.springboot.MyTodoList.model;

import jakarta.persistence.*;


@Entity
@Table(name = "credentials")
public class Credential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credential_id")
    private int id;

    @Column(name = "username")
    private String username;
    
    @Column(name = "password")
    private String password;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Credential{" + "id=" + id + ", username=" + username + ", password=" + password + '}';
    }
    
    
    
}

