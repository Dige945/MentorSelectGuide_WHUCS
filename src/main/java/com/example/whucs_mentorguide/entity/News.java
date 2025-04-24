package com.example.whucs_mentorguide.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@TableName("news")
// Declare a string variable to store the teacher's name
@Data
public class News {
    @TableId(type = IdType.AUTO)
    private int id;

    private String content;

    private String label; // 存储 JSON 格式的标签数组字符串

    private Timestamp createdAt; // 创建时间

    // 用于转换标签列表和 JSON 字符串
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void setLabelList(List<String> labels) {
        try {
            this.label = objectMapper.writeValueAsString(labels);
        } catch (JsonProcessingException e) {
            this.label = "[]";
        }
    }

    public List<String> getLabelList() {
        try {
            return objectMapper.readValue(this.label, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
