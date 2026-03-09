// 文件名: scripts/SPACE_WH_02.groovy

// =========================================================
// 🔧 核心配置区 (二号仓库专属)
// =========================================================

// 1. 聚合时间窗口 (秒)
def windowSeconds = 5

// 2. 参与聚合的传感器清单 (精准锁定二号仓库的设备 SN)
def targetSensors = [
        "门禁相机": "FACE_WH_02", // 🔥精准绑定
        "RFID闸机": "GATE_WH_02"  // 🔥精准绑定
]

// =========================================================

// --- 0. 防御性检查 ---
if (events == null || events.isEmpty()) {
    return null
}

// --- 1. 设备过滤 (第一层筛子) ---
// 🔥彻底删除了时间计算代码！完全信任底层 Java 传来的 5 秒数据包！
def validEvents = events.findAll { event ->
    boolean isTargetDevice = targetSensors.any { key, value ->
        event.deviceType == value || event.deviceSn == value
    }
    return isTargetDevice
}

// --- 2. 角色提取 (第二层筛子) ---
// 找到最新的人脸事件
def faceEvent = validEvents.findAll { it.deviceType == "FACE_ACCESS" }.max { it.timestamp }
// 找到最新的RFID事件
def rfidEvent = validEvents.findAll { it.deviceType == "RFID_GATE" }.max { it.timestamp }

// 如果两者都没有，说明没有有效事件，直接退出
if (faceEvent == null && rfidEvent == null) {
    return null
}

// --- 3. 安全的数据提取与情景判定 (彻底解决空指针异常) ---

// 3.1 提取人员姓名
def personName = "【异常未识别/无人】"
if (faceEvent != null && faceEvent.data?.personId) {
    personName = faceEvent.data.personId
}

// 3.2 提取物品标签与进出方向
def tagsDetail = []
def antennaId = 0
if (rfidEvent != null) {
    tagsDetail = rfidEvent.data?.tagsDetail ?: []
    antennaId = rfidEvent.data?.ant ?: 0
}

// 3.3 判定通行场景分类
def sceneType = ""
if (faceEvent != null && rfidEvent != null) {
    sceneType = "👤📦 人物协同通行"
} else if (faceEvent != null && rfidEvent == null) {
    sceneType = "👤 人员空手通行"
} else if (faceEvent == null && rfidEvent != null) {
    sceneType = "⚠️📦 严重异常：仅载体移动 (抛掷/尾随/漏抓拍)"
}

// 如果既没有有效人脸，也没有读到任何有效载体，过滤掉无效触发
if (personName == "【异常未识别/无人】" && tagsDetail.isEmpty()) {
    return null
}

// 3.4 格式化展示物品
def tagsShow = "无载体 (空手)"
if (!tagsDetail.isEmpty()) {
    tagsShow = tagsDetail.collect { "【${it.secretLevel ?: '未知'}】${it.name ?: '未知物品'} (属:${it.owner ?: '未知'})" }.join("\n           ")
}

// 3.5 判定进出方向
def direction = "未知方向"
def directionCode = "UNKNOWN"
if (antennaId == 1) {
    direction = "➡️ 【出门/带出】"
    directionCode = "OUT"
} else if (antennaId == 2) {
    direction = "⬅️ 【进门/带入】"
    directionCode = "IN"
} else if (faceEvent != null && rfidEvent == null) {
    direction = "🚶 【人员通行】"
}

// 3.6 确定发生位置与时间 (安全合并)
def location = (faceEvent?.location) ?: (rfidEvent?.location ?: "一楼二号仓库门")

long maxTs = 0
if (faceEvent != null && rfidEvent != null) {
    maxTs = Math.max(faceEvent.timestamp, rfidEvent.timestamp)
} else if (faceEvent != null) {
    maxTs = faceEvent.timestamp
} else {
    maxTs = rfidEvent.timestamp
}

def timeShow = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(maxTs),
        java.time.ZoneId.systemDefault()
).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

// --- 4. 组装增强版业务报表 ---
def result = """
=========================================
🚨 【二号仓库 (SPACE_WH_02) 事件捕获】 🚨
=========================================
📍 发生位置：${location}
⏰ 判定时间：${timeShow}
🏷️ 场景判定：${sceneType}
↕️ 运行方向：${direction}  
👤 通行人员：${personName}
📦 涉及载体：
           ${tagsShow}
🤖 触发设备：相机(${faceEvent?.deviceSn ?: '无'}) & 闸机(${rfidEvent?.deviceSn ?: '无'})
=========================================
"""

return [
        "report": result,
        "actionData": [
                "spaceId": "SPACE_WH_02",
                "personName": personName,
                "direction": directionCode,
                "tags": tagsDetail,
                "sceneType": sceneType
        ]
]