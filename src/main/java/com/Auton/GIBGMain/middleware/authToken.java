package com.Auton.GIBGMain.middleware;

import com.Auton.GIBGMain.Response.ResponseWrapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class authToken {
    @Value("${jwt_secret}")
    private String jwt_secret;

    public ResponseEntity<ResponseWrapper<Void>> validateAuthorizationHeader(String authorizationHeader) {
        try {
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
            Long authenticatedUserId = claims.get("user_id", Long.class);
            Long roleId = claims.get("role_id", Long.class);
            String role = claims.get("role_name", String.class);
            // Check if the authenticated user has the appropriate role to perform this action (e.g., admin)
            if (roleId !=1  && roleId !=2 ) {
                ResponseWrapper<Void> responseWrapper = new ResponseWrapper<>("You are not authorized to perform this action.", null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseWrapper);
            }

            // If the authorization is successful, return a ResponseWrapper<Void> with success message
            ResponseWrapper<Void> responseWrapper = new ResponseWrapper<>("Authorization successful.", null);
            return ResponseEntity.ok(responseWrapper);
        } catch (JwtException e) {
            // Handle JWT exception (token verification failure)
            ResponseWrapper<Void> responseWrapper = new ResponseWrapper<>("Token is invalid.", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseWrapper);
        } catch (Exception e) {
            // Handle other exceptions
            String errorMessage = "An error occurred during authorization.";
            ResponseWrapper<Void> responseWrapper = new ResponseWrapper<>(errorMessage, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseWrapper);
        }
    }
}
