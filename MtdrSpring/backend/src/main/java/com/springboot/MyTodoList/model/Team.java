package com.springboot.MyTodoList.model;

import jakarta.persistence.*;
import java.util.List;



@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private int teamId;

    @Column(name = "name")
    private String name;

    @Column(name = "num_members")
    private int numMembers;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Member> members;

    public Team() {

    }

    public Team(int teamId, String name, int num_members) {
        this.teamId = teamId;
        this.name = name;
        this.numMembers = num_members;
    }

    // Getter and setter for team_id
    public long getTeam_id() {
        return teamId;
    }

    public void setTeam_id(int teamId) {
        this.teamId = teamId;
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
        return "Team{" + "team_id=" + teamId + ", name=" + name + ", num_members=" + numMembers + '}';
    }
    
    
}
