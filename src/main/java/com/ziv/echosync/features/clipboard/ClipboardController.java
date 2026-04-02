package com.ziv.echosync.features.clipboard;

import com.ziv.echosync.common.result.Result;
import com.ziv.echosync.features.clipboard.dto.ClipboardSyncDTO;
import com.ziv.echosync.features.clipboard.vo.ClipboardVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clipboard")
public class ClipboardController {

    @Autowired
    private ClipboardService clipboardService;

    @PostMapping("/sync")
    public Result<ClipboardVO> syncClipboard(@Valid @RequestBody ClipboardSyncDTO dto) {
        return Result.success(clipboardService.syncClipboard(dto));
    }

    @GetMapping("/latest")
    public Result<ClipboardVO> getLatest() {
        return Result.success(clipboardService.getLatest());
    }
}
