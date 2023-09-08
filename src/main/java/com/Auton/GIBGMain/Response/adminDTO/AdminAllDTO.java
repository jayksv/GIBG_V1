package com.Auton.GIBGMain.Response.adminDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminAllDTO {
    private String user_id;
    private String username;
    private String email;
//    private String password;
    private String surname;
    private String phone;
//    private String secret_password;
//    private String role_name;
    private Date created_at;
    private Long vehicle_id;
    private String is_active;
}
