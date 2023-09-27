package com.Auton.GIBGMain.entity.shop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shop_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class shopType_entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_type_id ")
    private Long shop_type_id ;

    @Column(name = "shop_id")
    private Long shop_id;

    @Column(name = "type_id")
    private Long type_id;

    @Column(name = "create_at")
    private String create_at;

}
