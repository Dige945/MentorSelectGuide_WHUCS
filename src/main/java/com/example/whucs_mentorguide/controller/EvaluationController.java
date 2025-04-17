package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.Evaluation;
import com.example.whucs_mentorguide.entity.Teacher;
import com.example.whucs_mentorguide.mapper.EvaluationMapper;
import com.example.whucs_mentorguide.mapper.TeacherMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluations")
public class EvaluationController {
    @Resource
    EvaluationMapper evaluationMapper;
    
    @Resource
    TeacherMapper teacherMapper;

    @PostMapping("/insert")  //插入评价
    public Result<?> insert(@RequestBody Evaluation evaluation) {
        if (evaluation.getTeacherId() == null) {
            return Result.error("-1", "教师ID不能为空");
        }
        if (evaluation.getContent() == null || evaluation.getContent().trim().isEmpty()) {
            return Result.error("-1", "评价内容不能为空");
        }
        
        try {
            // 设置创建时间为当前时间
            evaluation.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            System.out.println("准备保存评价数据：" + evaluation);
            evaluationMapper.insert(evaluation);
            System.out.println("评价保存成功");
            
            // 更新教师推荐数
            Teacher teacher = teacherMapper.selectById(evaluation.getTeacherId());
            if (teacher != null) {
                System.out.println("找到教师：" + teacher);
                teacher.setRecommendcount(teacher.getRecommendcount() != null ? teacher.getRecommendcount() + 1 : 1);
                teacherMapper.updateById(teacher);
                System.out.println("教师推荐数更新成功");
            } else {
                System.out.println("未找到教师：" + evaluation.getTeacherId());
            }
            
            return Result.success();
        } catch (Exception e) {
            System.err.println("保存评价失败，错误详情：");
            e.printStackTrace();
            return Result.error("-1", "保存评价失败：" + e.getMessage());
        }
    }

    @GetMapping("/item/{id}")        //根据id查询评价
    public Result<?> findByID(@PathVariable Integer id){
        Evaluation item = evaluationMapper.selectById(id);
        return Result.success(item);
    }

    @GetMapping("/all")         //查询所有评价
    public Result<?> findAllEvaluations(){
        List<Evaluation> evaluations = evaluationMapper.selectList(null);
        return Result.success(evaluations);
    }

    @GetMapping("/teacher/{teacherId}")         //根据teacherId查询评价
    public Result<?> findByTeacherId(@PathVariable Integer teacherId){
        LambdaQueryWrapper<Evaluation> wrapper = Wrappers.<Evaluation>lambdaQuery();
        wrapper.eq(Evaluation::getTeacherId, teacherId);
        List<Evaluation> evaluations = evaluationMapper.selectList(wrapper);
        return Result.success(evaluations);
    }

    //根据id和userId删除评价
    @GetMapping("/delete/{id}/{userId}")
    public Result<?> delete(@PathVariable Integer id, @PathVariable String userId){
        LambdaQueryWrapper<Evaluation> wrapper = Wrappers.<Evaluation>lambdaQuery();
        wrapper.eq(Evaluation::getId, id).eq(Evaluation::getUserId, userId);
        evaluationMapper.delete(wrapper);
        return Result.success();
    }
}
