package com.inventrik.digitalestore.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AuthorizationErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        // Get current user's authentication details
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth != null ? auth.getName() : "anonymous";
        String currentRoles = auth != null ? auth.getAuthorities().toString() : "[]";
        
        // Extract role information from the exception message if available
        String detailedMessage = "Access denied. Your current role does not have permission to access this resource.";
        String requiredRole = "Unknown";
        
        if (ex.getMessage() != null && ex.getMessage().contains("hasRole")) {
            // Try to extract required role from the exception message
            String exceptionMsg = ex.getMessage();
            if (exceptionMsg.contains("ROLE_ADMIN")) {
                detailedMessage = "Access denied. This operation requires ADMIN privileges.";
                requiredRole = "ROLE_ADMIN";
            } else if (exceptionMsg.contains("ROLE_USER")) {
                detailedMessage = "Access denied. This operation requires USER privileges.";
                requiredRole = "ROLE_USER";
            } else if (exceptionMsg.contains("ROLE_MANAGER")) {
                detailedMessage = "Access denied. This operation requires MANAGER privileges.";
                requiredRole = "ROLE_MANAGER";
            }
        }
        
        AuthorizationErrorResponse errorResponse = new AuthorizationErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                detailedMessage,
                LocalDateTime.now(),
                currentUser,
                currentRoles,
                requiredRole);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        
        String userFriendlyMessage = "A data conflict occurred.";
        
        // Extract meaningful information from the exception message
        String exceptionMessage = ex.getMessage();
        if (exceptionMessage != null) {
            if (exceptionMessage.contains("uk_tenants_subdomain")) {
                userFriendlyMessage = "A tenant with this subdomain already exists. Please choose a different subdomain.";
            } else if (exceptionMessage.contains("uk_tenants_shop_email")) {
                userFriendlyMessage = "A tenant with this email address already exists. Please use a different email.";
            } else if (exceptionMessage.contains("uk_tenants_domain_name")) {
                userFriendlyMessage = "A tenant with this domain name already exists. Please choose a different domain.";
            } else if (exceptionMessage.contains("uk_store_themes_theme_name")) {
                userFriendlyMessage = "A theme with this name already exists for this tenant. Please choose a different theme name.";
            } else if (exceptionMessage.contains("Duplicate entry")) {
                // Extract the duplicate value if possible
                if (exceptionMessage.contains("Duplicate entry '") && exceptionMessage.contains("' for key")) {
                    String duplicateValue = extractDuplicateValue(exceptionMessage);
                    String constraintName = extractConstraintName(exceptionMessage);
                    userFriendlyMessage = String.format("The value '%s' is already in use. Please choose a different value.", duplicateValue);
                }
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                userFriendlyMessage,
                LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
    private String extractDuplicateValue(String message) {
        try {
            int startIndex = message.indexOf("Duplicate entry '") + 17;
            int endIndex = message.indexOf("'", startIndex);
            return message.substring(startIndex, endIndex);
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    private String extractConstraintName(String message) {
        try {
            int startIndex = message.indexOf("for key '") + 9;
            int endIndex = message.indexOf("'", startIndex);
            return message.substring(startIndex, endIndex);
        } catch (Exception e) {
            return "unknown";
        }
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                LocalDateTime.now(),
                errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the full exception details for debugging (server-side only)
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please contact support if the issue persists.",
                LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    // Generic error response class
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
        
        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public int getStatus() {
            return status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
    
    // Error response for validation errors with field details
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> fieldErrors;
        
        public ValidationErrorResponse(int status, String message, LocalDateTime timestamp, Map<String, String> fieldErrors) {
            super(status, message, timestamp);
            this.fieldErrors = fieldErrors;
        }
        
        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }
    }
    
    // Error response for authorization failures with role details
    public static class AuthorizationErrorResponse extends ErrorResponse {
        private String currentUser;
        private String currentRoles;
        private String requiredRole;
        
        public AuthorizationErrorResponse(int status, String message, LocalDateTime timestamp, 
                                        String currentUser, String currentRoles, String requiredRole) {
            super(status, message, timestamp);
            this.currentUser = currentUser;
            this.currentRoles = currentRoles;
            this.requiredRole = requiredRole;
        }
        
        public String getCurrentUser() {
            return currentUser;
        }
        
        public String getCurrentRoles() {
            return currentRoles;
        }
        
        public String getRequiredRole() {
            return requiredRole;
        }
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        // Return a 404 status instead of a 500 error
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                LocalDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}