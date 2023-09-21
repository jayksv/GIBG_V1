package com.Auton.GIBGMain.controller.service;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.entity.service.service_entity;
import com.Auton.GIBGMain.middleware.authToken;
import com.Auton.GIBGMain.repository.service.service_repository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin")
@CrossOrigin(origins = "*") // Allow requests from any origin
public class service_controller {
    @Value("${jwt_secret}")
    private String jwt_secret;
    private final JdbcTemplate jdbcTemplate;
    private final authToken authService;
    @Autowired
    private service_repository serviceRepository;

    @Autowired
    public service_controller(JdbcTemplate jdbcTemplate, authToken authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
    }
    @PostMapping("/service/insert")
    public ResponseEntity<ResponseWrapper<String>> insertService(
            @RequestBody service_entity service,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<Void> authResponseBody = authResponse.getBody();
                return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
            }

// Verify the token from the Authorization header
            String token = authorizationHeader.substring("Bearer ".length());

            Claims claims = Jwts.parser()
                    .setSigningKey(jwt_secret) // Replace with your secret key
                    .parseClaimsJws(token)
                    .getBody();

// Extract necessary claims (you can add more as needed)
            String authenticatedUserId = claims.get("user_id", String.class);

// Check if required fields are not null
            if (service.getService_name() == null || service.getServiceType_id() == null) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Service fields cannot be null.", null);
                return ResponseEntity.badRequest().body(responseWrapper);
            }

// Insert service using SQL
            String insertServiceSql = "INSERT INTO tb_service (`service_name`, `description`, `createBy`, `service_icon`, `serviceType_id`) " +
                    "VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertServiceSql,
                    service.getService_name(),
                    service.getDescription(),
                    authenticatedUserId, // Use Long directly
                    service.getService_icon(),
                    service.getServiceType_id());




            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Service inserted successfully", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while inserting the service", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }


}
