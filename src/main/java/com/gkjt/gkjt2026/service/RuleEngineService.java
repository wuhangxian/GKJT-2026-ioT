package com.gkjt.gkjt2026.service;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.beans.factory.annotation.Autowired;
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

    // 🔥 修复 1：@Autowired 必须写在这里（类的成员变量），不能写在方法里面！
    @Autowired
    private GlobalTrackingService trackingService;

    public void executeRules(Map<String, Object> actionData) {

        // 1. 获取当前发生事件的地区 ID (比如 SPACE_WH_01)
        String spaceId = actionData.get("spaceId").toString();

        // 2. 动态拼接专属规则脚本的名字，例如：RULE_SPACE_WH_01.groovy
        File ruleFile = new File(scriptPath, "RULE_" + spaceId + ".groovy");

        // 3. 兜底逻辑：如果这个地区没有专门的脚本，可以退而求其次找全局脚本
        if (!ruleFile.exists()) {
            System.out.println(">>> [第三层] 地区 [" + spaceId + "] 没有专属规则，尝试加载全局规则...");
            ruleFile = new File(scriptPath, "GLOBAL_RULES.groovy");
            if (!ruleFile.exists()) {
                System.out.println(">>> [第三层] 无任何可用规则脚本，放行。");
                return;
            }
        }

        try {
            String ruleContent = Files.readString(ruleFile.toPath(), StandardCharsets.UTF_8);

            Binding binding = new Binding();
            binding.setVariable("action", actionData); // 注入底层传来的动作数据

            // 注入三大神器：报警器、闸机控制器、跨区追踪器
            binding.setVariable("alert", new AlertApi());
            binding.setVariable("door", new DoorApi(actionData.get("spaceId").toString()));
            binding.setVariable("tracker", new TrackerApi()); // 🔥 这里就不会报错了

            GroovyShell shell = new GroovyShell(binding);
            System.out.println("⚖️ [第三层] 开始执行业务规则判定...");
            shell.evaluate(ruleContent);

        } catch (Exception e) {
            System.err.println("❌ 规则脚本执行出错");
            e.printStackTrace();
        }
    }


    // ================== 提供给小白脚本使用的 API 工具类 ==================

    public class AlertApi {
        public void send(String level, String message) {
            System.out.println("🚨🚨🚨 【系统告警】 级别: [" + level + "] | 详情: " + message);
        }
    }

    public class DoorApi {
        private String space;
        public DoorApi(String space) { this.space = space; }

        public void block(String reason) {
            System.out.println("⛔⛔⛔ 【闸机控制】 已向 [" + space + "] 发送关门/锁死指令！原因: " + reason);
        }

        public void open(String reason) {
            System.out.println("✅✅✅ 【闸机控制】 验证通过，允许放行 [" + space + "]！原因: " + reason);
        }
    }

    // 🔥 修复 2：加上刚刚遗漏的追踪器 API 内部类！
    public class TrackerApi {
        // expect: 期待某个物品在几秒内到达某个地点
        public void expect(String tagId, String targetSpace, int seconds, String msg) {
            trackingService.expect(tagId, targetSpace, seconds, msg);
        }
        // arrive: 报告某个物品已经安全抵达
        public void arrive(String tagId, String currentSpace) {
            trackingService.arrive(tagId, currentSpace);
        }
    }
}