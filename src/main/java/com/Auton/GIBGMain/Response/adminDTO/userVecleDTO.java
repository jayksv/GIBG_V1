package com.Auton.GIBGMain.Response.adminDTO;

import com.Auton.GIBGMain.Response.userVecleDTO.VehicleDTO;
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
