package com.gkjt.gkjt2026.adapter.impl;

import com.gkjt.gkjt2026.adapter.DeviceAdapter;
import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * 翻译官 4号: 移动手持机 (PDA)
 */
@Component
public class HandheldAdapter implements DeviceAdapter {

    @Override
    public String supports() {
        // 对应数据库里的 type = "HANDHELD"
        return "HANDHELD";
    }

    @Override
    public SensorEvent convert(GatewayMessage msg) {
        return SensorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .deviceSn(msg.getSn())
                .deviceType("PDA")      // 归一化后的类型：手持机
                .deviceIp(msg.getIp())  // 注意：手持机IP可能是动态的
                .timestamp(msg.getTs())
                .data(msg.getPayload()) // 这里面通常包含 gps(定位), scanUser(操作员)
                .build();
    }
}