package com.Auton.GIBGMain.controller.service;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.Response.service.ServiceDTO;
import com.Auton.GIBGMain.entity.service.service_entity;
import com.Auton.GIBGMain.middleware.authToken;
import com.Auton.GIBGMain.repository.service.service_repository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/service/all")
    public ResponseEntity<ResponseWrapper<List<ServiceDTO>>> getAllServices(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<Void> authResponseBody = authResponse.getBody();
                return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
            }

            // Retrieve services using SQL query
            String selectAllServicesSql = "SELECT tb_service.service_id, tb_service.service_name, tb_service.description, tb_service.service_icon, tb_service.serviceType_id, tb_service.created_at, tb_service_type.type_name, tb_users.first_name " +
                    "FROM tb_service " +
                    "LEFT JOIN tb_service_type ON tb_service.serviceType_id = tb_service_type.serviceType_id " +
                    "LEFT JOIN tb_users ON tb_service.createBy = tb_users.user_id";

            List<ServiceDTO> services = jdbcTemplate.query(selectAllServicesSql, (resultSet, rowNum) -> {
                ServiceDTO service = new ServiceDTO();
                service.setService_id(resultSet.getLong("service_id"));
                service.setService_name(resultSet.getString("service_name"));
                service.setDescription(resultSet.getString("description"));
                service.setService_icon(resultSet.getString("service_icon"));
                service.setServiceType_id(resultSet.getLong("serviceType_id"));
                service.setCreated_at(resultSet.getTimestamp("created_at"));
                service.setType_name(resultSet.getString("type_name"));
                service.setFirst_name(resultSet.getString("first_name"));
                return service;
            });

            ResponseWrapper<List<ServiceDTO>> responseWrapper = new ResponseWrapper<>("Success", services);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<List<ServiceDTO>> responseWrapper = new ResponseWrapper<>("An error occurred while retrieving services", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @GetMapping("/service/findById/{serviceId}")
    public ResponseEntity<ResponseWrapper<ServiceDTO>> getServiceById(
            @PathVariable Long serviceId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<Void> authResponseBody = authResponse.getBody();
                return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
            }

            // Retrieve service by ID using SQL
            String selectServiceByIdSql = "SELECT tb_service.service_id, tb_service.service_name, tb_service.description, tb_service.service_icon, tb_service.serviceType_id, tb_service.created_at, tb_service_type.type_name, tb_users.first_name " +
                    "FROM tb_service " +
                    "LEFT JOIN tb_service_type ON tb_service.serviceType_id = tb_service_type.serviceType_id " +
                    "LEFT JOIN tb_users ON tb_service.createBy = tb_users.user_id " +
                    "WHERE tb_service.service_id = ?";

            try {
                ServiceDTO service = jdbcTemplate.queryForObject(selectServiceByIdSql,
                        new Object[]{serviceId},
                        (resultSet, rowNum) -> new ServiceDTO(
                                resultSet.getLong("service_id"),
                                resultSet.getString("service_name"),
                                resultSet.getString("description"),
                                resultSet.getString("service_icon"),
                                resultSet.getLong("serviceType_id"),
                                resultSet.getTimestamp("created_at"),
                                resultSet.getString("type_name"),
                                resultSet.getString("first_name")
                        ));

                ResponseWrapper<ServiceDTO> responseWrapper = new ResponseWrapper<>("Success", service);
                return ResponseEntity.ok(responseWrapper);
            } catch (EmptyResultDataAccessException ex) {
                ResponseWrapper<ServiceDTO> notFoundResponse = new ResponseWrapper<>("Service not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<ServiceDTO> responseWrapper = new ResponseWrapper<>("An error occurred while retrieving the service", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @PutMapping("/service/update/{serviceId}")
    public ResponseEntity<ResponseWrapper<String>> updateService(
            @PathVariable Long serviceId,
            @RequestBody service_entity updatedService,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<Void> authResponseBody = authResponse.getBody();
                return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
            }

            // Check if the service exists
            String selectServiceSql = "SELECT * FROM tb_service WHERE service_id = ?";
            try {
                service_entity existingService = jdbcTemplate.queryForObject(selectServiceSql, new Object[]{serviceId},
                        (resultSet, rowNum) -> new service_entity(
                                resultSet.getLong("service_id"),
                                resultSet.getString("service_name"),
                                resultSet.getString("service_icon"),
                                resultSet.getString("description"),
                                resultSet.getString("createBy"),
                                resultSet.getLong("serviceType_id")
                        ));

                // Debugging: Log the retrieved existingService and serviceId
                System.out.println("Existing Service: " + existingService);
                System.out.println("Service ID: " + serviceId);

                if (existingService == null) {
                    ResponseWrapper<String> notFoundResponse = new ResponseWrapper<>("Service not found", null);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
                }

                // Update the service fields
                existingService.setService_name(updatedService.getService_name());
                existingService.setDescription(updatedService.getDescription());
                existingService.setService_icon(updatedService.getService_icon());
                existingService.setServiceType_id(updatedService.getServiceType_id());

                // Update service using SQL
                String updateServiceSql = "UPDATE tb_service SET service_name = ?, description = ?, service_icon = ?, serviceType_id = ? WHERE service_id = ?";
                jdbcTemplate.update(updateServiceSql,
                        existingService.getService_name(),
                        existingService.getDescription(),
                        existingService.getService_icon(),
                        existingService.getServiceType_id(),
                        serviceId);

                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Service updated successfully", null);
                return ResponseEntity.ok(responseWrapper);
            } catch (EmptyResultDataAccessException ex) {
                // Handle case where no service was found
                ResponseWrapper<String> notFoundResponse = new ResponseWrapper<>("Service not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while updating the service", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @DeleteMapping("/service/delete/{serviceId}")
    public ResponseEntity<ResponseWrapper<String>> deleteService(
            @PathVariable Long serviceId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<Void> authResponseBody = authResponse.getBody();
                return ResponseEntity.status(authResponse.getStatusCode()).body(new ResponseWrapper<>(authResponseBody.getMessage(), null));
            }

            // Check if the service exists
            String selectServiceSql = "SELECT * FROM tb_service WHERE service_id = ?";
            try {
                service_entity existingService = jdbcTemplate.queryForObject(selectServiceSql, new Object[]{serviceId},
                        (resultSet, rowNum) -> new service_entity(
                                resultSet.getLong("service_id"),
                                resultSet.getString("service_name"),
                                resultSet.getString("service_icon"),
                                resultSet.getString("description"),
                                resultSet.getString("createBy"),
                                resultSet.getLong("serviceType_id")
                        ));

                // Debugging: Log the retrieved existingService and serviceId
                System.out.println("Existing Service: " + existingService);
                System.out.println("Service ID: " + serviceId);

                if (existingService == null) {
                    ResponseWrapper<String> notFoundResponse = new ResponseWrapper<>("Service not found", null);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
                }

                // Delete service using SQL
                String deleteServiceSql = "DELETE FROM tb_service WHERE service_id = ?";
                jdbcTemplate.update(deleteServiceSql, serviceId);

                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Service deleted successfully", null);
                return ResponseEntity.ok(responseWrapper);
            } catch (EmptyResultDataAccessException ex) {
                // Handle case where no service was found
                ResponseWrapper<String> notFoundResponse = new ResponseWrapper<>("Service not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while deleting the service", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
}
