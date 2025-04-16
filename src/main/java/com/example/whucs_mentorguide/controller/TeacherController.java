package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.Teacher;
import com.example.whucs_mentorguide.mapper.TeacherMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {
    @Resource
    TeacherMapper teacherMapper;

    @GetMapping("/{id}")        //根据老师id查找老师
    public Result<?> findById(@PathVariable Integer id){
        Teacher teacher = teacherMapper.selectById(id);
        return Result.success(teacher);
    }

    //根据老师名字查找老师
    @GetMapping("/name/{name}")     //根据老师名字查找老师
    public Result<?> findByName(@PathVariable String name){
        Teacher teacher = teacherMapper.selectOne(Wrappers.<Teacher>lambdaQuery().eq(Teacher::getName, name));
        return Result.success(teacher);
    }

//    @GetMapping("/person")
//    public Result<?> findByName(@PathVariable Integer id){
//        Teacher teacher = teacherMapper.selectById(id);
//        return Result.success(teacher);
//    }

    @GetMapping("/all")
    public Result<?> findAllTeachers() {
        List<Teacher> teachers = teacherMapper.selectList(Wrappers.emptyWrapper());
        return Result.success(teachers);
    }

    @GetMapping("/hot")
    public Result<?> getHotTeachers() {
        List<Teacher> hotTeachers = teacherMapper.selectList(
            Wrappers.<Teacher>lambdaQuery()
                .orderByDesc(Teacher::getRecommendcount)
                .last("LIMIT 5")
        );
        return Result.success(hotTeachers);
    }

    @GetMapping("/search")
    public Result<?> searchTeachers(@RequestParam String keyword) {
        List<Teacher> teachers = teacherMapper.selectList(
            Wrappers.<Teacher>lambdaQuery()
                .like(Teacher::getName, keyword)
                .last("LIMIT 10")
        );
        return Result.success(teachers);
    }

    @GetMapping("/profileUrl/{id}")
    public Result<?> getProfileUrl(@RequestParam int id) {
        Teacher teacher = teacherMapper.selectById(id);
        return Result.success(teacher.getProfileUrl());
    }

    @GetMapping("/avatar/{id}")
    public Result<?> getAvatar(@RequestParam int id) {
        Teacher teacher = teacherMapper.selectById(id);
        return Result.success(teacher.getAvatar());
    }
}
