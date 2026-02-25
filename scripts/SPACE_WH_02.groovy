// 文件名: scripts/SPACE_WH_01.groovy

// =========================================================
// 🔧 核心配置区 (在这里定义“时间”和“指定的传感器”)
// =========================================================

// 1. 聚合时间窗口 (秒)
def windowSeconds = 5

// 2. 参与聚合的传感器清单 (定义该区域的核心关联设备)
def targetSensors = [
        "门禁相机": "FACE_ACCESS",
        "RFID闸机": "RFID_GATE"
]

// =========================================================

// --- 0. 防御性检查 ---
if (events == null || events.isEmpty()) {
    return null
}

// --- 1. 时间与设备过滤 (第一层筛子) ---
def now = System.currentTimeMillis()
def timeLimit = windowSeconds * 1000

def validEvents = events.findAll { event ->
    // 1.1 检查时间：只取最近 N 秒内的
    boolean isTimeOk = (now - event.timestamp) <= timeLimit

    // 1.2 检查设备：只取配置清单中的设备类型或SN
    boolean isTargetDevice = targetSensors.any { key, value ->
        event.deviceType == value || event.deviceSn == value
    }

    return isTimeOk && isTargetDevice
}

// --- 2. 角色提取与完整性判定 (第二层筛子) ---
// 找到最新的人脸事件
def faceEvent = validEvents.findAll { it.deviceType == "FACE_ACCESS" }.max { it.timestamp }
// 找到最新的RFID事件
def rfidEvent = validEvents.findAll { it.deviceType == "RFID_GATE" }.max { it.timestamp }

// 【关键逻辑】必须“人”和“物”同时出现，才触发聚合逻辑
if (faceEvent == null || rfidEvent == null) {
    return null
}

// --- 3. 提取数据并处理“不可变列表”问题 ---

// ... 前面过滤和提取 events 的代码保持不变 ...

// 3.1 提取人员姓名
def personName = faceEvent.data.personId ?: "未知人员"

// 3.2 提取物品标签（处理不可变列表）
def rawTags = rfidEvent.data.tags
def tagList = rawTags instanceof List ? rawTags.collect().unique() : [rawTags.toString()]
def tagsShow = tagList.join(", ")

// 3.3 🔥【新增】判定进出方向
// 逻辑假设：网关定义 ant=1 为出门，ant=2 为进门 (这通常由硬件安装决定)
def antennaId = rfidEvent.data.ant ?: 0
def direction = "未知"
if (antennaId == 1) {
    direction = "➡️ 【出门/带出】"
} else if (antennaId == 2) {
    direction = "⬅️ 【进门/带入】"
}

// 3.4 确定发生位置与时间
def location = faceEvent.location ?: "未知位置"
def maxTs = Math.max(faceEvent.timestamp, rfidEvent.timestamp)
def timeShow = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(maxTs),
        java.time.ZoneId.systemDefault()
).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

// --- 4. 组装增强版业务报表 ---
def result = """
=========================================
🚨 【二号仓库 -- 涉密载体出入监控告警】 🚨
=========================================
📍 发生位置：${location}
⏰ 判定时间：${timeShow}
↕️ 运行方向：${direction}  
👤 通行人员：${personName}
📦 携带载体：[ ${tagsShow} ]
🤖 触发设备：相机(${faceEvent.deviceSn}) & 闸机(${rfidEvent.deviceSn})
=========================================
"""

return result