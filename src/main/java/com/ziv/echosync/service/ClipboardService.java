package com.ziv.echosync.service;

import com.ziv.echosync.Clipboard;

// 这里只定义“要做什么”，不关心“怎么做”
public interface ClipboardService {

    // 同步并广播剪贴板
    String syncClipboard(Clipboard clipboard);

    // 获取最新记录
    Clipboard getLatest();
}