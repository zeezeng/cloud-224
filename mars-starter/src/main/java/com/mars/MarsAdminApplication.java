package com.mars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Mars Admin 启动类
 */
@SpringBootApplication
@EnableScheduling
public class MarsAdminApplication {

    public static void main(String[] args) {
        // 斗鱼弹幕服务器仅支持 TLS_RSA_* 静态RSA密钥交换套件，而 JDK 默认在
        // jdk.tls.disabledAlgorithms 中禁用了 TLS_RSA_*。必须在 JSSE 首次初始化前置空，
        // 否则运行时再修改 Security 属性已无效（JSSE 首用即缓存）。
        java.security.Security.setProperty("jdk.tls.disabledAlgorithms", "");
        SpringApplication.run(MarsAdminApplication.class, args);
    }
}
