package com.ziv.echosync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/clipboard") // 统一给这个类的接口加个前缀
public class ClipboardController {

    // @Autowired 相当于 Android Dagger/Hilt 里的 @Inject，让 Spring 自动把 Mapper 注入进来
    @Autowired
    private ClipboardMapper clipboardMapper;

    /**
     * 1. 接收客户端推送的剪贴板内容并保存
     */
    @PostMapping("/sync")
    public String syncClipboard(@RequestBody Clipboard clipboard) {
        // @RequestBody 会自动把客户端传来的 JSON 字符串反序列化成 Clipboard 对象

        // 补全创建时间
        clipboard.setCreateTime(LocalDateTime.now());

        // 调用 MyBatis-Plus 提供的 insert 方法，直接存入数据库！
        clipboardMapper.insert(clipboard);

        return "Sync Success! ID: " + clipboard.getId();
    }

    /**
     * 2. 获取最新的一条剪贴板记录
     */
    @GetMapping("/latest")
    public Clipboard getLatest() {
        // QueryWrapper 是 MyBatis-Plus 提供的神器，用来写条件查询，不用写一行 SQL
        QueryWrapper<Clipboard> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").last("LIMIT 1"); // 按照时间倒序，只取第一条

        return clipboardMapper.selectOne(queryWrapper);
    }
}