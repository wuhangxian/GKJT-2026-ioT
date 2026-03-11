
// 文件名: scripts/RULE_SPACE_WH_01.groovy (一号大门专属规则)

拿东西的人 = action.personName
物品清单 = action.tags
运行方向 = action.direction

允许放行 = true

// 💡 核心魔法：使用 Java 的内置时间工具，获取此刻的精准时间 (比如: 10:56:23)
当前时间 = new java.text.SimpleDateFormat("HH:mm:ss").format(new Date())

for (物品 in 物品清单) {
    // 规则 1：不属于自己的东西绝对不能带走
    if (物品.owner != 拿东西的人) {
        alert.send("严重违规", "【${拿东西的人}】试图带走属于【${物品.owner}】的物品：《${物品.name}》！")
        允许放行 = false
    }

    // 规则 2：绝密文件出门，启动定时追踪炸弹
    if (运行方向 == "OUT" && 物品.secretLevel == "绝密") {
        alert.send("特别提醒", "绝密载体【${物品.name}】正在离开一号大门！")

        // 🔥 核心修改：拼装一句极其详细的“5W1H”报警话术！
        定制报警话术 = "【跨区流失警报】${拿东西的人} 在 ${当前时间} 带走的绝密文件《${物品.name}》，未在规定的 10 秒内抵达保密室！请立即调取监控核查！"

        // 把这句完美的话术，装进定时炸弹里！
        tracker.expect(物品.tagId, "SPACE_SEC_A", 10, 定制报警话术)
    }
}

// --- 最终闸机控制 ---
if (允许放行 == false) {
    door.block("一楼大门安全校验未通过！")
} else {
    door.open("一楼仓库常规校验通过。")
}