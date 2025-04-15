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

    @GetMapping("/person")
    public Result<?> findByName(@PathVariable Integer name){
//        System.out.println(id);
        Teacher teacher = teacherMapper.selectById(name);
//        System.out.println(user);
        return Result.success(teacher);
    }

    @GetMapping("/all")
    public Result<?> findAllTeachers() {
        List<Teacher> teachers = teacherMapper.selectList(Wrappers.emptyWrapper());
        return Result.success(teachers);
    }

    @GetMapping("/profileUrl")
    public Result<?> getProfileUrl(@RequestParam int id) {
        Teacher teacher = teacherMapper.selectById(id);
        return Result.success(teacher.getProfileUrl());
    }

    @GetMapping("/avatar")
    public Result<?> getAvatar(@RequestParam int id) {
        Teacher teacher = teacherMapper.selectById(id);
        return Result.success(teacher.getAvatar());
    }
}
