package com.ziv.echosync.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 * 扫描 features 包下所有 @Mapper 接口
 */
@Configuration
@MapperScan("com.ziv.echosync.features")
public class MyBatisPlusConfig {
}
