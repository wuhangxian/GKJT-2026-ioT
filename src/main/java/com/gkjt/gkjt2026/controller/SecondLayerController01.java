package com.gkjt.gkjt2026.controller;

import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import com.gkjt.gkjt2026.service.SensorDataService;
import com.gkjt.gkjt2026.service.SpaceAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 🚀 第二层：自动化业务逻辑控制器
 * 作用：接收网关原始数据 -> 第一层加工(增强) -> 第二层聚合(脚本分析)
 */
//http://localhost:8080/api/second-layer1f/auto-test
@RestController
@RequestMapping("/api/second-layer01")
public class SecondLayerController01 {

    @Autowired
    private SensorDataService sensorDataService; // 第一层：负责查数据库补全字段

    @Autowired
    private SpaceAggregationService spaceAggregationService; // 第二层：负责内存池聚合和脚本运行

    /**
     * 1. 核心上报接口 (POST)
     * 供网关或模拟器调用，传入原始 GatewayMessage
     */
    @PostMapping("/report")
    public String report(@RequestBody GatewayMessage msg) {
        long startTime = System.currentTimeMillis();
        System.out.println(">>> 开始处理消息, MsgId: " + msg.getMsgId());

        // --- 环节 A：调用第一层加工算子 ---
        // 这里会自动根据 SN 查数据库，补齐 location、spaceId 等
        SensorEvent event = sensorDataService.process(msg);

        if (event == null) {
            return "Error: 转换失败，请检查设备是否注册";
        }

        // --- 环节 B：调用第二层聚合算子 ---
        // 它会根据 event 里的 spaceId 自动去读取对应的 Groovy 脚本
        if (event.getSpaceId() != null) {
            spaceAggregationService.process(event);
        }
        long endTime = System.currentTimeMillis();

        // 计算耗时并打印
        long costTime = endTime - startTime;
        System.out.println("<<< 处理完成! 总耗时: " + costTime + " 毫秒 (ms)");
        return "OK";
    }

    /**
     * 2. 自动化测试接口 (GET)
     * 作用：你点一下浏览器，它就自动模拟“人”和“货”两次上报，触发完整流程
     * 地址：http://localhost:8080/api/second-layer/auto-test
     */
    @GetMapping("/auto-test")
    public String autoTest() {
        long now = System.currentTimeMillis();

        // --- 模拟 A：网关发来人脸识别数据 ---
        GatewayMessage faceMsg = new GatewayMessage();
        faceMsg.setMsgId("MOCK-F-101");
        faceMsg.setType("HIK_FACE");
        faceMsg.setSn("FACE_WH_01"); // 数据库中对应的 SN
        faceMsg.setTs(now - 2000);   // 模拟 2 秒前刷脸
        Map<String, Object> facePayload = new HashMap<>();
        facePayload.put("personId", "张三"); // 模拟网关识别出的姓名
        faceMsg.setPayload(facePayload);

        this.report(faceMsg); // 模拟入库

        // --- 模拟 B：网关发来 RFID 标签数据 ---
        GatewayMessage rfidMsg = new GatewayMessage();
        rfidMsg.setMsgId("MOCK-R-202");
        rfidMsg.setType("RFID_REPORT");
        rfidMsg.setSn("GATE_WH_01"); // 数据库中对应的 SN
        rfidMsg.setTs(now);          // 模拟现在经过闸机
        Map<String, Object> rfidPayload = new HashMap<>();
        rfidPayload.put("tags", java.util.Arrays.asList("涉密载体200", "涉密载体201"));
        // 💡 就在这里：直接给 ant 赋值，1代表出门，2代表进门
        rfidPayload.put("ant", 1);
        rfidMsg.setPayload(rfidPayload);

        this.report(rfidMsg); // 模拟入库
        GatewayMessage rfidMsg2 = new GatewayMessage();
        rfidMsg.setMsgId("MOCK-R-203");
        rfidMsg.setType("RFID_REPORT");
        rfidMsg.setSn("GATE_WH_01"); // 数据库中对应的 SN
        rfidMsg.setTs(now);          // 模拟现在经过闸机
        Map<String, Object> rfidPayload2 = new HashMap<>();
        rfidPayload.put("tags", java.util.Arrays.asList("涉密载体205", "涉密载体206"));
        // 💡 就在这里：直接给 ant 赋值，1代表出门，2代表进门
        rfidPayload.put("ant", 1);
        rfidMsg.setPayload(rfidPayload2);

        return "<h1>自动化流程已触发</h1><p>请观察 IDEA 控制台，查看数据增强和脚本聚合的结果！</p>";
    }
}