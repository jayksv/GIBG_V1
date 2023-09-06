package com.Auton.GIBGMain.repository;

import com.Auton.GIBGMain.entity.admin_entity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface admin_repository extends JpaRepository<admin_entity, String> {
//    admin_entity findByEmail(String email);
admin_entity findByUsername(String username);
}
