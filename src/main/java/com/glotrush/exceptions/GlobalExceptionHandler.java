package com.glotrush.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.glotrush.dto.response.ErrorResponse;
import com.stripe.exception.StripeException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            WeakPasswordException.class,
            TwoFactorNotEnabledException.class,
            SubscriptionOperationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return buildError(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /* =========================
       401 - UNAUTHORIZED
       ========================= */
    @ExceptionHandler({
            InvalidTokenException.class,
            InvalidTotpCodeException.class,
            BadCredentialsException.class,
            ExpiredJwtException.class,
            MalformedJwtException.class,
            SignatureException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex) {
        return buildError(resolveMessage(ex), HttpStatus.UNAUTHORIZED);
    }

    /* =========================
       404 - NOT FOUND
       ========================= */
    @ExceptionHandler({
            UserNotFoundException.class,
            UsernameNotFoundException.class,
            SubscriptionNotFoundException.class,
            PlanNotFoundException.class,
            LessonNotFoundException.class,
            TopicNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        return buildError(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /* =========================
       409 - CONFLICT
       ========================= */
    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            UsernameAlreadyExistsException.class,
            TwoFactorAlreadyEnabledException.class,
            SubscriptionAlreadyExistException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return buildError(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /* =========================
       423 - LOCKED
       ========================= */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(AccountLockedException ex) {
        return buildError(ex.getMessage(), HttpStatus.LOCKED);
    }

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<ErrorResponse> handleStripeException(StripeException ex) {
        return buildError(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("JSON parse error: ", ex);
        String message = "Invalid request body";
        if (ex.getMessage() != null && ex.getMessage().contains("Could not resolve subtype")) {
            message = "Invalid or missing lessonType";
        }
        return buildError(message, HttpStatus.BAD_REQUEST);
    }

    /* =========================
       403 - FORBIDDEN
       ========================= */
    @ExceptionHandler(PasswordExpiredException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(PasswordExpiredException ex) {
        return buildError(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildError(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return buildError("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildError(String message, HttpStatus status) {
        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(response);
    }

    private String resolveMessage(Exception ex) {
        if (ex instanceof BadCredentialsException) {
            return "Invalid email or password";
        }
        return ex.getMessage();
    }
}
