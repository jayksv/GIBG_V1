package com.Auton.GIBGMain.Response.adminDTO;

import com.Auton.GIBGMain.Response.VehicleDTO;
import com.Auton.GIBGMain.entity.user_type.userType_entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class userVecleDTO {
    private String message;
    private AdminAllDTO username;
    private List<VehicleDTO> usertype;

}
