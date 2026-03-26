package com.ziv.echosync;

import com.ziv.echosync.utils.JwtUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    // 模拟一个简单的设备注册/登录接口
    @PostMapping("/api/auth/login")
    public String login(@RequestParam String deviceName) {
        // 在真实业务中，这里应该去查数据库，校验用户名和密码
        // 但对于我们这个自用的剪贴板同步工具，我们只要设备名验证通过，就给它发 Token

        System.out.println("收到设备注册请求: " + deviceName);

        // 签发通行证
        String token = JwtUtils.generateToken(deviceName);

        return "登录成功！你的专属 Token 是:\n" + token;
    }
}