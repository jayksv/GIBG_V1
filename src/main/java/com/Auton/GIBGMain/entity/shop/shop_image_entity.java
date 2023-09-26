package com.Auton.GIBGMain.entity.shop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "shop_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class shop_image_entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_image_id")
    private Long shop_image_id;

    @Column(name = "shop_id")
    private Long shop_id;

    @Column(name = "image_path")
    private String image_path;

    @Column(name = "create_at")
    private Date create_at;
}
