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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            @RequestBody adminRequest req_user
            ) {
        try {
            userType_entity updatedVehicle = new userType_entity();
            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

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
            // ตรวจสอบรหัสผ่าน
            String authenticatedUserId = claims.get("user_id", String.class);
            admin_entity exituser = adminRepository.findByUserId(authenticatedUserId);

            if (!bcrypt.matches(req_user.getAdmin().getSecret_password(), exituser.getSecret_password())) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Password and secret password do not match.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }

            // เก็บเลขทะเบียนรถที่เคยเห็นแล้ว
            Set<String> seenLicensePlates = new HashSet<>();

// ดำเนินการตรวจสอบและอัปเดตข้อมูลยานพาหนะตามที่คุณต้องการ
            List<userType_entity> vehicle = req_user.getVehicle();
            for (userType_entity oneVehicle : vehicle) {
                String licensePlate = oneVehicle.getLicense_plate();

                // ตรวจสอบความถูกต้องของข้อมูลยานพาหนะ
                if (!isValidLicensePlate(licensePlate)) {
                    ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("License plate is not valid.", null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
                }

                // ตรวจสอบว่าเลขทะเบียนรถไม่ซ้ำกัน
                if (seenLicensePlates.contains(licensePlate)) {
                    ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Duplicate license plate found in the request.", null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
                }

                // ตรวจสอบว่าเลขทะเบียนรถอยู่ในฐานข้อมูลหรือไม่
                if (!isLicensePlateInDatabase(licensePlate)) {
                    ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("License plate does not exist in the database.", null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
                }



                // เพิ่มเลขทะเบียนรถลงในเซ็ตเพื่อตรวจสอบในรอบถัดไป
                seenLicensePlates.add(oneVehicle.getLicense_plate());

                // กำหนดค่า updatedVehicle เท่ากับ oneVehicle
                updatedVehicle = oneVehicle;

                // ลบข้อมูลยานพาหนะเดิมและเพิ่มข้อมูลใหม่ลงในฐานข้อมูล
                String deleteSql = "DELETE FROM tb_user_vicle WHERE user_id = ? AND license_plate = ?";
                jdbcTemplate.update(deleteSql, authenticatedUserId, oneVehicle.getLicense_plate());
                String insertSql = "INSERT INTO tb_user_vicle (user_id, license_plate) VALUES (?, ?)";
                jdbcTemplate.update(insertSql, authenticatedUserId, updatedVehicle.getLicense_plate());
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

    private boolean isValidLicensePlate(String licensePlate) {
        // ตรวจสอบว่า licensePlate เป็นตัวเลขและมีความยาวที่ถูกต้อง (ตัวอย่างเช่น 2 ถึง 7 หลัก)
        return licensePlate != null && licensePlate.matches("\\d{2,7}");
    }

    private boolean isLicensePlateInDatabase(String licensePlate) {
        // เพิ่มโค้ดเชื่อมต่อกับฐานข้อมูลเพื่อตรวจสอบว่า licensePlate อยู่ในฐานข้อมูลหรือไม่
        // คืนค่า true หากมีในฐานข้อมูลแล้ว หรือคืนค่า false ถ้าไม่มี
        // เปลี่ยนโค้ดด้านล่างให้สอดคล้องกับระบบฐานข้อมูลของคุณ
        // เช่น ถ้าคุณใช้ Spring JDBC Template จะเป็นไปดังนี้
        String sql = "SELECT COUNT(*) FROM tb_user_vicle WHERE license_plate = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, licensePlate);
        return count > 0;
    }

    private void updateVehicleInDatabase(userType_entity vehicle) {
        // ก่อนที่จะเพิ่มข้อมูลใหม่ลงในฐานข้อมูล
        // ลบข้อมูลเดิมของ vehicle ออกจากฐานข้อมูล
        String deleteSql = "DELETE FROM tb_user_vicle WHERE user_id = ? AND license_plate = ?";
        jdbcTemplate.update(deleteSql, vehicle.getUser_id(), vehicle.getLicense_plate());

        // ต่อมาให้เพิ่มข้อมูลใหม่ลงในฐานข้อมูล
        String insertSql = "INSERT INTO tb_user_vicle (user_id, license_plate) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, vehicle.getUser_id(), vehicle.getLicense_plate());
    }


}
