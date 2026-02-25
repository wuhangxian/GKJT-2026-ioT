// ===================================================================
// 第三层：全局业务报警与流转规则 (面向非技术管理人员)
// 可用变量: action (包含 spaceId, personName, direction, tags 列表)
// 可用工具: alert.send(), timer.startTracking(), timer.stopTracking()
// ===================================================================

// 1. 获取基础信息
def person = action.personName
def space = action.spaceId
def dir = action.direction
def tags = action.tags

// --- 规则一：人证不符告警模拟 ---
// 这里模拟查数据库判断归属人（实际可由 Java 层预先查好塞入 action）
for (tag in tags) {
    // 假设 200号载体 属于张三，但是现在拿到的是别人
    if (tag.contains("200") && person != "张三") {
        alert.send("严重违规", "人员【${person}】试图携带属于他人的载体【${tag}】通过【${space}】！")
    }
}

// --- 规则二：A园区 到 B园区 的跨区倒计时 ---
// 场景：从一号仓库(SPACE_WH_01)出门，开启倒计时，必须在 30 秒内到达 二号仓库(SPACE_WH_02)
if (space == "SPACE_WH_01" && dir == "OUT") {
    for (tag in tags) {
        // 开启计时，限制 30 秒
        timer.startTracking(tag, person, "SPACE_WH_01", "SPACE_WH_02", 30)
    }
}

// --- 规则三：安全到达目标区域，解除倒计时 ---
// 场景：进入二号仓库(SPACE_WH_02)，取消追踪
if (space == "SPACE_WH_02" && dir == "IN") {
    for (tag in tags) {
        timer.stopTracking(tag, "SPACE_WH_02")
    }
}