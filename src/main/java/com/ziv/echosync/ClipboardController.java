package com.ziv.echosync;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziv.echosync.websocket.EchoWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/clipboard") // 统一给这个类的接口加个前缀
public class ClipboardController {

    // @Autowired 相当于 Android Dagger/Hilt 里的 @Inject，让 Spring 自动把 Mapper 注入进来
    @Autowired
    private ClipboardMapper clipboardMapper;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot 自带的 JSON 处理工具

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

        // 2. 核心合并点：数据落盘后，立刻通过 WebSocket 广播给所有在线设备！
        try {
            // 将对象转为 JSON 字符串
            String jsonMessage = objectMapper.writeValueAsString(clipboard);
            // 广播给所有连接的客户端
            EchoWebSocketServer.broadcast(jsonMessage);
        } catch (Exception e) {
            // 如果转换 JSON 失败，打印错误日志，但不影响返回成功状态
            System.err.println("JSON 转换失败或广播异常: " + e.getMessage());
        }

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