package com.gkjt.gkjt2026.repository;

import com.gkjt.gkjt2026.model.entity.SysDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 设备仓库管理员
 * 作用：直接操作数据库，负责 CRUD (增删改查)
 */
@Repository
public interface DeviceRepository extends JpaRepository<SysDevice, Long> {

    /**
     * 魔法方法：根据 SN 查找设备
     * Spring Data JPA 会自动把它翻译成 SQL:
     * SELECT * FROM sys_device WHERE sn = ?
     */
    SysDevice findBySn(String sn);

}