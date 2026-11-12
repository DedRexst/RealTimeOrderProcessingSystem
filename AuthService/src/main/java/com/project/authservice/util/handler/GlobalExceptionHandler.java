package com.project.authservice.util.handler;

import com.project.authservice.util.exception.BusinessException;
import com.project.authservice.util.exception.ErrorCode;
import com.project.authservice.util.exception.OAuth2Exception;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFound(EntityNotFoundException e) {
    log.debug("Entity not found: {}", e.getMessage());
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusiness(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.getStatus(), e.getMessage());
    problem.setProperty("code", code.name());
    return problem;
  }

  /**
   * The token endpoint answers in OAuth2's own error format rather than as a ProblemDetail — its
   * callers are client libraries that branch on the {@code error} code.
   */
  @ExceptionHandler(OAuth2Exception.class)
  public ResponseEntity<Map<String, String>> handleOAuth2(OAuth2Exception e) {
    log.debug("Token endpoint rejected a request: {} ({})", e.getError(), e.getMessage());
    return ResponseEntity.status(e.getStatus())
        .body(Map.of("error", e.getError(), "error_description", e.getMessage()));
  }

  /** Names the offending fields, which the default 400 body does not. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      errors.merge(error.getField(), error.getDefaultMessage(), (first, second) ->
          first + "; " + second);
    }
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
    problem.setProperty("errors", errors);
    return problem;
  }
}
