package com.gkjt.gkjt2026.service;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * 💡 第三层：业务规则判断引擎
 * 作用：接收第二层的动作数据，运行管理员配置的傻瓜式告警规则
 */
@Service
public class RuleEngineService {

    @Value("${app.script.path}")
    private String scriptPath;

    public void executeRules(Map<String, Object> actionData) {
        File ruleFile = new File(scriptPath, "GLOBAL_RULES.groovy");
        if (!ruleFile.exists()) return;

        try {
            String ruleContent = Files.readString(ruleFile.toPath(), StandardCharsets.UTF_8);

            // 准备提供给小白脚本使用的工具环境
            Binding binding = new Binding();
            binding.setVariable("action", actionData); // 注入刚才传过来的动作数据
            binding.setVariable("alert", new AlertApi()); // 注入报警工具
            binding.setVariable("timer", new TimerApi()); // 注入跨区倒计时工具

            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(ruleContent);

        } catch (Exception e) {
            System.err.println("❌ 规则脚本执行出错");
            e.printStackTrace();
        }
    }

    // ================== 提供给脚本的极简 API 工具类 ==================

    public class AlertApi {
        public void send(String type, String message) {
            System.out.println("🚨🚨🚨 【系统告警触发】 类型: " + type + " | 详情: " + message);
            // 未来这里可以对接发短信、企业微信推送、记录到数据库告警表等逻辑
        }
    }

    public class TimerApi {
        public void startTracking(String tagId, String personName, String fromSpace, String toSpace, int seconds) {
            System.out.println("⏱️ 【开启跨区追踪】 人员["+personName+"] 携带载体["+tagId+"] 从 "+fromSpace+" 出发。");
            System.out.println("   要求在 " + seconds + " 秒内到达 " + toSpace + "，否则将触发违规报警！");

            // 【实现思路】：实际项目中，这里可以把这条记录写入 Redis，设置过期时间为 seconds
            // 监听 Redis 的 key 过期事件。如果过期了还没被删除，就触发报警。
        }

        public void stopTracking(String tagId, String currentSpace) {
            System.out.println("✅ 【跨区追踪结束】 载体["+tagId+"] 已安全到达 " + currentSpace + "，销毁定时器。");

            // 【实现思路】：实际项目中，就是去 Redis 里把刚才那个 key 删掉
        }
    }
}