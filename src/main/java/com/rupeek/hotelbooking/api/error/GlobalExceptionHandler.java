package com.rupeek.hotelbooking.api.error;

import com.rupeek.hotelbooking.domain.booking.BookingHoldExpiredException;
import com.rupeek.hotelbooking.domain.common.BusinessRuleViolationException;
import com.rupeek.hotelbooking.domain.common.InvalidStateTransitionException;
import com.rupeek.hotelbooking.domain.common.ResourceNotFoundException;
import com.rupeek.hotelbooking.domain.inventory.InsufficientInventoryException;
import com.rupeek.hotelbooking.domain.payment.IdempotencyKeyConflictException;
import com.rupeek.hotelbooking.domain.payment.PaymentGatewayException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Translates domain failures into RFC 7807 problem responses. The HTTP mapping lives only here; the domain
 * exceptions know nothing about HTTP.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex);
    }

    @ExceptionHandler({
            InvalidStateTransitionException.class,
            InsufficientInventoryException.class,
            BookingHoldExpiredException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict with current state", ex);
    }

    @ExceptionHandler({BusinessRuleViolationException.class, IdempotencyKeyConflictException.class})
    public ProblemDetail handleBusinessRule(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated", ex);
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ProblemDetail handleGateway(PaymentGatewayException ex) {
        return problem(HttpStatus.BAD_GATEWAY, "Payment provider error", ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, this::messageOf, (first, second) -> first));
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        body.setTitle("Invalid request");
        body.setProperty("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    private String messageOf(FieldError error) {
        return error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage();
    }

    private static ProblemDetail problem(HttpStatus status, String title, RuntimeException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        body.setTitle(title);
        return body;
    }
}
