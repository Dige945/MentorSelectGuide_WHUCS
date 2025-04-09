package com.example.whucs_mentorguide.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.User;
import com.example.whucs_mentorguide.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    UserMapper userMapper;
    @ResponseBody
    @PostMapping
    public Result<?> save(@RequestBody User user){
//        System.out.println(user);
        if(user.getPassword() == null){
            user.setPassword("123456");
        }
        userMapper.insert(user);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody User user){
        if(user.getAvatar() == null){
            user.setAvatar("../photos/default.jpg");
        }
        userMapper.updateById(user);
        System.out.println(user);
        return Result.success();
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search){
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        if(StrUtil.isNotBlank(search)){
            wrapper.like(User::getNickname,search);
        }
        Page<User> userPage = userMapper.selectPage(new Page<>(pageNum,pageSize), wrapper);
        return Result.success(userPage);
    }

    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Integer id){
//        System.out.println(id);
        User user = userMapper.selectById(id);
//        System.out.println(user);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id){
        userMapper.deleteById(id);
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()).eq(User::getPassword,user.getPassword()));
        if(res == null){
            return Result.error("-1","用户名或密码错误");
        }
        return Result.success(res);
    }

    @ResponseBody
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()));
        if(res != null){
            return Result.error("-1","用户名已存在");
        }
        if(user.getPassword() == null){
            user.setPassword("123456");
        }
        userMapper.insert(user);
        return Result.success();
    }


}
