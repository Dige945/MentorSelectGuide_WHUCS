package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.Teacher;
import com.example.whucs_mentorguide.mapper.TeacherMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
