package com.Auton.GIBGMain.Response.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {
    private Long service_id;
    private String service_name;
    private String description;
    private String service_icon;
    private Long serviceType_id;
    private Timestamp created_at;
    private String type_name;
    private String first_name;
}
