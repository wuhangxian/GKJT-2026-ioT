// 文件名: scripts/RULE_SPACE_SEC_A.groovy (保密室专属规则)

// 1. 统一全局变量名，全部去掉 def！
拿东西的人 = action.personName
运行方向 = action.direction
物品清单 = action.tags

允许放行 = true

// --- 动作 A：往里送东西 (解除警报) ---
if (运行方向 == "IN" && 物品清单.size() > 0) {
    for (物品 in 物品清单) {
        // 🔥 核心魔法：告诉追踪器，物品安全到达了！解除警报！
        tracker.arrive(物品.tagId, "SPACE_SEC_A")
    }
    door.open("物品入库保密室成功")
}

// --- 动作 B：往外带东西 (OUT)，严苛的防盗锁门逻辑 ---
if (运行方向 == "OUT" && 物品清单.size() > 0) {
    // 特权判断：除非是保密员王芳在操作，否则谁都不行
    if (拿东西的人 != "保密员-王芳") {
        alert.send("最高级别入侵警报", "【${拿东西的人}】正试图从二楼核心保密室带出物理载体！")
        允许放行 = false
    } else {
        alert.send("保密室通知", "保密员王芳正在执行物资调拨。")
    }
}

// --- 最终闸机控制 ---
if (允许放行 == false) {
    door.block("保密室防盗门已死锁！并联动安保中心！")
} else if (运行方向 == "OUT") {
    door.open("保密室权限校验通过。")
}