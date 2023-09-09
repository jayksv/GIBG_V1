package com.Auton.GIBGMain.Response.adminDTO;

import com.Auton.GIBGMain.entity.admin_entity;
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
public class userVecleResponseDTO {
    private String message;
    private admin_entity admin;
    private List<userType_entity> usertype;
}
