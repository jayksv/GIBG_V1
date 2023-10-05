package com.Auton.GIBGMain.repository;

import com.Auton.GIBGMain.entity.admin_entity;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
public interface admin_repository extends JpaRepository<admin_entity, Long> {
    @Query("SELECT a FROM admin_entity a WHERE a.user_id = :user_id")
    admin_entity findByUserId(@Param("user_id") String user_id);
    admin_entity findByUsername(String username);
    admin_entity findByPhone(String phone);
}
