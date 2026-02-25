package com.gkjt.gkjt2026.adapter;

import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;

/**
 * 3. 适配器接口
 * 规定所有翻译官必须干的两件事：支持谁？怎么转？
 */
public interface DeviceAdapter {
    // 我支持哪种消息类型？
    String supports();

    // 把信封转换成标准件
    SensorEvent convert(GatewayMessage msg);
}