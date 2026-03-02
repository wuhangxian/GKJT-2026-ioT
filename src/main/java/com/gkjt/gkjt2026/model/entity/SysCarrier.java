package com.gkjt.gkjt2026.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sys_carrier")
public class SysCarrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_id", unique = true, nullable = false)
    private String tagId;

    private String name;
    private String type;

    @Column(name = "secret_level")
    private String secretLevel;

    private String owner;

    @Column(name = "dept_name")
    private String deptName;

    private Integer status;
}