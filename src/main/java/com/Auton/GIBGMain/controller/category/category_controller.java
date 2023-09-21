package com.Auton.GIBGMain.controller.category;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import com.Auton.GIBGMain.entity.category.category_entity;
import com.Auton.GIBGMain.middleware.authToken;
import com.Auton.GIBGMain.repository.category.category_repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/shop/category")
public class category_controller {
    private final category_repository categoryRepository;
    private final authToken authService;

    @Autowired
    public category_controller(category_repository categoryRepository , authToken authService) {
        this.categoryRepository = categoryRepository;
        this.authService = authService;
    }
    @PostMapping("/insert")
    public ResponseEntity<ResponseWrapper<String>> addCategory(@RequestBody category_entity category,
                                                               @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            System.out.println(authResponse.getStatusCode());
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            if (category.getCategory_name() == null || category.getCategory_name().isEmpty()) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Category name is missing or empty.", null);
                return ResponseEntity.ok(responseWrapper);
            }

            // Save the category using the repository
            categoryRepository.save(category);

            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Category added successfully", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while adding the category", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @GetMapping("/findById/{categoryId}")
    public ResponseEntity<ResponseWrapper<category_entity>> getCategoryById(@PathVariable long categoryId,
                                                                            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<category_entity> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Find the category by ID
            Optional<category_entity> categoryOptional = categoryRepository.findById(categoryId);
            if (categoryOptional.isPresent()) {
                category_entity category = categoryOptional.get();
                ResponseWrapper<category_entity> responseWrapper = new ResponseWrapper<>("Category found.", category);
                return ResponseEntity.ok(responseWrapper);
            } else {
                ResponseWrapper<category_entity> responseWrapper = new ResponseWrapper<>("Category not found.", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseWrapper);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<category_entity> responseWrapper = new ResponseWrapper<>("An error occurred while fetching the category", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @GetMapping("/all")
    public ResponseEntity<ResponseWrapper<List<category_entity>>> getAllCategories(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<List<category_entity>> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Fetch all categories from the repository
            List<category_entity> categories = categoryRepository.findAll();

            // Wrap the categories in a ResponseWrapper and return
            ResponseWrapper<List<category_entity>> responseWrapper = new ResponseWrapper<>("Categories fetched successfully", categories);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<List<category_entity>> responseWrapper = new ResponseWrapper<>("An error occurred while fetching categories", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @PutMapping("/update/{categoryId}")
    public ResponseEntity<ResponseWrapper<String>> updateCategory(@PathVariable Long categoryId,
                                                                  @RequestBody category_entity updatedCategory,
                                                                  @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Fetch the existing category from the repository by its ID
            Optional<category_entity> existingCategoryOptional = categoryRepository.findById(categoryId);
            if (existingCategoryOptional.isEmpty()) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Category not found.", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseWrapper);
            }

            // Update the properties of the existing category with the properties of the updatedCategory
            category_entity existingCategory = existingCategoryOptional.get();
            existingCategory.setCategory_name(updatedCategory.getCategory_name());
            existingCategory.setDescription(updatedCategory.getDescription());
            // Set other properties as needed

            // Save the updated category
            categoryRepository.save(existingCategory);

            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Category updated successfully", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while updating the category", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<ResponseWrapper<String>> deleteCategory(@PathVariable Long categoryId,
                                                                  @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Validate authorization using authService
            ResponseEntity<ResponseWrapper<Void>> authResponse = authService.validateAuthorizationHeader(authorizationHeader);
            if (authResponse.getStatusCode() != HttpStatus.OK) {
                // Token is invalid or has expired
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
            }

            // Check if the category exists
            if (!categoryRepository.existsById(categoryId)) {
                ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Category not found.", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseWrapper);
            }

            // Delete the category by ID
            categoryRepository.deleteById(categoryId);

            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("Category deleted successfully", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseWrapper<String> responseWrapper = new ResponseWrapper<>("An error occurred while deleting the category", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
}
