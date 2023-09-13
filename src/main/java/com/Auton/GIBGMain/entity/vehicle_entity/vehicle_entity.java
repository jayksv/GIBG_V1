package com.Auton.GIBGMain.entity.vehicle_entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
    @Table(name = "tb_vehicles")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class vehicle_entity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "license_plate  ")
        private String licensePlate;

        @Column(name = "Vehicle_type")
        private String Vehicle_type;

        @Column(name = "Manufacturer")
        private String Manufacturer;

        @Column(name = "Vehicle_type_name")
        private String Vehicle_type_name;

        @Column(name = "vehicle_year")
        private String vehicle_year;

        @Column(name = "fuel")
        private String fuel;

        @Column(name = "exhast_cc")
        private String exhast_cc;

        @Column(name = "color_vehicle")
        private String color_vehicle;

        @Column(name = "first_registration")
        private String first_registration;

        @Column(name = "vehicle_stand_number")
        private String vehicle_stand_number;

        @Column(name = "date_of_vehicle")
        private String date_of_vehicle;

        @Column(name = "Classification_of_vehicle_type")
        private String Classification_of_vehicle_type;

        @Column(name = "Final_inspection_date")
        private String Final_inspection_date;


}
