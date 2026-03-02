package com.gkjt.gkjt2026.service;

import com.gkjt.gkjt2026.adapter.AdapterFactory;
import com.gkjt.gkjt2026.adapter.DeviceAdapter;
import com.gkjt.gkjt2026.model.SensorEvent;
import com.gkjt.gkjt2026.model.dto.GatewayMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 🏭 传感器数据核心加工厂
 * 作用：专门负责把“网关原始数据”变成“系统标准事件”
 * 就像是一个黑盒子：一边进原料(Msg)，一边出成品(Event)
 */
@Service
public class SensorDataService {

    @Autowired
    private AdapterFactory adapterFactory;

    @Autowired
    private DeviceMetaService deviceMetaService;

    // 👇新增：注入物品查询服务
    @Autowired
    private CarrierMetaService carrierMetaService;

    // 如果你之前写了 TagMetaService (查物品的)，也要注入进来
    // @Autowired
    // private TagMetaService tagMetaService;

    /**
     * 核心算子：一键转换
     * @param msg 网关发来的原始消息
     * @return 转换并填充好的标准事件 (如果不支持或出错，返回 null)
     */
    public SensorEvent process(GatewayMessage msg) {
        // 1. 找适配器 (翻译官)
        DeviceAdapter adapter = adapterFactory.getAdapter(msg.getType());
        if (adapter == null) {
            System.out.println(">>> [加工厂] 拒单：未知设备类型 " + msg.getType());
            return null;
        }

        // 2. 基础转换 (由适配器完成)
        SensorEvent event = adapter.convert(msg);
        if (event == null) return null;

        // 3. 查户口 (去数据库补全 位置、部门、负责人)
        DeviceMetaService.DeviceInfo info = deviceMetaService.getInfoBySn(msg.getSn());

        if (info != null) {
            // 查到了，把信息填进去
            event.setLocation(info.location());
            event.setSpaceId(info.spaceId());
            event.setDeptId(info.deptId());
            event.setManager(info.manager());
            System.out.println(">>> [加工厂] 数据增强成功：关联到 " + info.location());
        } else {
            // 没查到，填默认值
            event.setLocation("未知区域");
            event.setManager("待定");
            System.out.println(">>> [加工厂] 警告：数据库无此设备 SN=" + msg.getSn());
        }

        // 4. 如果是 RFID，去数据库查一下标签物品到底是什么、主人是谁
        if ("RFID_GATE".equals(event.getDeviceType())) {
            Object rawTags = event.getData().get("tags");
            if (rawTags instanceof java.util.List) {
                java.util.List<String> tagIds = (java.util.List<String>) rawTags;
                java.util.List<java.util.Map<String, String>> tagsWithInfo = new java.util.ArrayList<>();

                for (String tagId : tagIds) {
                    java.util.Map<String, String> tagInfo = new java.util.HashMap<>();
                    tagInfo.put("tagId", tagId);

                    // 👇 调用真实数据库查询
                    CarrierMetaService.CarrierInfo cInfo = carrierMetaService.getInfoByTagId(tagId);

                    if (cInfo != null) {
                        tagInfo.put("owner", cInfo.owner());
                        tagInfo.put("name", cInfo.name());
                        tagInfo.put("secretLevel", cInfo.secretLevel());
                        tagInfo.put("dept", cInfo.deptName());
                    } else {
                        // 数据库里没这个标签的兜底逻辑
                        tagInfo.put("owner", "未知");
                        tagInfo.put("name", "未注册载体(" + tagId + ")");
                        tagInfo.put("secretLevel", "未知");
                        tagInfo.put("dept", "未知");
                    }
                    tagsWithInfo.add(tagInfo);
                }
                // 把丰富后的对象列表塞回 data 里
                event.getData().put("tagsDetail", tagsWithInfo);
            }
        }

        return event;

    }
}