package com.Auton.GIBGMain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

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

    @Column(name = "email")
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "surname")
    private String surname;

    @Column(name = "phone")
    private String phone;

    @Column(name = "secret_password")
    private String secret_password;

    @Column(name = "role_id", columnDefinition = "bigint default 4")
    private Long role_id;

    @Column(name = "vehicle_id")
    private Long vehicle_id;

    @Column(name = "is_active",columnDefinition = "bigint default Active")
    private String is_active;


}
