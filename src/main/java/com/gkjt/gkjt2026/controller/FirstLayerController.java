package com.gkjt.gkjt2026.controller;

import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import com.gkjt.gkjt2026.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 🏭 第一层：数据清洗质检台
 * 作用：专门检查 GatewayMessage -> SensorEvent 的转换逻辑是否正确
 */
@RestController
@RequestMapping("/api/first-layer")
public class FirstLayerController {

    // 注入之前的“加工厂”服务 (这里面包含了 Adapter 和 查数据库 的逻辑)
    @Autowired
    private SensorDataService sensorDataService;

    /**
     * 🟢 接口 1: 专业质检 (POST)
     * 作用：你给它一段 JSON，它给你返回转换后的结果
     * 场景：用 Apifox 或 Postman 发送真实数据
     */
    @PostMapping("/convert")
    public SensorEvent convertRealData(@RequestBody GatewayMessage msg) {
        System.out.println(">>> [第一层质检] 收到原始数据: " + msg.getSn());

        // 调用加工厂进行转换
        SensorEvent event = sensorDataService.process(msg);

        if (event == null) {
            System.err.println("❌ 转换失败，请检查 SN 号或设备类型");
        } else {
            System.out.println("✅ 转换成功: " + event);
        }

        return event;
    }

    /**
     * 🔵 接口 2: 懒人模拟 (GET)
     * 作用：浏览器访问一下，自动模拟一条数据进行测试
     * 地址：http://localhost:8080/api/first-layer/simulate
     */
    @GetMapping("/simulate")
    public SensorEvent simulate() {
        // 1. 自己造一个假的网关消息
        GatewayMessage mockMsg = new GatewayMessage();
        mockMsg.setMsgId("AUTO-TEST-001");
        mockMsg.setType("RFID_REPORT");  // 假设是 RFID
        mockMsg.setSn("GATE_WH_01");     // 假设是一楼大门 (数据库里有的)
        mockMsg.setTs(System.currentTimeMillis());
        mockMsg.setIp("192.168.1.100");

        Map<String, Object> payload = new HashMap<>();
        payload.put("rssi", -55);
        payload.put("tags", "测试标签E200...");
        mockMsg.setPayload(payload);

        // 2. 送去转换
        return this.convertRealData(mockMsg);
    }
}