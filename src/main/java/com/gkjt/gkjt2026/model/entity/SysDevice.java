package com.gkjt.gkjt2026.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_device") // 必须对应数据库里的表名
public class SysDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 对应 sn 列
    @Column(unique = true, nullable = false)
    private String sn;

    private String name;
    private String type;

    // --- 对应数据库里的聚合字段 ---
    private String location; // 物理位置
    private String spaceId;  // 空间ID
    private String deptId;   // 归属部门
    private String manager;  // 责任人

    private Integer status;  // 状态
}