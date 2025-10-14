package com.gsm.exception;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A centralized exception handling class for the entire application.
 * Any exception thrown from a Controller will be caught here.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException (HTTP 404).
     * This method is primarily for API calls, returning a structured JSON error response.
     * @param ex The exception that was thrown.
     * @param request The current web request.
     * @return A ResponseEntity containing the JSON error body and 404 status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value()); // 404
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", ((ServletWebRequest)request).getRequest().getRequestURI());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Catches and handles DuplicateResourceException (HTTP 409).
     * Instead of showing an error page, it redirects back to the form and displays a message.
     * @param ex The exception that was thrown.
     * @param request Used to get the URL of the page that caused the error.
     * @param redirectAttributes Used to pass an error message across a redirect.
     * @return A redirect command.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        // Get the URL of the previous page and redirect back to it.
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Catches all other exceptions (fallback) to prevent the application from crashing (HTTP 500).
     * @param ex The exception that was thrown.
     * @param model Used to pass error information to the view page.
     * @return The name of the 500 error view page.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        ex.printStackTrace(); // Print stack trace to console for debugging.
        model.addAttribute("errorMessage", "An unexpected error occurred. Please contact support.");
        model.addAttribute("errorStatus", "500 Internal Server Error");
        return "error/error_page";
    }

    /**
     * Handles authorization failures (HTTP 403 Forbidden) for API endpoints.
     * This method catches Spring Security's {@link AccessDeniedException} and returns a standardized JSON error response,
     * which is useful for clients like the Zalo Mini App.
     * @param ex The AccessDeniedException thrown when authorization fails.
     * @return A ResponseEntity with an HTTP 403 Forbidden status and a JSON body containing the error message.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, String> response = Map.of("message", "You do not have permission to access this function.");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}