package com.example.whucs_mentorguide.controller;

import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.News;
import com.example.whucs_mentorguide.mapper.NewsMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.sql.Timestamp;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/news")
public class NewsController {
    @Resource
    NewsMapper newsMapper;

    @PostMapping("/insert")
    public Result<?> insert(@RequestBody News news) {
        try {
            if (news == null) {
                return Result.error("-1", "新闻数据不能为空");
            }
            // 设置创建时间
            news.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            // 确保标签是有效的 JSON 数组
            if (news.getLabel() == null || news.getLabel().isEmpty()) {
                news.setLabel("[]");
            } else {
                try {
                    // 验证标签是否为有效的 JSON 数组
                    new ObjectMapper().readTree(news.getLabel());
                } catch (Exception e) {
                    return Result.error("-1", "标签格式无效，必须是有效的 JSON 数组");
                }
            }

            newsMapper.insert(news);
            return Result.success();
        } catch (Exception e) {
            return Result.error("-1", "插入新闻失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/deleteNews/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        try {
            if (id == null) {
                return Result.error("-1", "新闻ID不能为空");
            }
            newsMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("-1", "删除新闻失败：" + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<?> update(@RequestBody News news) {
        try {
            if (news == null) {
                return Result.error("-1", "新闻数据不能为空");
            }

            if (news.getId() <= 0) {
                return Result.error("-1", "新闻ID无效");
            }

            // 检查新闻是否存在
            News existingNews = newsMapper.selectById(news.getId());
            if (existingNews == null) {
                return Result.error("-1", "要更新的新闻不存在");
            }

            // 确保标签是有效的 JSON 数组
            if (news.getLabel() == null || news.getLabel().isEmpty()) {
                news.setLabel("[]");
            } else {
                try {
                    // 验证标签是否为有效的 JSON 数组
                    new ObjectMapper().readTree(news.getLabel());
                } catch (Exception e) {
                    return Result.error("-1", "标签格式无效，必须是有效的 JSON 数组");
                }
            }

            // 保留原有的创建时间
            news.setCreatedAt(existingNews.getCreatedAt());

            int result = newsMapper.updateById(news);
            if (result > 0) {
                return Result.success();
            } else {
                return Result.error("-1", "更新失败，可能是数据未发生变化");
            }
        } catch (Exception e) {
            return Result.error("-1", "更新新闻失败：" + e.getMessage());
        }
    }

    @GetMapping("/getNewsList")
    public Result<?> getNewsList() {
        try {
            // 按创建时间降序排序
            QueryWrapper<News> queryWrapper = new QueryWrapper<>();
            queryWrapper.orderByDesc("created_at");
            List<News> newsList = newsMapper.selectList(queryWrapper);
            return Result.success(newsList);
        } catch (Exception e) {
            return Result.error("-1", "获取新闻列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/getNewsByTag/{tag}")
    public Result<?> getNewsByTag(@PathVariable String tag) {
        try {
            QueryWrapper<News> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("label", tag)
                    .orderByDesc("created_at");
            List<News> newsList = newsMapper.selectList(queryWrapper);
            return Result.success(newsList);
        } catch (Exception e) {
            return Result.error("-1", "获取新闻列表失败：" + e.getMessage());
        }
    }
}