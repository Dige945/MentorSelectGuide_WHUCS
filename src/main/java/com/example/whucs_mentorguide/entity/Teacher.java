package com.example.whucs_mentorguide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("users")
@Data
public class Teacher {

    // id: 主键，int 类型，非空
    private int id;

    // name: varchar(50)，非空
    private String name;

    // title: varchar(50)，可为空
    private String title;

    // research_area: text，可为空
    private String researchArea;

    // profile_url: varchar(255)，可为空
    private String profileUrl;

    // department: varchar(50)，可为空
    private String department;

    // rank_level: varchar(20)，可为空
    private String rankLevel;

    // created_at: timestamp，非空
    private java.sql.Timestamp createdAt;

    // Getter and Setter for id
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter and Setter for name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter for researchArea
    public String getResearchArea() {
        return researchArea;
    }

    public void setResearchArea(String researchArea) {
        this.researchArea = researchArea;
    }

    // Getter and Setter for profileUrl
    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    // Getter and Setter for department
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // Getter and Setter for rankLevel
    public String getRankLevel() {
        return rankLevel;
    }

    public void setRankLevel(String rankLevel) {
        this.rankLevel = rankLevel;
    }

    // Getter and Setter for createdAt
    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}