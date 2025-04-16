package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.Evaluation;
import com.example.whucs_mentorguide.entity.Paper;
import com.example.whucs_mentorguide.entity.Teacher;
import com.example.whucs_mentorguide.mapper.PaperMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/paper")
public class PaperController {
    PaperMapper paperMapper;

    @GetMapping("/all")     //得到所有论文
    public Result<?> findAllTeachers() {
        List<Paper> papers = paperMapper.selectList(Wrappers.emptyWrapper());
        return Result.success(papers);
    }

    @RequestMapping("/insert")  //插入论文
    public Result<?> insert(Paper paper){
        paperMapper.insert(paper);
        return Result.success();
    }

    @GetMapping("/teacher/{teacherName}")    //根据老师名字查找论文
    public Result<?> findByTeacherName(@PathVariable String teacherName) {
        LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
        wrapper.eq(Paper::getTeacher,teacherName);
        List<Paper> papers = paperMapper.selectList(wrapper);
        return Result.success(papers);
    }

    @GetMapping("/title/{title}")       //根据论文标题查找论文
    public Result<?> findByTitle(@PathVariable String title) {
        LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
        wrapper.eq(Paper::getTitle,title);
        List<Paper> papers = paperMapper.selectList(wrapper);
        return Result.success(papers);
    }

    @GetMapping("/author/{author}")         //根据论文作者查找论文
    public Result<?> findByAuthor(@PathVariable String author) {
        LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
        wrapper.eq(Paper::getAuthor,author);
        List<Paper> papers = paperMapper.selectList(wrapper);
        return Result.success(papers);
    }

    @GetMapping("/year/{year}")         // 根据论文年份查找论文
    public Result<?> findByYear(@PathVariable String year) {
        LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
        wrapper.eq(Paper::getYear,year);
        List<Paper> papers = paperMapper.selectList(wrapper);
        return Result.success(papers);
    }

    @GetMapping("/id/{id}") // 根据论文id查找论文
    public Result<?> findById(@PathVariable Integer id) {
        LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
        wrapper.eq(Paper::getId,id);
        Paper paper = paperMapper.selectOne(wrapper);
        return Result.success(paper);
    }

    //根据论文id得到url
    @GetMapping("/url/{id}")
    public Result<?> getUrl(@PathVariable Integer id) {
        LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
        wrapper.eq(Paper::getId,id);
        Paper paper = paperMapper.selectOne(wrapper);
        return Result.success(paper.getUrl());
    }
}