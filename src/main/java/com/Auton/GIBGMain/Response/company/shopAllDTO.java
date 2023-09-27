package com.Auton.GIBGMain.Response.company;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class shopAllDTO {
    private Long shop_id;
    private String shop_name;
    private String street_address;
    private String city;
    private String state;
    private String postal_code;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String  type_name;
    private String shop_image;
    private String shop_phone;
    private String shop_mail;
    private String shop_website;

    private Time monday_open;
    private Time monday_close;
    private Time tuesday_open;
    private Time tuesday_close;
    private Time wednesday_open;
    private Time wednesday_close;
    private Time thursday_open;
    private Time thursday_close;
    private Time friday_open;
    private Time friday_close;
    private Time saturday_open;
    private Time saturday_close;
    private Time sunday_open;
    private Time sunday_close;

}
