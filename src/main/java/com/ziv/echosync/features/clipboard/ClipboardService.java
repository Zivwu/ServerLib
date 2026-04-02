package com.ziv.echosync.features.clipboard;

import com.ziv.echosync.features.clipboard.dto.ClipboardSyncDTO;
import com.ziv.echosync.features.clipboard.vo.ClipboardVO;

public interface ClipboardService {

    ClipboardVO syncClipboard(ClipboardSyncDTO dto);

    ClipboardVO getLatest();
}
