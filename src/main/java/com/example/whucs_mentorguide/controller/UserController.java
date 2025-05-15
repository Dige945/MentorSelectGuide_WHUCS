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
    public Result<?> save(@RequestBody User user) {
        // System.out.println(user);
        if (user.getPassword() == null) {
            user.setPassword("123456");
        }
        userMapper.insert(user);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody User user) {
        if (user.getAvatar() == null) {
            user.setAvatar("../photos/default.jpg");
        }
        userMapper.updateById(user);
        System.out.println(user);
        return Result.success();
    }

    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String search) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(User::getNickname, search);
        }
        Page<User> userPage = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(userPage);
    }

    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Integer id) {
        // System.out.println(id);
        User user = userMapper.selectById(id);
        // System.out.println(user);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername())
                .eq(User::getPassword, user.getPassword()));
        if (res == null) {
            return Result.error("-1", "用户名或密码错误");
        }
        return Result.success(res);
    }

    @ResponseBody
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, user.getUsername()));
        if (res != null) {
            return Result.error("-1", "用户名已存在");
        }
        if (user.getPassword() == null) {
            user.setPassword("123456");
        }
        userMapper.insert(user);
        return Result.success();
    }

    @ResponseBody
    @PostMapping("/update")
    public Result<?> updateProfile(@RequestBody User user) {
        // 检查用户是否存在
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            return Result.error("-1", "用户不存在");
        }

        // 更新所有允许修改的字段
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getAge() != null) {
            existingUser.setAge(user.getAge());
        }
        if (user.getSex() != null) {
            existingUser.setSex(user.getSex());
        }
        if (user.getAddress() != null) {
            existingUser.setAddress(user.getAddress());
        }
        if (user.getBirthday() != null) {
            existingUser.setBirthday(user.getBirthday());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }

        // 更新用户信息
        int result = userMapper.updateById(existingUser);
        if (result > 0) {
            return Result.success(existingUser);
        } else {
            return Result.error("-1", "更新失败");
        }
    }

    @ResponseBody
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestHeader("Authorization") String token) {
        // 从 token 中获取用户名
        String username = token; // 这里需要根据实际的 token 解析逻辑来获取用户名
        if (username == null || username.isEmpty()) {
            return Result.error("-1", "未登录或登录已过期");
        }

        // 根据用户名查询用户信息
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }

        // 不返回密码信息
        user.setPassword(null);
        return Result.success(user);
    }

    @ResponseBody
    @PostMapping("/updatePersonImage")
    public Result<?> updatePersonImage(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("-1", "用户ID不能为空");
        }
        
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            return Result.error("-1", "用户不存在");
        }

        // 更新个人画像
        existingUser.setPersonImage(user.getPersonImage());
        
        int result = userMapper.updateById(existingUser);
        if (result > 0) {
            return Result.success();
        } else {
            return Result.error("-1", "更新失败");
        }
    }
}
