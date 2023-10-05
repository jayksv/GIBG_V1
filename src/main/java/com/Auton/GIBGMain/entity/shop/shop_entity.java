package com.Auton.GIBGMain.entity.shop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;

@Entity
@Table(name = "tb_shop")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class shop_entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "monday_open")
    private Time mondayOpen;

    @Column(name = "monday_close")
    private Time mondayClose;

    @Column(name = "tuesday_open")
    private Time tuesday_open;

    @Column(name = "tuesday_close")
    private Time tuesday_close;

    @Column(name = "wednesday_open")
    private Time wednesday_open;

    @Column(name = "wednesday_close")
    private Time wednesday_close;

    @Column(name = "thursday_open")
    private Time thursday_open;

    @Column(name = "thursday_close")
    private Time thursday_close;

    @Column(name = "friday_open")
    private Time friday_open;

    @Column(name = "friday_close")
    private Time friday_close;

    @Column(name = "saturday_open")
    private Time saturday_open;

    @Column(name = "saturday_close")
    private Time saturday_close;

    @Column(name = "sunday_open")
    private Time sunday_open;

    @Column(name = "sunday_close")
    private Time sunday_close;



    @Column(name = "shop_image")
    private String shop_image;

    @Column(name = "shop_type_id")
    private Long shop_type_id;

    @Column(name = "shop_status_id")
    private Long shop_status_id;

    @Column(name = "shop_phone")
    private String shop_phone;

    @Column(name = "shop_mail")
    private String shop_mail;

    @Column(name = "shop_website")
    private String shop_website;


}
