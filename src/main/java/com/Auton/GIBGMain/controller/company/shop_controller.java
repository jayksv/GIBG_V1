package com.Auton.GIBGMain.controller.company;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.Response.company.shopAllDTO;
import com.Auton.GIBGMain.entity.shop.shop_entity;
import com.Auton.GIBGMain.entity.shop.shop_image_entity;
import com.Auton.GIBGMain.entity.shop.shop_service_entity;
import com.Auton.GIBGMain.entity.wrapper.ShopWrapper;
import com.Auton.GIBGMain.middleware.authToken;
import com.Auton.GIBGMain.repository.company.shop_amenitie_repository;
import com.Auton.GIBGMain.repository.company.shop_image_repository;
import com.Auton.GIBGMain.repository.company.shop_repository;
import com.Auton.GIBGMain.repository.company.shop_service_repository;
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

    public shop_controller(JdbcTemplate jdbcTemplate,  authToken authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
    }
    @GetMapping("/shop/all")
    public ResponseEntity<ResponseWrapper<List<shopAllDTO>>> getAllRoles(@RequestHeader("Authorization") String authorizationHeader) {
        try {


            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<List<shopAllDTO>> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            String sql = "SELECT * FROM `shop_details_view`";
            List<shopAllDTO> roles = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                shopAllDTO shop = new shopAllDTO();
                shop.setShop_id(resultSet.getLong("shop_id"));
                shop.setShop_name(resultSet.getString("shop_name"));
                shop.setStreet_address(resultSet.getString("street_address"));
                shop.setCity(resultSet.getString("city"));
                shop.setState(resultSet.getString("state"));
                shop.setPostal_code(resultSet.getString("postal_code"));
                shop.setCountry(resultSet.getString("country"));
                shop.setLatitude(resultSet.getBigDecimal("latitude"));
                shop.setLongitude(resultSet.getBigDecimal("longitude"));
                shop.setStatus_name(resultSet.getString("status_name"));
                shop.setType_name(resultSet.getString("type_name"));
//                shop.setShop_image(resultSet.getString("shop_image"));
//                shop.setMonday_open(resultSet.getTime("monday_open"));
//                shop.setMonday_close(resultSet.getTime("monday_close"));
//                shop.setTuesday_open(resultSet.getTime("tuesday_open"));
//                shop.setTuesday_close(resultSet.getTime("tuesday_close"));
//                shop.setWednesday_open(resultSet.getTime("wednesday_open"));
//                shop.setWednesday_close(resultSet.getTime("wednesday_close"));
//                shop.setThursday_open(resultSet.getTime("thursday_open"));
//                shop.setThursday_close(resultSet.getTime("thursday_close"));
//                shop.setFriday_open(resultSet.getTime("friday_open"));
//                shop.setFriday_close(resultSet.getTime("friday_close"));
//                shop.setSaturday_open(resultSet.getTime("saturday_open"));
//                shop.setSaturday_close(resultSet.getTime("saturday_close"));
//                shop.setSunday_open(resultSet.getTime("sunday_open"));
//                shop.setSunday_close(resultSet.getTime("sunday_close"));
//                shop.setShop_owner(resultSet.getString("owner_name"));
//                shop.setShop_type_id(resultSet.getLong("shop_type_id"));
//                shop.setShop_status_id(resultSet.getLong("shop_status_id"));
                return shop;
            });

            ResponseWrapper<List<shopAllDTO>> responseWrapper = new ResponseWrapper<>("shops retrieved successfully.", roles);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            String errorMessage = "An error occurred while retrieving roles.";
            ResponseWrapper<List<shopAllDTO>> errorResponse = new ResponseWrapper<>(errorMessage, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
}
