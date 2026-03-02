package com.gkjt.gkjt2026.repository;

import com.gkjt.gkjt2026.model.entity.SysCarrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 载体仓库管理员：负责对 sys_carrier 表进行增删改查
 */
@Repository
public interface CarrierRepository extends JpaRepository<SysCarrier, Long> {

    // Spring Data JPA 魔法方法：根据标签号查找物品
    SysCarrier findByTagId(String tagId);
}