package com.Auton.GIBGMain.repository;

import com.Auton.GIBGMain.entity.user_type.userType_entity;
import com.Auton.GIBGMain.entity.vehicle_entity.vehicle_entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface vehicle_repository extends JpaRepository<vehicle_entity,Long> {
    vehicle_entity findByLicensePlate(String licensePlate);
}
