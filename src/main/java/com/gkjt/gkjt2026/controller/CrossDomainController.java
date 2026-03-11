package com.gkjt.gkjt2026.controller;

import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 🌐 跨区域状态追踪专属控制器 (Cross-Domain Tracking)
 * 作用：模拟和处理物品跨越物理空间（如：一楼大门 -> 保密室）的流转事件
 */
@RestController
@RequestMapping("/api/cross-domain") // 🔥 路由也改得非常专业
public class CrossDomainController {

    // 注入一号大门的控制器
    @Autowired
    private SecondLayerController01 controller01;

    // 注入二楼保密室的控制器
    @Autowired
    private SpaceSecController spaceSecController;

    /**
     * ✅ 测试场景 1：10秒内成功抵达保密室 (正常合规)
     * 测试地址: http://localhost:8080/api/cross-domain/track-success
     */
    @GetMapping("/track-success")
    public String trackSuccess() throws InterruptedException {
        long now = System.currentTimeMillis();
        System.out.println("\n========== [🌐 跨区追踪：10秒内成功送达] ==========");

        // 1. 动作一：李四从一楼大门带出绝密笔记本 (ant=1 代表带出)
        System.out.println(">>> [真实模拟] 李四正从一号大门(SPACE_WH_01)带出绝密文件...");
        controller01.report(createFaceMsg("FACE_WH_01", "李四", now));
        controller01.report(createRfidMsg("GATE_WH_01", Arrays.asList("涉密载体301"), 1, now));

        // 2. 模拟物理世界的时间流逝：李四爬楼梯花了 5 秒钟
        System.out.println(">>> ⏳ (模拟李四在走廊移动中，等待5秒...)");
        Thread.sleep(5000);

        // 3. 动作二：5秒后，李四到达二楼保密室，把文件存入 (ant=2 代表带入)
        long later = System.currentTimeMillis();
        System.out.println(">>> [真实模拟] 李四到达保密室(SPACE_SEC_A)，准备存入文件...");
        spaceSecController.report(createFaceMsg("FACE_SEC_A", "李四", later));
        spaceSecController.report(createRfidMsg("GATE_SEC_A", Arrays.asList("涉密载体301"), 2, later));

        return "<h3>✅ 跨区追踪场景1测试完成：5秒内成功送达保密室！</h3><p>请查看IDEA控制台，您应该能看到【警报已成功解除】的绿色提示。</p>";
    }

    /**
     * 🚨 测试场景 2：中途墨迹超过10秒未送达 (触发跨区大警报)
     * 测试地址: http://localhost:8080/api/cross-domain/track-fail
     */
    @GetMapping("/track-fail")
    public String trackFail() throws InterruptedException {
        long now = System.currentTimeMillis();
        System.out.println("\n========== [🌐 跨区追踪：超过10秒未送达 (中途丢失)] ==========");

        // 1. 动作一：李四从一楼大门带出绝密笔记本
        System.out.println(">>> [真实模拟] 李四正从一号大门(SPACE_WH_01)带出绝密文件...");
        controller01.report(createFaceMsg("FACE_WH_01", "李四", now));
        controller01.report(createRfidMsg("GATE_WH_01", Arrays.asList("涉密载体301"), 1, now));

        // 2. 模拟异常情况：李四中途去抽了根烟，花了 13 秒钟！
        System.out.println(">>> ⏳ (模拟李四去抽烟了，程序将等待13秒... 盯紧控制台，第10秒会爆炸！)");
        Thread.sleep(13000);

        // 3. 动作二：13秒后李四才姗姗来迟到达保密室
        long later = System.currentTimeMillis();
        System.out.println(">>> [真实模拟] 13秒后李四终于到达保密室，但警报应该早就响了...");
        spaceSecController.report(createFaceMsg("FACE_SEC_A", "李四", later));
        spaceSecController.report(createRfidMsg("GATE_SEC_A", Arrays.asList("涉密载体301"), 2, later));

        return "<h3>🚨 跨区追踪场景2测试完成：超出10秒送达！</h3><p>请查看IDEA控制台，您应该会在等待的过程中，看到红色高亮的【跨区域严重违规】警报！</p>";
    }

    // --- 辅助造数据的工具方法 ---
    private GatewayMessage createFaceMsg(String sn, String person, long ts) {
        GatewayMessage msg = new GatewayMessage();
        msg.setMsgId("F-" + sn + "-" + ts);
        msg.setType("HIK_FACE");
        msg.setSn(sn);
        msg.setTs(ts);
        Map<String, Object> payload = new HashMap<>();
        payload.put("personId", person);
        msg.setPayload(payload);
        return msg;
    }

    private GatewayMessage createRfidMsg(String sn, java.util.List<String> tags, int ant, long ts) {
        GatewayMessage msg = new GatewayMessage();
        msg.setMsgId("R-" + sn + "-" + ts);
        msg.setType("RFID_REPORT");
        msg.setSn(sn);
        msg.setTs(ts);
        Map<String, Object> payload = new HashMap<>();
        payload.put("tags", tags);
        payload.put("ant", ant);
        msg.setPayload(payload);
        return msg;
    }
}