package com.Auton.GIBGMain.controller.service;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.entity.service.serviceType_entity;
import com.Auton.GIBGMain.middleware.authToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/admin")
@CrossOrigin(origins = "*") // Allow requests from any origin
public class serviceType_Controller {
    private final JdbcTemplate jdbcTemplate;
    private final authToken authService;

    @Autowired
    public serviceType_Controller(JdbcTemplate jdbcTemplate, authToken authService) {
        this.jdbcTemplate = jdbcTemplate;
        this.authService = authService;
    }
    @PostMapping("/serviceType/insert")
    public ResponseEntity<ResponseWrapper<String>> insertServiceType(
            @RequestBody serviceType_entity serviceType,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {

            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Check if required fields are not null
            if (serviceType.getType_name() == null) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Type name cannot be null.", null);
                return ResponseEntity.badRequest().body(responseWrapper);
            }

            // Insert service type using SQL
            String insertServiceTypeSql = "INSERT INTO tb_service_type (`type_name`, `parent_type_id`, `serviceType_icon`) VALUES (?, ? ,?)";
            jdbcTemplate.update(insertServiceTypeSql, serviceType.getType_name(), serviceType.getParent_type_id(), serviceType.getServiceType_icon());

            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Service type inserted successfully", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while inserting the service type", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @GetMapping("/serviceType/all")
    public ResponseEntity<ResponseWrapper<List<serviceType_entity>>> getServiceTypeAll(
//            @PathVariable Long parent_id,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<List<serviceType_entity>> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Retrieve service types by parent ID using SQL
            String selectServiceTypesSql = "SELECT * FROM tb_service_type WHERE parent_type_id = ?";
            List<serviceType_entity> serviceTypes = jdbcTemplate.query(selectServiceTypesSql,
                    new Object[]{0},
                    (resultSet, rowNum) -> new serviceType_entity(
                            resultSet.getLong("serviceType_id"),
                            resultSet.getString("type_name"),
                            resultSet.getString("serviceType_icon"),
                            resultSet.getLong("parent_type_id")
                    ));

            if (serviceTypes.isEmpty()) {
                ResponseWrapper<List<serviceType_entity>> notFoundResponse = new ResponseWrapper<>("Service types not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }

            ResponseWrapper<List<serviceType_entity>> responseWrapper = new ResponseWrapper<>("Success", serviceTypes);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<List<serviceType_entity>> responseWrapper = new ResponseWrapper<>("An error occurred while retrieving the service types", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @GetMapping("/serviceType/{parent_id}")
    public ResponseEntity<ResponseWrapper<List<serviceType_entity>>> getServiceTypeByParentID(
            @PathVariable Long parent_id,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<List<serviceType_entity>> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Retrieve service types by parent ID using SQL
            String selectServiceTypesSql = "SELECT * FROM tb_service_type WHERE parent_type_id = ?";
            List<serviceType_entity> serviceTypes = jdbcTemplate.query(selectServiceTypesSql,
                    new Object[]{parent_id},
                    (resultSet, rowNum) -> new serviceType_entity(
                            resultSet.getLong("serviceType_id"),
                            resultSet.getString("type_name"),
                            resultSet.getString("serviceType_icon"),
                            resultSet.getLong("parent_type_id")
                    ));

            if (serviceTypes.isEmpty()) {
                ResponseWrapper<List<serviceType_entity>> notFoundResponse = new ResponseWrapper<>("Service types not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }

            ResponseWrapper<List<serviceType_entity>> responseWrapper = new ResponseWrapper<>("Success", serviceTypes);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<List<serviceType_entity>> responseWrapper = new ResponseWrapper<>("An error occurred while retrieving the service types", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @GetMapping("/serviceType/findById/{serviceTypeId}")
    public ResponseEntity<ResponseWrapper<serviceType_entity>> getServiceTypeById(
            @PathVariable Long serviceTypeId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<serviceType_entity> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Retrieve service type by ID using SQL
            String selectServiceTypeByIdSql = "SELECT * FROM tb_service_type WHERE serviceType_id = ?";
            serviceType_entity serviceType = null;
            try {
                serviceType = jdbcTemplate.queryForObject(selectServiceTypeByIdSql,
                        new Object[]{serviceTypeId},
                        (resultSet, rowNum) -> new serviceType_entity(
                                resultSet.getLong("serviceType_id"),
                                resultSet.getString("type_name"),
                                resultSet.getString("serviceType_icon"),
                                resultSet.getLong("parent_type_id")
                        ));
            } catch (EmptyResultDataAccessException ignored) {
                // Handle the case where no rows were found
            }

            if (serviceType == null) {
                ResponseWrapper<serviceType_entity> notFoundResponse = new ResponseWrapper<>("Service type not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }

            ResponseWrapper<serviceType_entity> responseWrapper = new ResponseWrapper<>("Success", serviceType);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<serviceType_entity> responseWrapper = new ResponseWrapper<>("An error occurred while retrieving the service type", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @PutMapping("/serviceType/update/{serviceTypeId}")
    public ResponseEntity<ResponseWrapper<String>> updateServiceType(
            @PathVariable Long serviceTypeId,
            @RequestBody serviceType_entity updatedServiceType,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Check if the service type exists
            serviceType_entity existingServiceType = getServiceTypeById(serviceTypeId);
            System.out.println(existingServiceType.getServiceType_icon());
            if (existingServiceType == null) {
                ResponseWrapper<String> notFoundResponse = new ResponseWrapper<>("Service type not found", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundResponse);
            }

            // Update the service type fields
            existingServiceType.setType_name(updatedServiceType.getType_name());
            existingServiceType.setParent_type_id(updatedServiceType.getParent_type_id());
            existingServiceType.setServiceType_icon(updatedServiceType.getServiceType_icon());

            // Update service type using SQL
            String updateServiceTypeSql = "UPDATE tb_service_type SET type_name = ?, parent_type_id = ?,serviceType_icon=? WHERE serviceType_id = ?";
            jdbcTemplate.update(updateServiceTypeSql, existingServiceType.getType_name(), existingServiceType.getParent_type_id(), existingServiceType.getServiceType_icon(),serviceTypeId);

            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Service type updated successfully", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while updating the service type", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }

    private serviceType_entity getServiceTypeById(Long serviceTypeId) {
        String selectServiceTypeByIdSql = "SELECT * FROM tb_service_type WHERE serviceType_id = ?";
        try {
            return jdbcTemplate.queryForObject(selectServiceTypeByIdSql,
                    new Object[]{serviceTypeId},
                    (resultSet, rowNum) -> new serviceType_entity(
                            resultSet.getLong("serviceType_id"),
                            resultSet.getString("type_name"),
                            resultSet.getString("serviceType_icon"),
                            resultSet.getLong("parent_type_id")
                    ));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    @DeleteMapping("/serviceType/delete/{serviceTypeId}")
    public ResponseEntity<ResponseWrapper<String>> deleteServiceType(
            @PathVariable Long serviceTypeId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<?> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Check if there are references to the service type in tb_service
            String checkReferencesSql = "SELECT COUNT(*) FROM tb_service WHERE serviceType_id = ?";
            int referencesCount = jdbcTemplate.queryForObject(checkReferencesSql, Integer.class, serviceTypeId);

            if (referencesCount > 0) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Cannot delete service type with existing references in tb_service.", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
            }

            // Delete service type using SQL
            String deleteServiceTypeSql = "DELETE FROM tb_service_type WHERE serviceType_id = ?";
            jdbcTemplate.update(deleteServiceTypeSql, serviceTypeId);

            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Service type deleted successfully", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while deleting the service type", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
}
