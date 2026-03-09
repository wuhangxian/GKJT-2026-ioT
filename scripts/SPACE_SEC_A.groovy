// 文件名: scripts/SPACE_SEC_A.groovy

// =========================================================
// 🔧 核心配置区 (二楼保密室专属)
// =========================================================

// 1. 聚合时间窗口 (秒)
def windowSeconds = 5

// 2. 参与聚合的传感器清单 (精准绑定保密室的设备 SN，绝对防串台)
def targetSensors = [
        "保密室相机": "FACE_SEC_A", // 数据库里的保密室相机
        "保密室RFID": "GATE_SEC_A"  // 刚才在数据库补上的保密室RFID感知门
]

// =========================================================

// --- 0. 防御性检查 ---
if (events == null || events.isEmpty()) {
    return null
}

// --- 1. 设备过滤 (第一层筛子) ---
// 🔥 彻底删掉了时间过滤代码！完全信任 Java 层传来的 5 秒数据包！
def validEvents = events.findAll { event ->
    // 只留下 SN 码属于保密室的设备数据，其他的全扔掉
    return targetSensors.containsValue(event.deviceSn)
}

// --- 2. 角色提取 (第二层筛子) ---
// 🔥 改进：直接用保密室的设备 SN 去提取事件，百分之百不会认错人！
def faceEvent = validEvents.findAll { it.deviceSn == "FACE_SEC_A" }.max { it.timestamp }
def rfidEvent = validEvents.findAll { it.deviceSn == "GATE_SEC_A" }.max { it.timestamp }

// 如果既没人刷脸，也没扫到物品，说明是无效触发，直接退出
if (faceEvent == null && rfidEvent == null) {
    return null
}

// --- 3. 安全的数据提取与情景判定 ---

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

// 3.5 判定进出方向 (保密室专属文案)
def direction = "未知方向"
def directionCode = "UNKNOWN"
if (antennaId == 1) {
    direction = "➡️ 【带出保密室】"
    directionCode = "OUT"
} else if (antennaId == 2) {
    direction = "⬅️ 【带入保密室】"
    directionCode = "IN"
} else if (faceEvent != null && rfidEvent == null) {
    direction = "🚶 【人员通行】"
}

// 3.6 确定发生位置与时间 (安全合并)
def location = (faceEvent?.location) ?: (rfidEvent?.location ?: "二楼保密室")

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
// 🔥 彻底净化：将这里的所有文字改成了“二楼保密室”
def result = """
=========================================
🚨 【二楼保密室 (SPACE_SEC_A) 事件捕获】 🚨
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
                "spaceId": "SPACE_SEC_A",    // 🔥 极其重要：告诉第三层，我是保密室！
                "personName": personName,
                "direction": directionCode,
                "tags": tagsDetail,
                "sceneType": sceneType
        ]
]