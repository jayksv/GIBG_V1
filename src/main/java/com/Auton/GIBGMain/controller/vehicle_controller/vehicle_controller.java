package com.Auton.GIBGMain.controller.vehicle_controller;

import ch.qos.logback.core.net.SMTPAppenderBase;
import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.Response.adminDTO.AdminAllDTO;
import com.Auton.GIBGMain.Response.adminDTO.adminRequest;
import com.Auton.GIBGMain.Response.adminDTO.userVecleDTO;
import com.Auton.GIBGMain.Response.userVecleDTO.VehicleDTO;
//import com.Auton.GIBGMain.Response.vehicle.vehicleAllDTO;
import com.Auton.GIBGMain.Response.vehicle.vehicleAllDTO;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
    @GetMapping("/all")
    public ResponseEntity<?> getAllVehicles(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                ResponseWrapper<Void> responseWrapper = new ResponseWrapper<>("Authorization header is missing or empty.", null);
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
                ResponseWrapper<Void> responseWrapper = new ResponseWrapper<>("Token has expired.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }
            // Extract necessary claims (you can add more as needed)
            String authenticatedUserId = claims.get("user_id", String.class);
            Long roleId = claims.get("role_id", Long.class);

//            String role = claims.get("role_name", String.class);
            // Check if the authenticated user has the appropriate role to perform this action (e.g., admin)
            if (roleId !=1  && roleId !=2 ) {
                ResponseWrapper<Void> responseWrapper = new ResponseWrapper<>("You are not authorized to perform this action.", null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseWrapper);
            }


            String sql = "SELECT * FROM `tb_vehicles`"; // Replace with your SQL query
            List<vehicleAllDTO> vehicles = jdbcTemplate.query(sql, this::mapVehicleRow);

            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
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
            Set<String> seenLicensePlates = new HashSet<>();

// ดึงข้อมูลเลขทะเบียนรถทั้งหมดของ user_id นี้
            String selectSqlUserVicle = "SELECT license_plate FROM tb_user_vicle WHERE user_id = ?";
            List<String> userLicensePlates = jdbcTemplate.queryForList(selectSqlUserVicle, String.class, authenticatedUserId);

// ตรวจสอบความถูกต้องของข้อมูลยานพาหนะและการช้ำ

            String deleteSqlUserVicle = "DELETE FROM tb_user_vicle WHERE user_id = ?";
            jdbcTemplate.update(deleteSqlUserVicle, authenticatedUserId);

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

                // เพิ่มเลขทะเบียนรถลงใน Set เพื่อตรวจสอบในรอบถัดไป
                seenLicensePlates.add(licensePlate);

                // เพิ่มข้อมูลใหม่ลงใน tb_user_vicle
                String insertSqlUserVicle = "INSERT INTO tb_user_vicle (user_id, license_plate) VALUES (?, ?)";
                jdbcTemplate.update(insertSqlUserVicle, authenticatedUserId, licensePlate);
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
        // Validate that the licensePlate is not null, and it matches the desired pattern
        return licensePlate != null && licensePlate.matches("\\d{2,7}");
    }

    private boolean isLicensePlateInDatabase(String licensePlate) {
        // Query the database to check if the licensePlate exists in the database
        String sql = "SELECT COUNT(*) FROM tb_user_vicle WHERE license_plate = ?";
        int userVicleCount = jdbcTemplate.queryForObject(sql, Integer.class, licensePlate);

        sql = "SELECT COUNT(*) FROM tb_vehicles WHERE license_plate = ?";
        int vehiclesCount = jdbcTemplate.queryForObject(sql, Integer.class, licensePlate);

        // If either count is greater than 0, the license plate is in the database
        return userVicleCount > 0 || vehiclesCount > 0;
    }

    private vehicleAllDTO mapVehicleRow(ResultSet rs, int rowNum) throws SQLException {
        vehicleAllDTO vehicleDTO = new vehicleAllDTO();
        vehicleDTO.setLicense_plate(rs.getString("license_plate"));
        vehicleDTO.setVehicle_type(rs.getString("Vehicle_type"));
        vehicleDTO.setManufacturer(rs.getString("Manufacturer"));
        vehicleDTO.setVehicle_type_name(rs.getString("Vehicle_type_name"));
        vehicleDTO.setVehicle_year(rs.getString("vehicle_year"));
        vehicleDTO.setFuel(rs.getString("fuel"));
        vehicleDTO.setExhast_cc(rs.getString("exhast_cc"));
        vehicleDTO.setColor_vehicle(rs.getString("color_vehicle"));
        vehicleDTO.setFirst_registration(rs.getString("first_registration"));
        vehicleDTO.setVehicle_stand_number(rs.getString("vehicle_stand_number"));
        vehicleDTO.setDate_of_vehicle(rs.getString("date_of_vehicle"));
        vehicleDTO.setClassification_of_vehicle_type(rs.getString("Classification_of_vehicle_type"));
        vehicleDTO.setFinal_inspection_date(rs.getString("Final_inspection_date"));

        return vehicleDTO;

    }

}
