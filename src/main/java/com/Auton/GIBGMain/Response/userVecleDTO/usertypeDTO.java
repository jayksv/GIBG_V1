package com.Auton.GIBGMain.Response.userVecleDTO;

import com.Auton.GIBGMain.Response.adminDTO.AdminAllDTO;
import com.Auton.GIBGMain.entity.user_type.userType_entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class usertypeDTO {
    private String message;
    private VehicleDTO vehicle;
    private List<userType_entity> usertype;
}
