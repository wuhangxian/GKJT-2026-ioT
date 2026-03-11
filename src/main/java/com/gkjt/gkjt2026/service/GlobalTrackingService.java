package com.gkjt.gkjt2026.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ⏱️ 全局跨区域追踪器
 * 作用：负责开启倒计时任务，如果物品未在规定时间内到达指定地点，则触发跨区警报
 */
@Service
public class GlobalTrackingService {

    // 定时器线程池
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    // 任务暂存池：Key = 物品ID_目标地点， Value = 倒计时任务本身
    private final Map<String, ScheduledFuture<?>> trackingTasks = new ConcurrentHashMap<>();

    /**
     * 1. 发起期待：限时必须到达
     */
    public void expect(String tagId, String expectedSpaceId, int timeoutSeconds, String alertMsg) {
        String taskId = tagId + "_" + expectedSpaceId;

        System.out.println("⏱️ [全局追踪] 已启动倒计时！限时 " + timeoutSeconds + " 秒内，物品 [" + tagId + "] 必须到达 [" + expectedSpaceId + "]");

        // 开启一个延时任务
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            // 如果时间到了，任务还没被取消（说明没到达），就触发严重报警！
            System.out.println("🚨🚨🚨 【跨区域严重违规】: " + alertMsg);
            // 这里未来可以调用短信接口发给安保主管
            trackingTasks.remove(taskId);
        }, timeoutSeconds, TimeUnit.SECONDS);

        trackingTasks.put(taskId, task);
    }

    /**
     * 2. 成功抵达：解除警报
     */
    public void arrive(String tagId, String currentSpaceId) {
        String taskId = tagId + "_" + currentSpaceId;
        ScheduledFuture<?> task = trackingTasks.remove(taskId);

        if (task != null) {
            // 取消炸弹倒计时！
            task.cancel(false);
            System.out.println("✅ [全局追踪] 物品 [" + tagId + "] 按时抵达 [" + currentSpaceId + "]，警报已成功解除！");
        }
    }
}