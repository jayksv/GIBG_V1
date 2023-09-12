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
    private String first_name;
    private String last_name;
    private String datebirth;
    private String phone;
    private String secret_password;
    private String image_profile;
    private Long create_by;
    private Date created_at;
    private String is_active;
    private String gender;
    private String address;
    private String shop_id;
//    private Long user_vicle_id ;
}
