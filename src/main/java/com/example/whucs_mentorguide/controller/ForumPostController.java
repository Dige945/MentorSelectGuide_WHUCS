package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.ForumPost;
import com.example.whucs_mentorguide.mapper.ForumPostMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/forum/posts")
public class ForumPostController {

    @Resource
    private ForumPostMapper forumPostMapper;

    @GetMapping
    public Result<?> getPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer categoryId) {
        try {
            LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
            if (categoryId != null) {
                wrapper.eq(ForumPost::getCategoryId, categoryId);
            }
            wrapper.isNull(ForumPost::getParentId);
            wrapper.orderByDesc(ForumPost::getCreatedAt);
            
            Page<ForumPost> page = new Page<>(pageNum, pageSize);
            Page<ForumPost> postPage = forumPostMapper.selectPage(page, wrapper);
            
            // 获取每个帖子的回复数
            for (ForumPost post : postPage.getRecords()) {
                LambdaQueryWrapper<ForumPost> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.eq(ForumPost::getParentId, post.getPostId());
                Long replyCount = forumPostMapper.selectCount(replyWrapper);
                post.setReplyCount(replyCount.intValue());
            }
            
            return Result.success(postPage);
        } catch (Exception e) {
            System.err.println("获取帖子列表时发生错误：");
            e.printStackTrace();
            return Result.error("-1", "获取帖子列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<?> createPost(@RequestBody ForumPost post) {
        try {
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            post.setLikeCount(0);
            post.setReplyCount(0);  // 初始化回复数
            
            forumPostMapper.insert(post);
            
            // 如果是回复，增加父帖子的回复数
            if (post.getParentId() != null) {
                forumPostMapper.incrementReplyCount(post.getParentId());
            }
            
            return Result.success();
        } catch (Exception e) {
            System.err.println("创建帖子时发生错误：");
            e.printStackTrace();
            return Result.error("-1", "创建帖子失败：" + e.getMessage());
        }
    }

    @GetMapping("/replies/{postId}")
    public Result<?> getReplies(@PathVariable Integer postId) {
        try {
            List<ForumPost> replies = forumPostMapper.findRepliesByParentId(postId);
            return Result.success(replies);
        } catch (Exception e) {
            System.err.println("获取回复列表时发生错误：" + e.getMessage());
            e.printStackTrace();
            return Result.error("-1", "获取回复列表失败");
        }
    }

    @PostMapping("/{postId}/like")
    public Result<?> likePost(@PathVariable Integer postId) {
        try {
            ForumPost post = forumPostMapper.selectById(postId);
            if (post == null) {
                return Result.error("-1", "帖子不存在");
            }
            
            forumPostMapper.incrementLikeCount(postId);
            return Result.success();
        } catch (Exception e) {
            System.err.println("点赞帖子时发生错误：" + e.getMessage());
            e.printStackTrace();
            return Result.error("-1", "点赞失败");
        }
    }

    @DeleteMapping("/{postId}")
    public Result<?> deletePost(@PathVariable Integer postId) {
        try {
            ForumPost post = forumPostMapper.selectById(postId);
            if (post == null) {
                return Result.error("-1", "帖子不存在");
            }
            
            // 如果是回复，减少父帖子的回复数
            if (post.getParentId() != null) {
                forumPostMapper.decrementReplyCount(post.getParentId());
            }
            
            forumPostMapper.deleteById(postId);
            return Result.success();
        } catch (Exception e) {
            System.err.println("删除帖子时发生错误：" + e.getMessage());
            e.printStackTrace();
            return Result.error("-1", "删除失败");
        }
    }

    @GetMapping("/test")
    public Result<?> testGetPosts() {
        try {
            List<ForumPost> posts = forumPostMapper.selectList(null);
            System.out.println("测试查询结果：" + posts);
            return Result.success(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("-1", "测试查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{postId}/reply-count")
    public Result<?> getReplyCount(@PathVariable Integer postId) {
        try {
            LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ForumPost::getParentId, postId);
            Long replyCount = forumPostMapper.selectCount(wrapper);
            return Result.success(replyCount);
        } catch (Exception e) {
            System.err.println("获取回复数时发生错误：" + e.getMessage());
            e.printStackTrace();
            return Result.error("-1", "获取回复数失败");
        }
    }
}








