package com.example.whucs_mentorguide.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.whucs_mentorguide.common.Result;
import com.example.whucs_mentorguide.entity.Paper;
import com.example.whucs_mentorguide.mapper.PaperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/paper")
public class PaperController {
    
    @Autowired
    private PaperMapper paperMapper;

    @GetMapping("/all")     //得到所有论文
    public Result<?> findAllPapers() {
        try {
            List<Paper> papers = paperMapper.selectList(Wrappers.emptyWrapper());
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error("-1", "查询所有论文失败：" + e.getMessage());
        }
    }

    @PostMapping("/insert")  //插入论文
    public Result<?> insert(@RequestBody Paper paper){
        try {
            if (paper == null) {
                return Result.error("-1", "论文数据不能为空");
            }
            paperMapper.insert(paper);
            return Result.success();
        } catch (Exception e) {
            return Result.error("-1", "插入论文失败：" + e.getMessage());
        }
    }


    @DeleteMapping("/delete/{id}")  //删除论文
    public Result<?> delete(@PathVariable Integer id) {
        try {
            paperMapper.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("-1", "删除论文失败：" + e.getMessage());
        }
    }

    @GetMapping("/page")  //分页查询
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            IPage<Paper> page = new Page<>(pageNum, pageSize);
            return Result.success(paperMapper.selectPage(page, Wrappers.emptyWrapper()));
        } catch (Exception e) {
            return Result.error("-1", "分页查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/teacher/{teacherName}")    //根据老师名字查找论文
    public Result<?> findByTeacherName(@PathVariable String teacherName) {
        try {
            LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
            wrapper.eq(Paper::getTeacher, teacherName);
            List<Paper> papers = paperMapper.selectList(wrapper);
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error("-1", "查询论文失败：" + e.getMessage());
        }
    }

    @GetMapping("/title/{title}")       //根据论文标题查找论文
    public Result<?> findByTitle(@PathVariable String title) {
        try {
            LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
            wrapper.eq(Paper::getTitle, title);
            List<Paper> papers = paperMapper.selectList(wrapper);
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error("-1", "查询论文失败：" + e.getMessage());
        }
    }

    @GetMapping("/author/{author}")         //根据论文作者查找论文
    public Result<?> findByAuthor(@PathVariable String author) {
        try {
            LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
            wrapper.eq(Paper::getAuthor, author);
            List<Paper> papers = paperMapper.selectList(wrapper);
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error("-1", "查询论文失败：" + e.getMessage());
        }
    }

    @GetMapping("/year/{year}")         // 根据论文年份查找论文
    public Result<?> findByYear(@PathVariable String year) {
        try {
            LambdaQueryWrapper<Paper> wrapper = Wrappers.<Paper>lambdaQuery();
            wrapper.eq(Paper::getYear, year);
            List<Paper> papers = paperMapper.selectList(wrapper);
            return Result.success(papers);
        } catch (Exception e) {
            return Result.error("-1", "查询论文失败：" + e.getMessage());
        }
    }

    @GetMapping("/id/{id}") // 根据论文id查找论文
    public Result<?> findById(@PathVariable Integer id) {
        try {
            Paper paper = paperMapper.selectById(id);
            if (paper == null) {
                return Result.error("-1", "未找到指定ID的论文");
            }
            return Result.success(paper);
        } catch (Exception e) {
            return Result.error("-1", "查询论文失败：" + e.getMessage());
        }
    }

    @GetMapping("/url/{id}")  //根据论文id得到url
    public Result<?> getUrl(@PathVariable Integer id) {
        try {
            Paper paper = paperMapper.selectById(id);
            if (paper == null) {
                return Result.error("-1", "未找到指定ID的论文");
            }
            return Result.success(paper.getUrl());
        } catch (Exception e) {
            return Result.error("-1", "获取论文URL失败：" + e.getMessage());
        }
    }
}