package com.Auton.GIBGMain.Response.adminDTO;

import com.Auton.GIBGMain.entity.admin_entity;
import com.Auton.GIBGMain.entity.vehicle_entity.vehicle_entity;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class adminRequest {
    private admin_entity admin;
    private List<vehicle_entity> vehicle;
}
