package com.gkjt.gkjt2026.adapter.impl;

import com.gkjt.gkjt2026.adapter.DeviceAdapter;
import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * 翻译官 2号: 海康威视人脸识别
 */
@Component
public class HikFaceAdapter implements DeviceAdapter {

    @Override
    public String supports() {
        // 对应数据库里的 type = "HIK_FACE"
        return "HIK_FACE";
    }

    @Override
    public SensorEvent convert(GatewayMessage msg) {
        return SensorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .deviceSn(msg.getSn())
                .deviceType("FACE_ACCESS") // 归一化后的类型：人脸门禁
                .deviceIp(msg.getIp())
                .timestamp(msg.getTs())
                .data(msg.getPayload())    // 这里面通常包含 personId, similarity(相似度)
                .build();
    }
}