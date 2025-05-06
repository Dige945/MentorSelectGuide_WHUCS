package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.Evaluation;
import com.example.whucs_mentorguide.entity.Teacher;
import com.example.whucs_mentorguide.mapper.EvaluationMapper;
import com.example.whucs_mentorguide.mapper.TeacherMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/evaluations")
public class EvaluationController {
    @Resource
    EvaluationMapper evaluationMapper;
    
    @Resource
    TeacherMapper teacherMapper;
    
    // 内存缓存，记录 "IP-teacherId-日期" -> 次数
    // 实际生产环境建议使用Redis更合适，支持过期和分布式
    private static final Map<String, Integer> evaluationLimitCache = new ConcurrentHashMap<>();
    
    // 每天清理一次缓存的过期日期记录（简单实现，生产环境建议用定时任务）
    private static LocalDate lastCleanupDate = LocalDate.now();
    
    // 获取客户端IP
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，第一个IP为真实IP
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
    
    // 清理过期缓存记录（不同日期的）
    private void cleanupExpiredRecords() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastCleanupDate)) {
            evaluationLimitCache.clear();
            lastCleanupDate = today;
            System.out.println("已清理过期评价限制记录");
        }
    }

    @PostMapping("/insert")  //插入评价
    public Result<?> insert(@RequestBody Evaluation evaluation, HttpServletRequest request) {
        if (evaluation.getTeacherId() == null) {
            return Result.error("-1", "教师ID不能为空");
        }
        if (evaluation.getContent() == null || evaluation.getContent().trim().isEmpty()) {
            return Result.error("-1", "评价内容不能为空");
        }
        if (evaluation.getTeacherName() == null || evaluation.getTeacherName().trim().isEmpty()) {
            return Result.error("-1", "教师姓名不能为空");
        }
        
        try {
            // 清理过期缓存
            cleanupExpiredRecords();
            
            // 获取客户端IP
            String clientIp = getClientIp(request);
            System.out.println("客户端IP: " + clientIp);
            
            // 生成限制键: IP-teacherId-日期
            String today = LocalDate.now().toString();
            String limitKey = clientIp + "-" + evaluation.getTeacherId() + "-" + today;
            
            // 检查是否已经评价过
            Integer count = evaluationLimitCache.getOrDefault(limitKey, 0);
            if (count >= 1) {
                return Result.error("-1", "您今天已经评价过该教师，请明天再试");
            }
            
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
            
            // 更新评价限制缓存
            evaluationLimitCache.put(limitKey, count + 1);
            System.out.println("已更新评价限制: " + limitKey + " -> " + (count + 1));
            
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

    @GetMapping("/recent")      //查询最近5条评价
    public Result<?> findRecentEvaluations(){
        List<Evaluation> evaluations = evaluationMapper.selectList(
            Wrappers.<Evaluation>lambdaQuery()
                .orderByDesc(Evaluation::getCreatedAt)
                .last("LIMIT 5")
        );
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
    
    // 检查用户今天是否可以评价特定老师（给前端调用的接口）
    @GetMapping("/can-evaluate/{teacherId}")
    public Result<?> canEvaluate(@PathVariable Integer teacherId, HttpServletRequest request) {
        // 清理过期缓存
        cleanupExpiredRecords();
        
        String clientIp = getClientIp(request);
        String today = LocalDate.now().toString();
        String limitKey = clientIp + "-" + teacherId + "-" + today;
        
        Integer count = evaluationLimitCache.getOrDefault(limitKey, 0);
        boolean canEvaluate = count < 1;
        
        return Result.success(canEvaluate);
    }
}
