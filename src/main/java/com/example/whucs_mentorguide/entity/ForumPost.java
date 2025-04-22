package com.example.whucs_mentorguide.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("forum_posts")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Integer postId;
    private String title;
    private String content;
    private Integer categoryId;
    private Integer likeCount;
    private Integer replyCount;  // 新增字段
    private Integer parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


