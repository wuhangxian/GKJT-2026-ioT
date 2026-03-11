def 拿东西的人 = action.personName
def 物品清单 = action.tags
def 运行方向 = action.direction

def 允许放行 = true

for (物品 in 物品清单) {
    if (运行方向 == "OUT" && 物品.secretLevel == "绝密") {
        alert.send("特别提醒", "绝密载体【${物品.name}】正在离开一号大门！")

        // 🔥 核心魔法：放行，但同时按下了倒计时秒表！(测试用 10 秒)
        tracker.expect(物品.tagId, "SPACE_SEC_A", 10, "严重违规！绝密文件【${物品.name}】未在规定时间内抵达保密室！可能已流失！")
    }
}

if (允许放行 == false) {
    door.block("一楼大门安全校验未通过！")
} else {
    door.open("一楼仓库常规校验通过。")
}