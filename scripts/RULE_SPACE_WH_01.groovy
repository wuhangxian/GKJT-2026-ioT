// 文件名: scripts/RULE_SPACE_WH_01.groovy (一号大门专属规则)

// 1. 统一全局变量名，全部去掉 def！
拿东西的人 = action.personName
物品清单 = action.tags
运行方向 = action.direction

允许放行 = true

for (物品 in 物品清单) {
    // 规则 1：不属于自己的东西绝对不能带走
    // 🔥 修复：把“通行人员”全部改回“拿东西的人”
    if (物品.owner != 拿东西的人) {
        alert.send("严重违规", "【${拿东西的人}】试图带走属于【${物品.owner}】的物品：《${物品.name}》！")
        允许放行 = false
    }

    // 规则 2：绝密文件出门，启动定时追踪炸弹
    if (运行方向 == "OUT" && 物品.secretLevel == "绝密") {
        alert.send("特别提醒", "绝密载体【${物品.name}】正在离开一号大门！")

        // 🔥 核心魔法：放行，但同时按下了倒计时秒表！(测试用 10 秒)
        tracker.expect(物品.tagId, "SPACE_SEC_A", 10, "严重违规！绝密文件【${物品.name}】未在规定时间内抵达保密室！可能已流失！")
    }
}

// --- 最终闸机控制 ---
if (允许放行 == false) {
    door.block("一楼大门安全校验未通过！")
} else {
    door.open("一楼仓库常规校验通过。")
}