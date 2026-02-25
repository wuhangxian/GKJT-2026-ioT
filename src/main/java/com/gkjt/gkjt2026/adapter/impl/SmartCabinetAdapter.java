package com.gkjt.gkjt2026.adapter.impl;

import com.gkjt.gkjt2026.adapter.DeviceAdapter;
import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * 翻译官 3号: 智能保密柜
 */
@Component
public class SmartCabinetAdapter implements DeviceAdapter {

    @Override
    public String supports() {
        // 对应数据库里的 type = "SMART_CABINET"
        return "SMART_CABINET";
    }

    @Override
    public SensorEvent convert(GatewayMessage msg) {
        return SensorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .deviceSn(msg.getSn())
                .deviceType("CABINET") // 归一化后的类型：柜子
                .deviceIp(msg.getIp())
                .timestamp(msg.getTs())
                .data(msg.getPayload()) // 这里面通常包含 doorStatus(门状态), fileList(在位文件)
                .build();
    }
}