package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.Evaluation;
import com.example.whucs_mentorguide.mapper.EvaluationMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/evaluations")
public class EvaluationController {
    EvaluationMapper evaluationMapper;
    @GetMapping("/insert")  //插入评价
    public Result<?> insert(Evaluation evaluation){
        evaluationMapper.insert(evaluation);
        return Result.success();
    }

    @GetMapping("/item")        //根据id查询评价
    public Result<?> findByID(@PathVariable Integer id){
        Evaluation item = evaluationMapper.selectById(id);
        return Result.success(item);
    }

    @GetMapping("/all")         //查询所有评价
    public Result<?> findAllEvaluations(){
        List<Evaluation> evaluations = evaluationMapper.selectList(null);
        return Result.success(evaluations);
    }

    @GetMapping("/teacher")         //根据teacherId查询评价
    public Result<?> findByTeacherId(@PathVariable Integer teacherId){
        LambdaQueryWrapper<Evaluation> wrapper = Wrappers.<Evaluation>lambdaQuery();
        wrapper.eq(Evaluation::getTeacherId,teacherId);
        List<Evaluation> evaluations = evaluationMapper.selectList(wrapper);
        return Result.success(evaluations);
    }

    //根据id和userId删除评价
    @GetMapping("/delete")
    public Result<?> delete(@PathVariable Integer id,@PathVariable Integer userId){
        LambdaQueryWrapper<Evaluation> wrapper = Wrappers.<Evaluation>lambdaQuery();
        wrapper.eq(Evaluation::getId,id).eq(Evaluation::getUserId,userId);
        evaluationMapper.delete(wrapper);
        return Result.success();
    }
}
