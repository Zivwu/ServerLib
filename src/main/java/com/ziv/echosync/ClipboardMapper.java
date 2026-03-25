package com.ziv.echosync;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 把这个接口交给 Spring 管理
public interface ClipboardMapper extends BaseMapper<Clipboard> {
    // 继承了 BaseMapper 后，你不用写任何 SQL 语句，就已经免费获得了所有的增删改查方法！
}