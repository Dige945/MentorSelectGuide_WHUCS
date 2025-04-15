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
}
