package com.Auton.GIBGMain.controller;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.Response.adminDTO.AdminAllDTO;
import com.Auton.GIBGMain.entity.AdminAddressWrapper;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
            String sql = "SELECT `user_id`, `username`, `password`, `surname`, `phone`, `secret_password`, `role_id`, `email` FROM `tb_users` WHERE `username` = ?";
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
    @GetMapping("/user/all")
    public ResponseEntity<ResponseWrapper<List<AdminAllDTO>>> getAllUsers(@RequestHeader("Authorization") String authorizationHeader) {
        try {


            String sql = "SELECT *FROM `tb_view`";

            List<AdminAllDTO> users = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                AdminAllDTO usersDTO = new AdminAllDTO();
                usersDTO.setUser_id(resultSet.getString("user_id"));
                usersDTO.setUsername(resultSet.getString("email"));
                usersDTO.setEmail(resultSet.getString("username"));
                usersDTO.setSurname(resultSet.getString("surname"));
                usersDTO.setPhone(resultSet.getString("phone"));
//                usersDTO.setRole_name(resultSet.getString("role_name"));
                usersDTO.setVehicle_id(resultSet.getLong("vehicle_id"));
                usersDTO.setCreated_at(resultSet.getDate("created_at"));
                usersDTO.setIs_active(resultSet.getString("is_active"));
                return usersDTO;
            });

            ResponseWrapper<List<AdminAllDTO>> responseWrapper = new ResponseWrapper<>("User data retrieved successfully.", users);
            return ResponseEntity.ok(responseWrapper);
        } catch (JwtException e) {
            // Token is invalid or has expired
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseWrapper<>("Token is invalid.", null));
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            String errorMessage = "An error occurred while retrieving user data.";
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

// Rest of your code for user creation


            // Generate a unique user_id
            String userId = generateUserId.generateUserId();
            req_user.setUser_id(userId);

            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
            String encryptedPass = bcrypt.encode(req_user.getPassword());
            req_user.setPassword(encryptedPass);
            req_user.setSecret_password(encryptedPass);
            req_user.setPhone(encryptedPass);

            req_user.setIs_active("Active");
//            user.setAddress_id(savedAddress.getAddressId().longValue());
            req_user.setRole_id((long) 1);

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
    @PutMapping("/user/update_subadmin/{userId}")
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
            Optional<admin_entity> existingUserOptional = adminRepository.findById(userId);

            if (existingUserOptional.isEmpty()) {
                // User not found, return an error response
                ResponseWrapper<admin_entity> responseWrapper = new ResponseWrapper<>("User not found.", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseWrapper);
            }

            // Get the existing user entity
            admin_entity existingUser = existingUserOptional.get();

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
}
