package com.Auton.GIBGMain.controller.imageUpload;

import com.Auton.GIBGMain.Response.ImageUploadResponse;
import com.Auton.GIBGMain.middleware.authToken;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/api/admin")
public class imageUpload_controller {
    private final authToken authService;
    @Value("${upload.dir}")
    private String uploadDir;
    @Value("${uploadFilePro.dir}")
    private String uploadProDir;

    @Autowired
    public imageUpload_controller(authToken authService) {
        this.authService = authService;

    }
    @PostMapping("/user/profile/upload")
    public ResponseEntity<ImageUploadResponse> uploadImageToCloudinary(@RequestParam("imageFile") MultipartFile imageFile, @RequestHeader("Authorization") String authorizationHeader) {
        ImageUploadResponse response = new ImageUploadResponse();

        try {
            if (imageFile.isEmpty()) {
                response.setMessage("Image file is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }


            // Check image size
            if (imageFile.getSize() > 1024 * 1024 * 9) { // 9MB
                response.setMessage("Image size must be less than 9MB");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Generate a random name for the uploaded image
            String fileExtension = imageFile.getOriginalFilename().substring(imageFile.getOriginalFilename().lastIndexOf("."));
            String randomFileName = "profile_" + System.currentTimeMillis() + fileExtension;

            // Initialize Cloudinary instance
            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", "ds639zn4t", "api_key", "297389628948212", "api_secret", "gyTMSMSLKyKjVG6A_IC3bhrHUvE"));

            // Resize and upload image to Cloudinary
            Map<String, Object> resizeParams = ObjectUtils.asMap(
                    "transformation", new Transformation().width(600).height(600).crop("limit")
            );
            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), resizeParams);

            // Get the Cloudinary URL of the uploaded image
            String imageUrl = (String) uploadResult.get("secure_url");

            response.setMessage("Image uploaded successfully");
            response.setUrlPath(imageUrl);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            response.setMessage("Error uploading image");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
