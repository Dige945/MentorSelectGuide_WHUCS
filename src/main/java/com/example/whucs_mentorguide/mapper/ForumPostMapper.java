package com.example.whucs_mentorguide.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.whucs_mentorguide.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {
    
    @Select("SELECT * FROM forum_posts WHERE parent_id IS NULL ORDER BY created_at DESC LIMIT #{limit}")
    List<ForumPost> findLatestMainPosts(int limit);
    
    @Select("SELECT * FROM forum_posts WHERE category_id = #{categoryId} AND parent_id IS NULL ORDER BY created_at DESC")
    List<ForumPost> findPostsByCategory(Integer categoryId);
    
    @Update("UPDATE forum_posts SET reply_count = reply_count + 1 WHERE post_id = #{postId}")
    int incrementReplyCount(Integer postId);
    
    @Update("UPDATE forum_posts SET reply_count = reply_count - 1 WHERE post_id = #{postId}")
    int decrementReplyCount(Integer postId);
    
    @Update("UPDATE forum_posts SET like_count = like_count + 1 WHERE post_id = #{postId}")
    int incrementLikeCount(Integer postId);
    
    @Select("SELECT * FROM forum_posts WHERE parent_id = #{parentId} ORDER BY created_at ASC")
    List<ForumPost> findRepliesByParentId(Integer parentId);
}
