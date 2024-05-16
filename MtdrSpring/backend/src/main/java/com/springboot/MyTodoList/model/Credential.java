/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.springboot.MyTodoList.model;

import javax.persistence.*;


@Entity
@Table(name = "credentials")
public class Credential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credential_id")
    private int credential_id;

    @Column(name = "username")
    private String username;
    
    @Column(name = "password")
    private String password;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    

    public int getCredential_id() {
        return credential_id;
    }

    public void setCredential_id(int credential_id) {
        this.credential_id = credential_id;
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
        return "Credential{" + "credential_id=" + credential_id + ", username=" + username + ", password=" + password + '}';
    }
    
    
    
}

