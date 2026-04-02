package com.ziv.echosync.features.clipboard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziv.echosync.features.clipboard.dto.ClipboardSyncDTO;
import com.ziv.echosync.features.clipboard.entity.Clipboard;
import com.ziv.echosync.features.clipboard.vo.ClipboardVO;
import com.ziv.echosync.features.websocket.EchoWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class ClipboardServiceImpl implements ClipboardService {

    private static final String CACHE_KEY = "clipboard:latest";

    @Autowired
    private ClipboardMapper clipboardMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ClipboardVO syncClipboard(ClipboardSyncDTO dto) {
        // 1. DTO → Entity
        Clipboard clipboard = new Clipboard();
        clipboard.setContent(dto.getContent());
        clipboard.setDeviceName(dto.getDeviceName());
        clipboard.setCreateTime(LocalDateTime.now());

        // 2. 持久化到数据库
        clipboardMapper.insert(clipboard);

        // 3. 更新 Redis 缓存（TTL 5 分钟）
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, clipboard, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("Redis 写入失败，跳过缓存: " + e.getMessage());
        }

        // 4. WebSocket 广播
        try {
            String json = objectMapper.writeValueAsString(clipboard);
            EchoWebSocketServer.broadcast(json);
        } catch (Exception e) {
            System.err.println("WebSocket 广播失败: " + e.getMessage());
        }

        // 5. Entity → VO
        return toVO(clipboard);
    }

    @Override
    public ClipboardVO getLatest() {
        // 1. 优先读 Redis 缓存
        try {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached instanceof Clipboard clipboard) {
                return toVO(clipboard);
            }
        } catch (Exception e) {
            System.err.println("Redis 读取失败，降级查数据库: " + e.getMessage());
        }

        // 2. 缓存未命中，查数据库
        QueryWrapper<Clipboard> qw = new QueryWrapper<>();
        qw.orderByDesc("create_time").last("LIMIT 1");
        Clipboard clipboard = clipboardMapper.selectOne(qw);

        // 3. 回填缓存
        if (clipboard != null) {
            try {
                redisTemplate.opsForValue().set(CACHE_KEY, clipboard, 5, TimeUnit.MINUTES);
            } catch (Exception e) {
                System.err.println("Redis 回填失败: " + e.getMessage());
            }
        }

        return clipboard != null ? toVO(clipboard) : null;
    }

    private ClipboardVO toVO(Clipboard clipboard) {
        ClipboardVO vo = new ClipboardVO();
        vo.setId(clipboard.getId());
        vo.setContent(clipboard.getContent());
        vo.setDeviceName(clipboard.getDeviceName());
        vo.setCreateTime(clipboard.getCreateTime());
        return vo;
    }
}
