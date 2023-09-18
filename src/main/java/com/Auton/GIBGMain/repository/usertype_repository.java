package com.Auton.GIBGMain.repository;

import com.Auton.GIBGMain.entity.user_type.userType_entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface usertype_repository extends JpaRepository<userType_entity,Long> {


}
