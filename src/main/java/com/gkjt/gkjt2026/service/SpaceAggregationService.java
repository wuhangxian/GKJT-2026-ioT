package com.gkjt.gkjt2026.service;

import com.gkjt.gkjt2026.model.SensorEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 🏊‍♂️ 第二层：时空数据聚合池 (动态时间窗口机制)
 * 作用：按房间（SpaceId）暂存事件。当第一个事件到来时，根据该区域脚本配置开启倒计时。
 * 倒计时结束后，将收集到的所有事件打包送给脚本分析。
 */
@Service
public class SpaceAggregationService {

    @Autowired
    private GroovyScriptService groovyScriptService;

    // 内存池：Key=房间ID, Value=这个房间最近收集到的事件列表
    private final Map<String, List<SensorEvent>> spaceBuffer = new ConcurrentHashMap<>();

    // 计时器锁：记录某个房间是否正在“倒计时”中
    private final Map<String, Boolean> spaceTimerMap = new ConcurrentHashMap<>();

    // 调度器：负责执行定时倒计时任务
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    public void process(SensorEvent event) {
        String spaceId = event.getSpaceId();
        if (spaceId == null) return;

        // 1. 来一个事件，就塞进当前房间的缓存池
        spaceBuffer.computeIfAbsent(spaceId, k -> new java.util.concurrent.CopyOnWriteArrayList<>()).add(event);
        System.out.println(">>> [聚合池] 空间 " + spaceId + " 收到新事件，当前缓存数: " + spaceBuffer.get(spaceId).size());

        // 2. 核心逻辑：如果这个房间目前没有在倒计时，说明这是这波人的“第一个动作”，开启倒计时！
        if (spaceTimerMap.putIfAbsent(spaceId, true) == null) {

            // 🔥 去问问底下的 Groovy 脚本，这个房间需要等几秒？
            int windowSeconds = groovyScriptService.getWindowSeconds(spaceId);
            System.out.println("⏳ [聚合池] 空间 " + spaceId + " 触发传感器，开启 " + windowSeconds + " 秒等待窗口...");

            // 开始倒计时任务
            scheduler.schedule(() -> {
                try {
                    // --- 倒计时结束，开始干活 ---
                    List<SensorEvent> allEvents = spaceBuffer.get(spaceId);

                    if (allEvents != null && !allEvents.isEmpty()) {
                        // 复制一份去处理，防止清理时影响新来的事件
                        List<SensorEvent> eventsToProcess = new ArrayList<>(allEvents);
                        System.out.println("🚀 [聚合池] " + windowSeconds + "秒窗口结束！打包 " + eventsToProcess.size() + " 个事件，提交给脚本引擎...");

                        // 交给 Groovy 脚本执行！
                        groovyScriptService.runSpaceAnalysis(spaceId, eventsToProcess);

                        // 这一波处理完了，把池子清空，准备迎接下一波人
                        allEvents.clear();
                    }
                } finally {
                    // 必须把倒计时标记移除，这样下一波人来的时候才能重新触发倒计时
                    spaceTimerMap.remove(spaceId);
                }
            }, windowSeconds, TimeUnit.SECONDS); // 使用动态读取的时间
        }
    }
}