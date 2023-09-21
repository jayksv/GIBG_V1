package com.Auton.GIBGMain.entity.service;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class service_entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long service_id;

    @Column(name = "service_name")
    private String service_name;

    @Column(name = "service_icon")
    private String service_icon;

    @Column(name = "description")
    private String description;

    @Column(name = "createBy")
    private String createBy;

    @Column(name = "serviceType_id")
    private Long serviceType_id;
}
