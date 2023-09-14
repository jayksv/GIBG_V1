package com.Auton.GIBGMain.Response.userVecleDTO;

import com.Auton.GIBGMain.entity.admin_entity;
import com.Auton.GIBGMain.entity.user_type.userType_entity;
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
public class vehicle_request {

        private admin_entity users;
        private List<userType_entity> updatedVehicle;
}
