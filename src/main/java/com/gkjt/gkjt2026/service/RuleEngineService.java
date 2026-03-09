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
        if (!ruleFile.exists()) {
            System.out.println(">>> [第三层] 未找到全局规则脚本 GLOBAL_RULES.groovy");
            return;
        }

        try {
            String ruleContent = Files.readString(ruleFile.toPath(), StandardCharsets.UTF_8);

            Binding binding = new Binding();
            binding.setVariable("action", actionData); // 注入底层传来的动作数据


            binding.setVariable("alert", new AlertApi());
            binding.setVariable("door", new DoorApi(actionData.get("spaceId").toString()));

            GroovyShell shell = new GroovyShell(binding);
            System.out.println("⚖️ [第三层] 开始执行全局业务规则判定...");
            shell.evaluate(ruleContent);

        } catch (Exception e) {
            System.err.println("❌ 规则脚本执行出错");
            e.printStackTrace();
        }
    }


    public class AlertApi {
        public void send(String level, String message) {
            System.out.println("🚨🚨🚨 【系统告警】 级别: [" + level + "] | 详情: " + message);
            // 未来这里可以写代码发短信、推送企业微信
        }
    }

    public class DoorApi {
        private String space;
        public DoorApi(String space) { this.space = space; }

        public void block(String reason) {
            System.out.println("⛔⛔⛔ 【闸机控制】 已向 [" + space + "] 发送关门/锁死指令！原因: " + reason);
            // 未来这里可以通过硬件接口真正把门锁死
        }

        public void open(String reason) {
            System.out.println("✅✅✅ 【闸机控制】 验证通过，允许放行 [" + space + "]！原因: " + reason);
        }
    }
}