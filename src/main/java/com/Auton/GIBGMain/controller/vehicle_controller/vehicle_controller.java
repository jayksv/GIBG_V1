package com.Auton.GIBGMain.controller.vehicle_controller;

import ch.qos.logback.core.net.SMTPAppenderBase;
import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.Response.adminDTO.adminRequest;
import com.Auton.GIBGMain.entity.admin_entity;
import com.Auton.GIBGMain.entity.user_type.userType_entity;
import com.Auton.GIBGMain.entity.vehicle_entity.vehicle_entity;
import com.Auton.GIBGMain.middleware.authToken;
import com.Auton.GIBGMain.myfuntion.IdGeneratorService;
import com.Auton.GIBGMain.repository.admin_repository;
import com.Auton.GIBGMain.repository.usertype_repository;
import com.Auton.GIBGMain.repository.vehicle_repository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle")
@CrossOrigin(origins = "*") // Allow requests from any origin
@Api(tags = "User Management")
public class vehicle_controller {
    private final JdbcTemplate jdbcTemplate;
    private final authToken authService;
    private final IdGeneratorService generateUserId;

    @Autowired
    private admin_repository adminRepository;

    @Autowired
    private usertype_repository usertypeRepository;
    @Autowired
    private vehicle_repository vehicleRepository;


    @Value("${jwt_secret}")
    private String jwt_secret;

    @Autowired
    public vehicle_controller(JdbcTemplate jdbcTemplate, authToken authService,IdGeneratorService generateUserId ) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
        this.generateUserId = generateUserId;
    }
    @PutMapping("/update")
    public ResponseEntity<ResponseWrapper<List<admin_entity>>> updateVehicleInformation(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody adminRequest req_user) {
        try {
            // ตรวจสอบการรับรองใน Authorization Header
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Authorization header is missing or empty.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // ถอดรหัส Token
            String token = authorizationHeader.substring("Bearer ".length());
            Claims claims = Jwts.parser()
                    .setSigningKey(jwt_secret) // Replace with your secret key
                    .parseClaimsJws(token)
                    .getBody();

            // ตรวจสอบความถูกต้องของ Token
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Token has expired.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // ดำเนินการตรวจสอบและอัปเดตข้อมูลยานพาหนะตามที่คุณต้องการ
            List<Vehicle> vehicle = req_user.getVehicle();
            for (Vehicle oneVehicle : vehicle) {
                String licensePlate = oneVehicle.getLicensePlate();

                // ตรวจสอบความถูกต้องของข้อมูลยานพาหนะ
                if (!isValidLicensePlate(licensePlate)) {
                    ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("License plate is not valid.", null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
                }

                // ตรวจสอบว่าเลขทะเบียนรถอยู่ในฐานข้อมูลหรือไม่
                if (!isLicensePlateInDatabase(licensePlate)) {
                    ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("License plate does not exist in the database.", null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
                }

                // ตรวจสอบรหัสผ่าน
                String authenticatedUserId = claims.get("user_id", String.class);
                admin_entity exituser = adminRepository.findByUserId(authenticatedUserId);
                BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
                if (!bcrypt.matches(req_user.getAdmin().getSecretPassword(), exituser.getSecret_password())) {
                    ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Password and secret password do not match.", null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
                }

                // อัปเดตข้อมูลยานพาหนะตามที่คุณต้องการ
                updateVehicleInDatabase(oneVehicle);
            }

            ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Update vehicle information successful", null);
            return ResponseEntity.ok(responseWrapper);

        } catch (JwtException e) {
            // Log the exception to see the details
            e.printStackTrace();
            String errorMessage = "Token is invalid.";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseWrapper<>(errorMessage, null));
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            String errorMessage = "An error occurred while updating vehicle information.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(errorMessage, null));
        }
    }

    // Define your validation, database check, and update methods as needed
    private boolean isValidLicensePlate(String licensePlate) {
        // Add your license plate validation logic here
        return true;
    }

    private boolean isLicensePlateInDatabase(String licensePlate) {
        // Add your database check logic here
        return true;
    }

    private void updateVehicleInDatabase(userType_entity vehicle) {
        // Add your update logic here
    }

}
