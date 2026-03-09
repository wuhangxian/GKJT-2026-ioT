// 文件名: RULE_SPACE_SEC_A.groovy (二楼保密室专属规则，极其严格)
def 通行人员 = action.personName
def 通行方向 = action.direction
def 携带的物品列表 = action.tags

def 允许放行 = true

// 保密室规则：只要是往外带东西 (OUT)，一律拦截报警！
if (通行方向 == "OUT" && 携带的物品列表.size() > 0) {
    // 特权判断：除非是保密员王芳在操作，否则谁都不行
    if (通行人员 != "保密员-王芳") {
        alert.send("最高级别入侵警报", "【${通行人员}】正试图从二楼核心保密室带出物理载体！")
        允许放行 = false
    } else {
        alert.send("保密室通知", "保密员王芳正在执行物资调拨。")
    }
}

if (允许放行 == false) {
    door.block("保密室防盗门已死锁！并联动安保中心！")
} else {
    door.open("保密室权限校验通过。")
}