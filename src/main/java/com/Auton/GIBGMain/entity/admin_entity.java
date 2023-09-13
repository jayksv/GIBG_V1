package com.Auton.GIBGMain.entity;

import com.Auton.GIBGMain.entity.user_type.userType_entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tb_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class admin_entity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_id", columnDefinition = "VARCHAR(50)")
    private String user_id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "first_name")
    private String first_name;

    @Column(name = "last_name")
    private String last_name;

    @Column(name = "datebirth")
    private String datebirth;

    @Column(name = "image_profile")
    private String image_profile;

    @Column(name = "gender")
    private String gender;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "secret_password")
    private String secret_password;

    @Column(name = "role_id", columnDefinition = "bigint default 4")
    private Long role_id;

//    @Column(name = "license_plate")
//    private Long license_plate;

    @Column(name = "is_active",columnDefinition = "bigint default Active")
    private String is_active;

    @Column(name = "create_by")
    private String create_by;

    @Column(name = "created_at")
    private String created_at;

    @Column(name = "user_vicle_id ")
    private Long user_vicle_id ;

}
