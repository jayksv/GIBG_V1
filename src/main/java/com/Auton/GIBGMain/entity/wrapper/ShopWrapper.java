package com.Auton.GIBGMain.entity.wrapper;

import com.Auton.GIBGMain.entity.shop.*;
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
public class ShopWrapper {
    private shop_entity userShop;
    private List<shop_amenitie_entity> shop_amenities;
    private List<shop_service_entity> shop_service;
    private List<shop_image_entity> shop_images;
    private List<shopType_entity> shop_type;
}
