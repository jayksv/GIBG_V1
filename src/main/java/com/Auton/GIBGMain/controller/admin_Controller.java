package com.Auton.GIBGMain.controller;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.Response.userVecleDTO.VehicleDTO;
import com.Auton.GIBGMain.Response.adminDTO.AdminAllDTO;
import com.Auton.GIBGMain.Response.adminDTO.userVecleDTO;
import com.Auton.GIBGMain.entity.LoginResponse;
import com.Auton.GIBGMain.entity.admin_entity;
import com.Auton.GIBGMain.myfuntion.IdGeneratorService;
import com.Auton.GIBGMain.middleware.authToken;
import com.Auton.GIBGMain.repository.admin_repository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // Allow requests from any origin
@Api(tags = "User Management")
public class admin_Controller {

    private final JdbcTemplate jdbcTemplate;
    private final authToken authService;
    private final IdGeneratorService generateUserId;

    @Autowired
    private admin_repository adminRepository;


    @Value("${jwt_secret}")
    private String jwt_secret;
    @Autowired
    public admin_Controller(JdbcTemplate jdbcTemplate, authToken authService,IdGeneratorService generateUserId ) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
        this.generateUserId = generateUserId;
    }
    @PostMapping("/login/login")
    public ResponseEntity<ResponseWrapper<LoginResponse>> superAdminLogin(@RequestBody admin_entity user) {
        try {
            // Validate username format
            // if (!isValidUsername(user.getUsername())) {
            //     ResponseWrapper<LoginResponse> responseWrapper = new ResponseWrapper<>("Invalid username format.", null);
            //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            // }

            // Check for null or invalid password
            if (user.getPassword() == null || user.getPassword().length() < 8) {
                ResponseWrapper<LoginResponse> responseWrapper = new ResponseWrapper<>("Invalid password. Password should be at least 8 characters long.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }

            // Custom SQL query to retrieve SuperAdmin by username
            String sql = "SELECT `user_id`, `username`, `password`, `first_name`, `last_name`, `datebirth`, `phone`, `secret_password`, `role_id`, `email`, `vehicle_id`, `image_profile`, `created_at`, `create_by`, `is_active`, `gender`, `address`, `shop_id` FROM `tb_users` WHERE `username` = ?";
            admin_entity superAdmin = jdbcTemplate.queryForObject(sql, new Object[]{user.getUsername()}, (resultSet, rowNum) -> {
                admin_entity superAdminEntity = new admin_entity();
                superAdminEntity.setUser_id(resultSet.getString("user_id"));
//                superAdminEntity.setUsername(resultSet.getString("username"));
//                superAdminEntity.setEmail(resultSet.getString("email"));
                superAdminEntity.setPassword(resultSet.getString("password"));
//                superAdminEntity.setSurname(resultSet.getString("surname"));
//                superAdminEntity.setPhone(resultSet.getString("phone"));
//                superAdminEntity.set(resultSet.getString("secret_password"));
                superAdminEntity.setRole_id(resultSet.getLong("role_id"));
                return superAdminEntity;
            });

            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
            if (bcrypt.matches(user.getPassword(), superAdmin.getPassword())) {
                // Build custom claims
                Claims claims = Jwts.claims();
                claims.setSubject(superAdmin.getUsername());
                claims.put("user_id", superAdmin.getUser_id());
//                claims.put("username", superAdmin.getUsername());
//                claims.put("email", superAdmin.getEmail());
//                claims.put("role_name", superAdmin.getRole_id());
                claims.put("role_id", superAdmin.getRole_id());

                String token = Jwts.builder()
                        .setClaims(claims)
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(new Date(System.currentTimeMillis() + 86400000 * 24)) // Token will expire in 24 hours
                        .signWith(SignatureAlgorithm.HS512, jwt_secret)
                        .compact();

                LoginResponse loginResponse = new LoginResponse(token, null);
                ResponseWrapper<LoginResponse> responseWrapper = new ResponseWrapper<>("Login successful.", loginResponse);


                return ResponseEntity.ok(responseWrapper);
            } else {
                // Invalid credentials or SuperAdmin not found
                ResponseWrapper<LoginResponse> responseWrapper = new ResponseWrapper<>("Invalid credentials.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }
        } catch (EmptyResultDataAccessException e) {
            // SuperAdmin not found
            ResponseWrapper<LoginResponse> responseWrapper = new ResponseWrapper<>("SuperAdmin not found.", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace(); // This will log the error on the server
            String errorMessage = "An error occurred while logging in: " + e.getMessage();
            // Handle the error and return an error response
            ResponseWrapper<LoginResponse> errorResponse = new ResponseWrapper<>(errorMessage, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
//    @GetMapping("/user/all")
//    public ResponseEntity<ResponseWrapper<List<AdminAllDTO>>> getAllUsers( @RequestHeader("Authorization") String authorizationHeader) {
//
//
//        try {
//
//            if (authorizationHeader == null || authorizationHeader.isBlank()) {
//               ResponseWrapper<List<AdminAllDTO>> responseWrapper = new ResponseWrapper<>("Authorization header is missing or empty.", null);
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
//            }
//            // Verify the token from the Authorization header
//            String token = authorizationHeader.substring("Bearer ".length());
//
//            Claims claims = Jwts.parser()
//                    .setSigningKey(jwt_secret) // Replace with your secret key
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            // Check token expiration
//            Date expiration = claims.getExpiration();
//            if (expiration != null && expiration.before(new Date())) {
//                ResponseWrapper<List<AdminAllDTO>> responseWrapper = new ResponseWrapper<>("Token has expired.", null);
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
//            }
//
//            // Extract necessary claims (you can add more as needed)
//            String authenticatedUserId = claims.get("user_id", String.class);
//            Long roleId = claims.get("role_id", Long.class);
//
////            String role = claims.get("role_name", String.class);
//            // Check if the authenticated user has the appropriate role to perform this action (e.g., admin)
//            if (roleId !=2 ) {
//                ResponseWrapper<List<AdminAllDTO>> responseWrapper = new ResponseWrapper<>("You are not authorized to perform this action.", null);
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseWrapper);
//            }
//
//
//            String sql = "SELECT *FROM `tb_view` WHERE role_id <> 1";
//
//            List<AdminAllDTO> users = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
//                AdminAllDTO usersDTO = new AdminAllDTO();
//                usersDTO.setUser_id(resultSet.getString("user_id"));
//                usersDTO.setEmail(resultSet.getString("username"));
//                usersDTO.setUsername(resultSet.getString("email"));
//                usersDTO.setSurname(resultSet.getString("surname"));
//                usersDTO.setPhone(resultSet.getString("phone"));
////                usersDTO.setRole_name(resultSet.getString("role_name"));
//                usersDTO.setVehicle_id(resultSet.getLong("vehicle_id"));
//                usersDTO.setCreated_at(resultSet.getDate("created_at"));
//                usersDTO.setIs_active(resultSet.getString("is_active"));
//                return usersDTO;
//            });
//
//            ResponseWrapper<List<AdminAllDTO>> responseWrapper = new ResponseWrapper<>("User data retrieved successfully.", users);
//            return ResponseEntity.ok(responseWrapper);
//        } catch (JwtException e) {
//            // Token is invalid or has expired
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new ResponseWrapper<>("Token is invalid.", null));
//        } catch (Exception e) {
//            // Log the error for debugging
//            e.printStackTrace();
//            String errorMessage = "An error occurred while retrieving user data.";
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ResponseWrapper<>(errorMessage, null));
//        }
//    }
@GetMapping("/admin/all")
public ResponseEntity<?> getAllAdmin(@RequestHeader("Authorization") String authorizationHeader) {
    try {
        // Validate authorization using authService
        ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            ResponseWrapper<Void> authResponseBody = authResponse.getBody();
            return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
        }



        String sql ="SELECT tb_users.user_id,tb_users.username,tb_users.first_name,tb_users.last_name,tb_users.datebirth,tb_users.phone,tb_users.role_id,tb_users.email,tb_users.image_profile,tb_users.created_at,tb_users.create_by,tb_users.is_active,tb_users.gender,tb_users.address,tb_role.role_name\n" +
                "FROM tb_users\n" +
                "JOIN tb_role ON tb_users.role_id = tb_role.role_id\n" +
                "WHERE tb_users.role_id =2";
        List<AdminAllDTO> username = jdbcTemplate.query(sql, this::mapUserRow);

        List<userVecleDTO> responses = new ArrayList<>();
        for (AdminAllDTO user : username) {
            List<VehicleDTO> usertype = findUserTypeByUserID((user.getUser_id()));


            userVecleDTO response = new userVecleDTO("Success", user,usertype);
            responses.add(response);
        }

        return ResponseEntity.ok(responses);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
@GetMapping("/user/all")
public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
    try {
        // Validate authorization using authService
        ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
        if (authResponse.getStatusCode() != HttpStatus.OK) {
            ResponseWrapper<Void> authResponseBody = authResponse.getBody();
            return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
        }



        String sql ="SELECT tb_users.user_id,tb_users.username,tb_users.first_name,tb_users.last_name,tb_users.datebirth,tb_users.phone,tb_users.role_id,tb_users.email,tb_users.image_profile,tb_users.created_at,tb_users.create_by,tb_users.is_active,tb_users.gender,tb_users.address,tb_role.role_name\n" +
                "FROM tb_users\n" +
                "JOIN tb_role ON tb_users.role_id = tb_role.role_id\n" +
                "WHERE tb_users.role_id =3";
        List<AdminAllDTO> username = jdbcTemplate.query(sql, this::mapUserRow);

        List<userVecleDTO> responses = new ArrayList<>();
        for (AdminAllDTO user : username) {
            List<VehicleDTO> usertype = findUserTypeByUserID((user.getUser_id()));


            userVecleDTO response = new userVecleDTO("Success", user,usertype);
            responses.add(response);
        }

        return ResponseEntity.ok(responses);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

    @GetMapping("/user/findbyid/{userId}")
    public ResponseEntity<ResponseWrapper<AdminAllDTO>> getUserById(@RequestHeader("Authorization") String authorizationHeader, @PathVariable String userId) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                ResponseWrapper<AdminAllDTO> responseWrapper = new ResponseWrapper<>("Authorization header is missing or empty.", null);
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
                ResponseWrapper<AdminAllDTO> responseWrapper = new ResponseWrapper<>("Token has expired.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }


            // Query the user by ID
            String sql = "SELECT * FROM `gibg_view` WHERE user_id = ?";
            AdminAllDTO user = jdbcTemplate.queryForObject(sql, new Object[] { userId }, (resultSet, rowNum) -> {
                AdminAllDTO userDTO = new AdminAllDTO();
                userDTO.setUser_id(resultSet.getString("user_id"));
                userDTO.setUsername(resultSet.getString("username"));
                userDTO.setEmail(resultSet.getString("email"));
//                usersDTO.setFirst_name(resultSet.getString("first_name"));
                userDTO.setPhone(resultSet.getString("phone"));
//                userDTO.setVehicle_id(resultSet.getString("vehicle_id"));
                userDTO.setCreated_at(resultSet.getDate("created_at"));
                userDTO.setIs_active(resultSet.getString("is_active"));
                return userDTO;
            });

            if (user != null) {
                ResponseWrapper<AdminAllDTO> responseWrapper = new ResponseWrapper<>("User found by ID.", user);
                return ResponseEntity.ok(responseWrapper);
            } else {
                return ResponseEntity.notFound().build(); // Return 404 without a response body
            }
        } catch (JwtException e) {
            // Token is invalid or has expired
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseWrapper<>("Token is invalid.", null));
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            String errorMessage = "An error occurred while retrieving user data by ID.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(errorMessage, null));
        }
    }
    @PostMapping("/user/profile")
    public ResponseEntity<ResponseWrapper<AdminAllDTO>> getUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || authorizationHeader.isBlank()) {
                ResponseWrapper<AdminAllDTO> responseWrapper = new ResponseWrapper<>("Authorization header is missing or empty.", null);
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
                ResponseWrapper<AdminAllDTO> responseWrapper = new ResponseWrapper<>("Token has expired.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Extract necessary claims (you can add more as needed)
            String authenticatedUserId = claims.get("user_id", String.class);

System.out.println(authenticatedUserId);
            // Query the user by ID
            String sql = "SELECT * FROM `gibg_view` WHERE user_id = ?";
            AdminAllDTO user = jdbcTemplate.queryForObject(sql, new Object[] { authenticatedUserId }, (resultSet, rowNum) -> {
                AdminAllDTO userDTO = new AdminAllDTO();
                userDTO.setUser_id(resultSet.getString("user_id"));
                userDTO.setUsername(resultSet.getString("username"));
                userDTO.setEmail(resultSet.getString("email"));
//                usersDTO.setFirst_name(resultSet.getString("first_name"));
                userDTO.setPhone(resultSet.getString("phone"));
//                userDTO.setVehicle_id(resultSet.getString("vehicle_id"));
                userDTO.setCreated_at(resultSet.getDate("created_at"));
                userDTO.setIs_active(resultSet.getString("is_active"));
                return userDTO;
            });

            if (user != null) {
                ResponseWrapper<AdminAllDTO> responseWrapper = new ResponseWrapper<>("User profile retrieved successfully.", user);
                return ResponseEntity.ok(responseWrapper);
            } else {
                return ResponseEntity.notFound().build(); // Return 404 without a response body
            }
        } catch (JwtException e) {
            // Log the exception to see the details
            e.printStackTrace();
            String errorMessage = "Token is invalid.";
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseWrapper<>(errorMessage, null));
        }
        catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            String errorMessage = "An error occurred while retrieving user profile.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseWrapper<>(errorMessage, null));
        }
    }

    @PostMapping("/user/add_subadmin")
    public ResponseEntity<ResponseWrapper<List<admin_entity>>> addNewUser(@RequestBody admin_entity req_user) {
        try {
            // Check if the username already exists
            admin_entity existingUser = adminRepository.findByUsername(req_user.getUsername());

            if (existingUser != null) {
                // Username already exists, return an error response
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Username already exists.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }
            // Check if the phone number is already registered in the database
            String phone = req_user.getPhone();
            admin_entity existingUserByPhone = adminRepository.findByPhone(phone);
            System.out.println(existingUserByPhone);

            if (existingUserByPhone != null) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Phone number is already registered.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }



            // Check password
            String password = req_user.getPassword();

            if (password.length() < 8) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Password should be at least 8 characters long.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }
            if (!password.matches(".*[a-z].*")) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Password should contain at least one lowercase letter.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }
            if (!password.matches(".*[A-Z].*")) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Password should contain at least one uppercase letter.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }
            if (!password.matches(".*\\d.*")) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Password should contain at least one digit.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }
            if (!password.matches(".*[@#$%^&+=].*")) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Password should contain at least one special character.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }
            // Check if the phone number contains only digits
            if (!req_user.getPhone().matches("\\d+")) {
                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Phone number should contain only digits.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }


// Rest of your code for user creation


            // Generate a unique user_id
            String userId = generateUserId.generateUserId();
            req_user.setUser_id(userId);

            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
            String encryptedPass = bcrypt.encode(req_user.getPassword());
            req_user.setPassword(encryptedPass);
            req_user.setSecret_password(encryptedPass);
//            req_user.setPhone(encryptedPass);

            req_user.setIs_active("Active");
//            user.setAddress_id(savedAddress.getAddressId().longValue());
            req_user.setRole_id((long) 2);

                admin_entity savedUser = adminRepository.save(req_user);

                ResponseWrapper<List<admin_entity>> responseWrapper = new ResponseWrapper<>("Insert new user and address successful", null);
                return ResponseEntity.ok(responseWrapper);



        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "An error occurred while adding a new user.";
            ResponseWrapper<List<admin_entity>> errorResponse = new ResponseWrapper<>(errorMessage, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @PutMapping("/user/update_subadmin")
    public ResponseEntity<ResponseWrapper<admin_entity>> updateUser(
            @PathVariable String userId,
            @RequestBody admin_entity updatedUser,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<admin_entity> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }
            // Check if the user with the given userId exists
            admin_entity existingUser = adminRepository.findByUserId(userId);

            if (existingUser == null) {
                // User not found, return an error response
                ResponseWrapper<admin_entity> responseWrapper = new ResponseWrapper<>("User not found.", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseWrapper);
            }

            // Update the user entity with the new data
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            // Update other fields as needed

            // Save the updated user entity
            admin_entity updatedUserEntity = adminRepository.save(existingUser);

            ResponseWrapper<admin_entity> responseWrapper = new ResponseWrapper<>("User updated successfully.", updatedUserEntity);
            return ResponseEntity.ok(responseWrapper);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "An error occurred while updating the user.";
            ResponseWrapper<admin_entity> errorResponse = new ResponseWrapper<>(errorMessage, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @DeleteMapping("/user/delete/{userId}")
    public ResponseEntity<ResponseWrapper<String>> deleteUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Check if the user with the given userId exists
            admin_entity existingUser = adminRepository.findByUserId(userId);

            if (existingUser == null) {
                // User not found, return an error response
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("User not found.", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseWrapper);
            }

            // Delete the user
            adminRepository.delete(existingUser);

            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("User deleted successfully.", null);
            return ResponseEntity.ok(responseWrapper);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "An error occurred while deleting the user.";
            ResponseWrapper<String> errorResponse = new ResponseWrapper<>(errorMessage, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private AdminAllDTO mapUserRow(ResultSet rs, int rowNum) throws SQLException {
        AdminAllDTO usersDTO = new AdminAllDTO();
                usersDTO.setUser_id(rs.getString("user_id"));
                usersDTO.setEmail(rs.getString("email"));
                usersDTO.setUsername(rs.getString("username"));
                usersDTO.setFirst_name(rs.getString("first_name"));
                usersDTO.setPhone(rs.getString("phone"));
                usersDTO.setDatebirth(rs.getString("datebirth"));
                usersDTO.setImage_profile(rs.getString("image_profile"));
                usersDTO.setCreated_at(rs.getDate("created_at"));
                usersDTO.setCreate_by(rs.getLong("create_by"));
                usersDTO.setIs_active(rs.getString("is_active"));
        usersDTO.setGender(rs.getString("gender"));
        usersDTO.setAddress(rs.getString("address"));

                return usersDTO;

    }

    private List<VehicleDTO> findUserTypeByUserID(String userId) {
        String sql = "SELECT uv.`user_vicle_id`, uv.`user_id`, uv.`license_plate`, uv.`create_at`, v.`Vehicle_type`, v.`Manufacturer`, v.`Vehicle_type_name`, v.`vehicle_year`, v.`fuel`, v.`exhast_cc`, v.`color_vehicle`, v.`first_registration`, v.`vehicle_stand_number`, v.`date_of_vehicle`, v.`Classification_of_vehicle_type`, v.`Final_inspection_date` FROM `tb_user_vicle` uv JOIN `tb_vehicles` v ON uv.`license_plate` = v.`license_plate` WHERE uv.user_id = ?";
        return jdbcTemplate.query(sql, this::mapUsertypeRow, userId);
    }
    private VehicleDTO mapUsertypeRow(ResultSet rs, int rowNum) throws SQLException {
        VehicleDTO userType = new VehicleDTO();
        userType.setUser_vicle_id(rs.getLong("user_vicle_id"));
        userType.setUser_id(rs.getString("user_id"));
        userType.setLicense_plate(rs.getString("license_plate"));
        userType.setVehicle_type(rs.getString("Vehicle_type"));
        userType.setManufacturer(rs.getString("Manufacturer"));
        userType.setVehicle_type_name(rs.getString("Vehicle_type_name"));
        userType.setVehicle_year(rs.getString("vehicle_year"));
        userType.setFuel(rs.getString("fuel"));
        userType.setExhast_cc(rs.getString("exhast_cc"));
        userType.setColor_vehicle(rs.getString("color_vehicle"));
        userType.setFirst_registration(rs.getString("first_registration"));
        userType.setVehicle_stand_number(rs.getString("vehicle_stand_number"));
        userType.setDate_of_vehicle(rs.getString("date_of_vehicle"));
        userType.setClassification_of_vehicle_type(rs.getString("Classification_of_vehicle_type"));
        userType.setFinal_inspection_date(rs.getString("Final_inspection_date"));

        return userType;
    }


}
