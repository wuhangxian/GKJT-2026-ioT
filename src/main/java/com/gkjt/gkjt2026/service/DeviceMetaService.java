package com.gkjt.gkjt2026.service;

import com.gkjt.gkjt2026.model.entity.SysDevice;
import com.gkjt.gkjt2026.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 业务层 (Service)：这里是写逻辑的地方
 * 作用：接收 Controller 的指令，指挥 Repository 去干活，并对数据进行加工
 */
@Service // 1. 【关键】贴上这个标签，Spring 才知道这是个“大厨”，系统启动时会自动聘用它
public class DeviceMetaService {

    // 2. 注入仓库管理员
    // 我们不需要自己 new DeviceRepository()，加了 @Autowired，Spring 会自动把之前写好的仓库塞进来
    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * 核心功能：根据 SN 号查设备详情
     * @param sn 设备序列号
     * @return 加工好的设备信息
     */
    public DeviceInfo getInfoBySn(String sn) {
        System.out.println(">>> Service层开始工作：正在查询 SN = " + sn);

        // 3. 指挥仓库管理员去数据库查
        // 这一行代码执行完，device 变量里就有了数据库里那一整行数据
        SysDevice device = deviceRepository.findBySn(sn);

        // 4. 逻辑判断：如果查不到，或者查到的设备是报废的(status=0)
        if (device == null) {
            System.out.println(">>> 查无此设备！");
            return null;
        }
        if (device.getStatus() != null && device.getStatus() == 0) {
            System.out.println(">>> 设备已报废/停用，拒绝处理！");
            return null;
        }

        // 5. 数据加工 (提取我们需要的字段)
        // 数据库里可能有很多杂乱的字段(比如id, create_time)，我们只挑出我们需要的这4个
        return new DeviceInfo(
                device.getLocation(),
                device.getSpaceId(),
                device.getDeptId(),
                device.getManager()
        );
    }

    // 6. 定义一个小盒子 (Java Record)
    // 专门用来把上面提取出来的4个数据打包，方便传给 Controller
    public static record DeviceInfo(
            String location,
            String spaceId,
            String deptId,
            String manager
    ) {}
}