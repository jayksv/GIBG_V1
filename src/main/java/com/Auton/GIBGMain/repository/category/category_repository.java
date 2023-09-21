package com.Auton.GIBGMain.repository.category;

import com.Auton.GIBGMain.entity.category.category_entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface category_repository extends JpaRepository<category_entity, Long> {

}
