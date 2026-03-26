package com.ziv.echosync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziv.echosync.Clipboard;
import com.ziv.echosync.ClipboardMapper;
import com.ziv.echosync.service.ClipboardService;
import com.ziv.echosync.websocket.EchoWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service // ⚠️ 极其重要：告诉 Spring 这是一个业务逻辑层的 Bean，请把它装进容器里
public class ClipboardServiceImpl implements ClipboardService {

    // 把原来 Controller 里的依赖全部转移到这里
    @Autowired
    private ClipboardMapper clipboardMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String syncClipboard(Clipboard clipboard) {
        // 1. 写数据库
        clipboard.setCreateTime(LocalDateTime.now());
        clipboardMapper.insert(clipboard);

        // 2. 转 JSON 并广播
        try {
            String jsonMessage = objectMapper.writeValueAsString(clipboard);
            EchoWebSocketServer.broadcast(jsonMessage);
        } catch (Exception e) {
            System.err.println("JSON 转换失败或广播异常: " + e.getMessage());
        }

        return "Sync Success! ID: " + clipboard.getId();
    }

    @Override
    public Clipboard getLatest() {
        // 组装查询条件
        QueryWrapper<Clipboard> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time").last("LIMIT 1");
        return clipboardMapper.selectOne(queryWrapper);
    }
}