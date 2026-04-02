package com.ziv.echosync.features.clipboard;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ziv.echosync.features.clipboard.entity.Clipboard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 剪贴板数据访问层
 * 继承 BaseMapper 即可免费获得所有基础 CRUD 方法
 */
@Mapper
public interface ClipboardMapper extends BaseMapper<Clipboard> {
}
