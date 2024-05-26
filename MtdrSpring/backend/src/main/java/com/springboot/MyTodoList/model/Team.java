package com.springboot.MyTodoList.model;

import javax.persistence.*;
import java.util.List;


@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "num_members")
    private int numMembers;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Member> members;

    public Team() {

    }

    public Team(int id, String name, int num_members) {
        this.id = id;
        this.name = name;
        this.numMembers = num_members;
    }

    // Getter and setter for team_id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and setter for num_members
    public int getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(int numMembers) {
        this.numMembers = numMembers;
    }

    @Override
    public String toString() {
        return "Team{" + "id=" + id + ", name=" + name + ", num_members=" + numMembers + '}';
    }
    
    
}
