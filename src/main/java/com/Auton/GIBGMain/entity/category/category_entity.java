package com.Auton.GIBGMain.entity.category;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class category_entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long category_id;
    private String category_name;
    private String description;
    private Long parent_category_id;

}
