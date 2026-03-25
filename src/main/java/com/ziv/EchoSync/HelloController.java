package com.ziv.EchoSync;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 这个注解告诉 Spring：这是一个用来处理 HTTP 请求的类
public class HelloController {

    @GetMapping("/hello") // 映射 GET 请求到 /hello 路径
    public String sayHello() {
        return "Hello Android, EchoSync Backend is running!";
    }
}