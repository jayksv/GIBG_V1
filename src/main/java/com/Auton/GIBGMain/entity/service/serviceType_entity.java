package com.Auton.GIBGMain.entity.service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_service_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class serviceType_entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "serviceType_id")
    private Long serviceType_id;

    @Column(name = "type_name", nullable = false)
    private String type_name;

    @Column(name = "serviceType_icon")
    private String serviceType_icon;

    @Column(name = "parent_type_id")
    private Long parent_type_id;
}
