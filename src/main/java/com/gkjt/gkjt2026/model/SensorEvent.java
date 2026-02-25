package com.gkjt.gkjt2026.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Map;

/**
 * 2. 归一化后的标准事件 (含背景信息)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorEvent implements Serializable {
    // --- 1. 物理层信息 (从网关来的) ---
    private String eventId;
    private String deviceSn;
    private String deviceType;
    private String deviceIp;
    private Long timestamp;

    // --- 2. 载荷数据 (具体读到了什么) ---
    private Map<String, Object> data;

    // --- 3. 业务层背景信息 (我们即将补全的) ---

    // 位置在哪里？(供脚本判断)
    private String location;     // 例如: "一楼大厅"
    private String spaceId;      // 例如: "SPACE_001"

    // 谁负责？(供告警使用)
    private String deptId;       // 例如: "DEPT_10" (部门10)
    private String manager;      // manager (管理员/责任人)

    // 上下文 (预留)
    private Map<String, Object> context;
}