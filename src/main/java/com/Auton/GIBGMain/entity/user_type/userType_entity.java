package com.Auton.GIBGMain.entity.user_type;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_user_vicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class userType_entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_vicle_id ")
    private Long user_vicle_id ;

    @Column(name = "user_id")
    private String user_id;

    @Column(name = "vehicle_id")
    private Long vehicle_id;
}
