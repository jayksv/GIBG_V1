package com.Auton.GIBGMain.Response.company;

import com.Auton.GIBGMain.entity.shop.shop_image_entity;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class shopOwner_DTO {
    private String message;

    private shopAllDTO shopInfo;

    private List<shopAmenitiesDTO> shopAmenrities;
    private List<shop_image_entity> shopImages;
    private List<shopServiceDTO> shopServices;
    private List<shopTypeDTO> shopType;
}
