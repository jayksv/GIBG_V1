package com.Auton.GIBGMain.Response.vehicle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class vehicleAllDTO {

    private String license_plate;
    private String Vehicle_type;
    private String Manufacturer;
    private String Vehicle_type_name;
    private String vehicle_year;
    private String fuel;
    private String exhast_cc;
    private String color_vehicle;
    private String first_registration;
    private String vehicle_stand_number;
    private String date_of_vehicle;
    private String Classification_of_vehicle_type;
    private String Final_inspection_date;
}
