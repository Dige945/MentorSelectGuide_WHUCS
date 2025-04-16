package com.example.whucs_mentorguide.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("evaluation")
@Data
public class Evaluation {
    @TableId(type = IdType.AUTO)       // 主键自增
    private int id;

    private int teacherId;

    private int userId;

    private int score;

    private String content;

    private java.sql.Timestamp createdAt;
}
