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
 * 🚀 二楼保密室 (SPACE_SEC_A) 专属接入控制器
 */
// 接口路径也改成了和保密室相关的名字
@RestController
@RequestMapping("/api/space-sec")
public class SpaceSecController {

    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private SpaceAggregationService spaceAggregationService;

    /**
     * 1. 核心上报接口 (POST)
     */
    @PostMapping("/report")
    public String report(@RequestBody GatewayMessage msg) {
        long startTime = System.currentTimeMillis();
        System.out.println(">>> [二楼保密室] 收到传感器消息, MsgId: " + msg.getMsgId());

        // --- 环节 A：调用第一层加工算子 ---
        SensorEvent event = sensorDataService.process(msg);

        if (event == null) {
            return "Error: 转换失败，请检查设备是否注册";
        }

        // --- 环节 B：调用第二层聚合算子 ---
        if (event.getSpaceId() != null) {
            spaceAggregationService.process(event);
        }

        long costTime = System.currentTimeMillis() - startTime;
        System.out.println("<<< [二楼保密室] 消息入池完成! 耗时: " + costTime + " ms");
        return "OK";
    }

    /**
     * 2. 自动化测试接口 (GET)
     * 地址：http://localhost:8080/api/space-sec/auto-test
     */
    @GetMapping("/auto-test")
    public String autoTest() {
        long now = System.currentTimeMillis();

        // --- 模拟 A：二楼保密室的人脸相机 (数据库里配置的保密室相机 SN) ---
        GatewayMessage faceMsg = new GatewayMessage();
        faceMsg.setMsgId("MOCK-SEC-F1");
        faceMsg.setType("HIK_FACE");
        faceMsg.setSn("FACE_SEC_A"); // 对应数据库：二楼保密室联动相机
        faceMsg.setTs(now - 1000);
        Map<String, Object> facePayload = new HashMap<>();
        facePayload.put("personId", "张三"); // 模拟张三进入了保密室
        faceMsg.setPayload(facePayload);

        this.report(faceMsg);

        // --- 模拟 B：二楼保密室的智能柜/RFID读取到了物品带出 ---
        // 注意：你的数据库目前只配了两个RFID闸机，为了测试保密室规则，
        // 我们临时假装 "GATE_WH_01" 被搬到了二楼，或者强行把它当做二楼的设备报上来。
        // （实际项目中，你应该在数据库插入一个 SN="GATE_SEC_A" 的二楼RFID设备）
        GatewayMessage rfidMsg = new GatewayMessage();
        rfidMsg.setMsgId("MOCK-SEC-R2");
        rfidMsg.setType("RFID_REPORT");

        // 🚨 技巧：为了让底层查库时把它识别为保密室的事件，我们借用一下二楼相机的SN，
        // 或者你需要去 sys_device 表加一行专门的二楼 RFID 设备。
        // 这里为了你能直接跑通，我们假装人脸机也附带了读RFID的功能：
        rfidMsg.setSn("FACE_SEC_A");

        rfidMsg.setTs(now);
        Map<String, Object> rfidPayload = new HashMap<>();
        rfidPayload.put("tags", java.util.Arrays.asList("涉密载体301")); // 张三试图拿走李四的绝密笔记本
        rfidPayload.put("ant", 1); // 1代表带出(OUT)
        rfidMsg.setPayload(rfidPayload);

        this.report(rfidMsg);

        return "<h1>二楼保密室 (SPACE_SEC_A) 测试已触发！</h1><p>请看控制台是否触发了最高警报和死锁！</p>";
    }
}