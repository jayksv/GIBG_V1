package com.Auton.GIBGMain.controller.company;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.Response.company.*;
import com.Auton.GIBGMain.entity.shop.shop_entity;
import com.Auton.GIBGMain.entity.shop.shop_image_entity;
import com.Auton.GIBGMain.entity.shop.shop_service_entity;
import com.Auton.GIBGMain.entity.wrapper.ShopWrapper;
import com.Auton.GIBGMain.middleware.authToken;
import com.Auton.GIBGMain.repository.company.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.Auton.GIBGMain.entity.shop.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class shop_controller {
    @Value("${jwt_secret}")
    private String jwt_secret;
    private final JdbcTemplate jdbcTemplate;
    private final authToken authService;

    @Autowired
    private shop_repository shopRepository;
    @Autowired
    private shop_amenitie_repository amenityRepository;
    @Autowired
    private shop_image_repository imageRepository;
    @Autowired
    private shop_service_repository serviceRepository;
    @Autowired
    private shopType_Repository shopTypeRepository;

    public shop_controller(JdbcTemplate jdbcTemplate,  authToken authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
    }
    @GetMapping("/shop/all")
    public ResponseEntity<?> getShopOwnerOnly(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<Void> authResponseBody = authResponse.getBody();
                return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
            }


            String sql = "SELECT * FROM `shop_view` ";
            List<shopAllDTO> shopInfo = jdbcTemplate.query(sql, this::mapShopAll);

            List<shopOwner_DTO> responses = new ArrayList<>();

            for (shopAllDTO usershopInfo : shopInfo) {

                List<shopAmenitiesDTO> amenity = findshopAmenrities(usershopInfo.getShop_id());
                List<shop_image_entity> image = findImage(usershopInfo.getShop_id());
                List<shopServiceDTO> service = findService(usershopInfo.getShop_id());
                List<shopTypeDTO> types = findShopType(usershopInfo.getShop_id());


                shopOwner_DTO res =new shopOwner_DTO("Success", usershopInfo, amenity, image, service, types);

                responses.add(res);
            }

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PostMapping("/shop/add")
    public ResponseEntity<ResponseWrapper<List<shop_entity>>> addNewShop(
            @RequestBody ShopWrapper request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                ResponseWrapper<List<shop_entity>> responseWrapper = new ResponseWrapper<>("Authorization header is missing or empty.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }
            // Verify the token from the Authorization header
            String token = authorizationHeader.substring("Bearer ".length());

            Claims claims = Jwts.parser()
                    .setSigningKey(jwt_secret) // Replace with your secret key
                    .parseClaimsJws(token)
                    .getBody();

            // Check token expiration
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                ResponseWrapper<List<shop_entity>> responseWrapper = new ResponseWrapper<>("Token has expired.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Extract necessary claims (you can add more as needed)
            String authenticatedUserId = claims.get("user_id", String.class);
            Long roleId = claims.get("role_id", Long.class);

//            String role = claims.get("role_name", String.class);
            // Check if the authenticated user has the appropriate role to perform this action (e.g., admin)
            if (roleId !=1  && roleId !=2 ) {
                ResponseWrapper<List<shop_entity>> responseWrapper = new ResponseWrapper<>("You are not authorized to perform this action.", null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseWrapper);
            }

            shop_entity userShop = request.getUserShop();
            List<shop_amenitie_entity> shopAmenrities = request.getShop_amenities();
            List<shop_service_entity> shopServices = request.getShop_service();
            List<shop_image_entity> shopImages = request.getShop_images();
            List<shopType_entity> shopTypes = request.getShop_type();


            // Validate shop information or any other necessary validations

            // Save the shop and related entities
//            userShop.setShopStatusId(1L); // You may want to use constants instead of hardcoding IDs
            shop_entity savedShop = shopRepository.save(userShop);


            if (shopAmenrities != null) {
                for (shop_amenitie_entity amenity : shopAmenrities) {
                    amenity.setShop_id(savedShop.getShopId());
                    amenityRepository.save(amenity);
                }
            }
            if (shopServices != null) {
                for (shop_service_entity service : shopServices) {
                    service.setShop_id(savedShop.getShopId()); // Set the shop entity directly
                    serviceRepository.save(service);
                }
            }

            if (shopImages != null) {
                for (shop_image_entity shopImage : shopImages) {
                    shopImage.setShop_id(savedShop.getShopId()); // Set the shop entity directly
                    imageRepository.save(shopImage);
                }
            }
            if (shopTypes != null) {
                for (shopType_entity shopType : shopTypes) {
                    shopType.setShop_id(savedShop.getShopId()); // Set the shop entity directly
                    shopTypeRepository.save(shopType);
                }
            }

//            savedShop.setIsActive("Active");
//            savedShop.setRoleId(2L); // You may want to use constants instead of hardcoding role IDs

//            shopRepository.save(savedShop);

            return ResponseEntity.ok(
                    new ResponseWrapper<>("Shop added successfully.", null)
            );

        } catch (JwtException e) {
            // Token is invalid or has expired
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new ResponseWrapper<>("Token is invalid.", null)
            );
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "An error occurred while adding a new shop.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseWrapper<>(errorMessage, null)
            );
        }
    }
    private shopAllDTO mapShopAll(ResultSet rs, int rowNum) throws SQLException {
        shopAllDTO shopInfo = new shopAllDTO();
        shopInfo.setShop_id(rs.getLong("shop_id"));
        shopInfo.setShop_name(rs.getString("shop_name"));
        shopInfo.setStreet_address(rs.getString("street_address"));
        shopInfo.setCity(rs.getString("city"));
        shopInfo.setState(rs.getString("state"));
        shopInfo.setPostal_code(rs.getString("postal_code"));
        shopInfo.setCountry(rs.getString("country"));
        shopInfo.setLatitude(rs.getBigDecimal("latitude"));
        shopInfo.setLongitude(rs.getBigDecimal("longitude"));
//        shopInfo.setShop_type_name(rs.getString("shop_type_name"));
        shopInfo.setShop_image(rs.getString("shop_image"));
        shopInfo.setShop_phone(rs.getString("shop_phone"));
        shopInfo.setShop_mail(rs.getString("shop_mail"));
        shopInfo.setShop_website(rs.getString("shop_website"));
//
        shopInfo.setMonday_open(rs.getTime("monday_open"));
        shopInfo.setMonday_close(rs.getTime("monday_close"));
        shopInfo.setTuesday_open(rs.getTime("tuesday_open"));
        shopInfo.setTuesday_close(rs.getTime("tuesday_close"));
        shopInfo.setWednesday_open(rs.getTime("wednesday_open"));
        shopInfo.setWednesday_close(rs.getTime("wednesday_close"));
        shopInfo.setThursday_open(rs.getTime("thursday_open"));
        shopInfo.setThursday_close(rs.getTime("thursday_close"));
        shopInfo.setFriday_open(rs.getTime("friday_open"));
        shopInfo.setFriday_close(rs.getTime("friday_close"));
        shopInfo.setSaturday_open(rs.getTime("saturday_open"));
        shopInfo.setSaturday_close(rs.getTime("saturday_close"));
        shopInfo.setSunday_open(rs.getTime("sunday_open"));
        shopInfo.setSunday_close(rs.getTime("sunday_close"));


        return shopInfo;

    }
    private  List<shopAmenitiesDTO> findshopAmenrities(Long shopId){
        String SQL = "SELECT shop_amenitie.amenities_id, shop_amenitie.shop_amenities_id, tb_shop.shop_name, tb_amenities.amenities_name \n" +
                "FROM shop_amenitie\n" +
                "LEFT JOIN tb_amenities ON shop_amenitie.amenities_id = tb_amenities.amenities_id\n" +
                "LEFT JOIN tb_shop ON shop_amenitie.shop_id = tb_shop.shop_id \n" +
                "WHERE shop_amenitie.shop_id =? ";
        return  jdbcTemplate.query(SQL,this::mappshopAmenrities,shopId);
    }
    private shopAmenitiesDTO mappshopAmenrities(ResultSet rs, int rowNum) throws SQLException {
        shopAmenitiesDTO shopAmenritiesEntity = new shopAmenitiesDTO();
        shopAmenritiesEntity.setAmenities_id(rs.getLong("amenities_id"));
        shopAmenritiesEntity.setAmenities_name(rs.getString("amenities_name"));

        return shopAmenritiesEntity;
    }
    private  List<shop_image_entity> findImage(Long shopId){
        String SQL = "SELECT * FROM `shop_image` WHERE `shop_id` =? ";
        return  jdbcTemplate.query(SQL,this::mappshopImage,shopId);
    }
    private shop_image_entity mappshopImage(ResultSet rs, int rowNum) throws SQLException {
        shop_image_entity shopImage = new shop_image_entity();
        shopImage.setShop_image_id(rs.getLong("shop_image_id"));
        shopImage.setShop_id(rs.getLong("shop_id"));
        shopImage.setImage_path(rs.getString("image_path"));

        return shopImage;
    }
    private  List<shopServiceDTO> findService(Long shopId){
        String SQL = "SELECT shop_service.service_id,shop_service.shop_service_id,tb_shop.shop_name ,tb_service.service_name FROM `shop_service`" +
                " JOIN tb_service ON shop_service.service_id = tb_service.service_id " +
                "JOIN tb_shop ON shop_service.shop_id = tb_shop.shop_id where shop_service.shop_id=?";
        return  jdbcTemplate.query(SQL,this::mappshopService,shopId);
    }
    private shopServiceDTO mappshopService(ResultSet rs, int rowNum) throws SQLException {
        shopServiceDTO shopService = new shopServiceDTO();
        shopService.setService_id(rs.getLong("service_id"));
        shopService.setService_name(rs.getString("service_name"));


        return shopService;
    }
    private  List<shopTypeDTO> findShopType(Long shopId){
        String SQL = "SELECT shop_type.type_id, shop_type.shop_type_id, tb_shop_types.type_name \n" +
                "             FROM shop_type \n" +
                "             JOIN tb_shop ON shop_type.shop_id = tb_shop.shop_id \n" +
                "             JOIN tb_shop_types ON shop_type.type_id = tb_shop_types.type_id  " +
                "WHERE shop_type.shop_id=? ";
        return  jdbcTemplate.query(SQL,this::mappshopType,shopId);
    }
    private shopTypeDTO mappshopType(ResultSet rs, int rowNum) throws SQLException {
        shopTypeDTO shopType = new shopTypeDTO();

        shopType.setType_id(rs.getLong("type_id"));
        shopType.setType_name(rs.getString("type_name"));

        return shopType;
    }
}
