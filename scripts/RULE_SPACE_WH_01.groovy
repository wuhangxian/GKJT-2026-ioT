// 文件名: RULE_SPACE_WH_1F.groovy (一楼大门专属规则)
def 通行人员 = action.personName
def 携带的物品列表 = action.tags
def 允许放行 = true

for (物品 in 携带的物品列表) {
    // 仓库规则：机密级别的图纸和文件，必须本人携带！
    if (物品.secretLevel == "机密" && 物品.owner != 通行人员) {
        alert.send("越权违规", "【${通行人员}】试图带走【${物品.owner}】的机密级物品：《${物品.name}》！一楼大门拦截！")
        允许放行 = false
    }

    // 仓库规则：绝密物品根本就不该出现在一楼，直接抓人！
    if (物品.secretLevel == "绝密") {
        alert.send("严重安全事故", "在一楼大门发现绝密物品：《${物品.name}》！立刻启动园区封锁！")
        允许放行 = false
    }

    //    if (物品.name == "李四的红头文件") {
//        alert.send("警报", "检测到：《${物品.name}》！该物品严禁离开存放区！")
//        允许放行 = false
//    }
}

if (允许放行 == false) {
    door.block("一楼大门安全校验未通过！")
} else {
    door.open("一楼仓库常规校验通过。")
}