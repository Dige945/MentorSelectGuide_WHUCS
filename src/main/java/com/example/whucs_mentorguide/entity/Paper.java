package com.example.whucs_mentorguide.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("paper")
    // Declare a string variable to store the teacher's name
@Data
public class Paper {
    private int id;

    private String teacher;

    private String title;

    private String author;

    private String year;

    private String url;
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
