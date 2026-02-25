package com.gkjt.gkjt2026.adapter;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 5. 适配器工厂 (调度员)
 */
@Component
public class AdapterFactory {

    // 一个花名册：记录了 "RFID_REPORT" -> RfidAdapter
    private final Map<String, DeviceAdapter> adapterMap = new ConcurrentHashMap<>();

    // 构造函数：Spring 启动时会自动把所有的 Adapter 塞给这个 list
    public AdapterFactory(List<DeviceAdapter> adapters) {
        for (DeviceAdapter adapter : adapters) {
            adapterMap.put(adapter.supports(), adapter);
            System.out.println(">>> 已加载适配器: " + adapter.supports());
        }
    }

    // 根据类型查找对应的翻译官
    public DeviceAdapter getAdapter(String type) {
        return adapterMap.get(type);
    }
}