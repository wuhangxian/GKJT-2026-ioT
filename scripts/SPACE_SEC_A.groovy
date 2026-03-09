// 文件名: scripts/SPACE_WH_1F.groovy

// =========================================================
// 🔧 核心配置区 (在这里定义“时间”和“指定的传感器”)
// =========================================================

// 1. 聚合时间窗口 (秒)
def windowSeconds = 5

// 2. 参与聚合的传感器清单 (定义该区域的核心关联设备)
def targetSensors = [
        "保密室相机": "FACE_SEC_A",
        "保密室RFID": "RFID_REPORT" // 模糊匹配类型
]

// =========================================================

// --- 0. 防御性检查 ---
if (events == null || events.isEmpty()) {
    return null
}

// --- 1. 时间与设备过滤 (第一层筛子) ---
// 找到这批事件里时间最晚的那个，作为计算 5 秒窗口的“现在”
def now = events.max { it.timestamp }.timestamp
def timeLimit = windowSeconds * 1000

def validEvents = events.findAll { event ->
    boolean isTimeOk = (now - event.timestamp) <= timeLimit
    boolean isTargetDevice = targetSensors.any { key, value ->
        event.deviceType == value || event.deviceSn == value
    }
    return isTimeOk && isTargetDevice
}

// --- 2. 角色提取 (第二层筛子) ---
def faceEvent = validEvents.findAll { it.deviceType == "FACE_ACCESS" }.max { it.timestamp }
def rfidEvent = validEvents.findAll { it.deviceType == "RFID_GATE" }.max { it.timestamp }

// 如果两者都没有，说明没有有效事件，直接退出
if (faceEvent == null && rfidEvent == null) {
    return null
}

// --- 3. 安全的数据提取与情景判定 (核心修复区) ---

// 3.1 提取人员姓名（安全判断：如果没拍到脸，就是异常抛掷或尾随）
def personName = "【异常未识别/无人】"
if (faceEvent != null && faceEvent.data?.personId) {
    personName = faceEvent.data.personId
}

// 3.2 提取物品标签与进出方向（安全判断：结合我们在第一层做好的数据库信息）
def tagsDetail = []
def antennaId = 0
if (rfidEvent != null) {
    tagsDetail = rfidEvent.data?.tagsDetail ?: []
    antennaId = rfidEvent.data?.ant ?: 0
}

// 3.3 判定通行场景分类 (非常关键，方便第三层写规则)
def sceneType = ""
if (faceEvent != null && rfidEvent != null) {
    sceneType = "👤📦 人物协同通行"
} else if (faceEvent != null && rfidEvent == null) {
    sceneType = "👤 人员空手通行"
} else if (faceEvent == null && rfidEvent != null) {
    sceneType = "⚠️📦 严重异常：仅载体移动 (抛掷/尾随/漏抓拍)"
}

// 如果既没有有效人脸，也没有读到任何有效载体（例如人脸机误触，或非涉密人员闲逛），可以选择忽略
if (personName == "【异常未识别/无人】" && tagsDetail.isEmpty()) {
    return null
}

// 3.4 格式化展示物品（有物品展示物品，没物品显示空手）
def tagsShow = "无载体 (空手)"
if (!tagsDetail.isEmpty()) {
    // 假设你在第一层查库时塞入了 secretLevel, name, owner 等字段
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

// 3.6 确定发生位置与时间（安全合并时间）
def location = (faceEvent?.location) ?: (rfidEvent?.location ?: "一号仓库大门")

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
🚨 【一号仓库 (SPACE_WH_1F) 事件捕获】 🚨
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
        "report": result,          // 留给原来打印控制台用
        "actionData": [            // 传给第三层逻辑判断用
                                   "spaceId": "SPACE_WH_01",
                                   "personName": personName,
                                   "direction": directionCode,
                                   "tags": tagsDetail,
                                   "sceneType": sceneType  // 🔥 将场景类型也传给第三层
        ]
]