package com.ziv.echosync;

import com.ziv.echosync.service.ClipboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/clipboard") // 统一给这个类的接口加个前缀
public class ClipboardController {

    // 以前注入 Mapper，现在只注入 Service (注意注入的是接口，这是面向接口编程的思想)
    @Autowired
    private ClipboardService clipboardService;

    @PostMapping("/sync")
    public String syncClipboard(@RequestBody Clipboard clipboard) {
        // Controller 彻底变成了一个“甩手掌柜”，一行代码搞定调用
        return clipboardService.syncClipboard(clipboard);
    }

    @GetMapping("/latest")
    public Clipboard getLatest() {
        return clipboardService.getLatest();
    }
}