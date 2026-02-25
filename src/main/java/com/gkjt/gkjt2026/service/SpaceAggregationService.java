package com.gkjt.gkjt2026.service;

import com.gkjt.gkjt2026.model.SensorEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🏊‍♂️ 第二层：时空数据聚合池
 * 作用：按房间（SpaceId）暂存最近的事件，并召唤脚本进行分析
 */
@Service
public class SpaceAggregationService {

    @Autowired
    private GroovyScriptService groovyScriptService;

    // 内存池：Key=房间ID, Value=这个房间最近发生的事件列表
    private final Map<String, List<SensorEvent>> spaceBuffer = new ConcurrentHashMap<>();

    // Java端的“保底清理时间” (比如5分钟)
    // 注意：这不是业务逻辑的10秒，而是防止内存溢出的底线
    private static final long MAX_SAFE_TIME_MS = 5 * 60 * 1000;

    public void process(SensorEvent event) {
        String spaceId = event.getSpaceId();
        if (spaceId == null) return;

// 修改后的代码（安全，换成了 CopyOnWriteArrayList）
        spaceBuffer.computeIfAbsent(spaceId, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(event);
        // 2. 【捞数据】获取这个房间目前所有的事件
        List<SensorEvent> allEvents = spaceBuffer.get(spaceId);

        // 3. 【大扫除】清理掉5分钟前的老数据 (Java只管防止内存爆，不管业务)
        long now = System.currentTimeMillis();
        allEvents.removeIf(e -> (now - e.getTimestamp()) > MAX_SAFE_TIME_MS);

        System.out.println(">>> [聚合池] 空间 " + spaceId + " 当前缓存事件数: " + allEvents.size());

        // 4. 【召唤脚本】把这一堆数据扔给脚本，让脚本自己去挑"最近10秒"的
        if (allEvents.size() > 0) {
            groovyScriptService.runSpaceAnalysis(spaceId, allEvents);
        }
    }
}