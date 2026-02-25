package com.gkjt.gkjt2026.adapter.impl;

import com.gkjt.gkjt2026.adapter.DeviceAdapter;
import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * 4. RFID 专门的翻译官
 */
@Component // 必须加这个，Spring 才能发现它
public class RfidAdapter implements DeviceAdapter {

    @Override
    public String supports() {
        // 只要网关发来的 type 是 "RFID_REPORT"，我就接单
        return "RFID_REPORT";
    }

    @Override
    public SensorEvent convert(GatewayMessage msg) {
        // 这里可以写复杂的清洗逻辑，现在先做最简单的透传
        return SensorEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .deviceSn(msg.getSn())
                .deviceType("RFID_GATE") // 统一归类
                .deviceIp(msg.getIp())
                .timestamp(msg.getTs())
                .data(msg.getPayload())  // 把核心数据塞进去
                .build();
    }
}