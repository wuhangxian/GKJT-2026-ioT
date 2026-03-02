package com.gkjt.gkjt2026.service;

import com.gkjt.gkjt2026.model.entity.SysCarrier;
import com.gkjt.gkjt2026.repository.CarrierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CarrierMetaService {

    @Autowired
    private CarrierRepository carrierRepository;

    /**
     * 根据 RFID 标签号查询载体详细信息
     */
    public CarrierInfo getInfoByTagId(String tagId) {
        SysCarrier carrier = carrierRepository.findByTagId(tagId);

        if (carrier == null) {
            return null; // 查不到说明是未注册的黑户标签
        }

        // 打包核心信息返回
        return new CarrierInfo(
                carrier.getTagId(),
                carrier.getName(),
                carrier.getOwner(),
                carrier.getSecretLevel(),
                carrier.getDeptName()
        );
    }

    // 使用 Java Record 定义一个简洁的数据载体
    public static record CarrierInfo(
            String tagId,
            String name,
            String owner,
            String secretLevel,
            String deptName
    ) {}
}