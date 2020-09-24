package com.arqaam.logframelab.advice;

import com.arqaam.logframelab.model.Error;
import com.arqaam.logframelab.model.properties.ErrorProperties;
import com.arqaam.logframelab.util.Logging;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@ControllerAdvice(basePackages = "com.arqaam.logframelab")
public class GlobalExceptionHandler implements Logging {

    private final ErrorProperties properties;

    public GlobalExceptionHandler(ErrorProperties properties) {
        this.properties = properties;
    }

    @ExceptionHandler
    public ResponseEntity<Error> ExceptionHandler(Exception e, HttpServletRequest request) {
        ErrorProperties.ErrorProp DEFAULT_ERROR_PROP = new ErrorProperties.ErrorProp();
        DEFAULT_ERROR_PROP.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        DEFAULT_ERROR_PROP.setMessage("Unexpected Error");
        DEFAULT_ERROR_PROP.setCode(0);
        ErrorProperties.ErrorProp errorProp = getError(e.getClass());

        if (isNull(errorProp)) {
            logger().error("An unexpected error occurred!", e);
            errorProp = DEFAULT_ERROR_PROP;
        }

        String message = nonNull(e.getMessage()) ? e.getMessage() : (nonNull(errorProp.getMessage()) ? errorProp.getMessage() : DEFAULT_ERROR_PROP.getMessage());
        Error error_body = Error.builder()
                .exception(isNull(errorProp.getExceptionName()) ? e.getClass().getSimpleName() : errorProp.getExceptionName())
                .code(errorProp.getCode())
                .message(message)
                .timestamp(OffsetDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")))
                .build();
        return ResponseEntity
                .status(isNull(errorProp.getHttpStatus()) ? HttpStatus.INTERNAL_SERVER_ERROR.value() : errorProp.getHttpStatus())
                .body(error_body);

    }

    private ErrorProperties.ErrorProp getError(Class<?> _class) {

        ErrorProperties.ErrorProp errorProp;

        while (!_class.equals(Exception.class)) {
            errorProp = properties.getErrors().get(_class.getSimpleName());

            if (!isNull(errorProp)) {
                return errorProp;
            }

            for (Class<?> interf : _class.getInterfaces()) {
                errorProp = properties.getErrors().get(interf.getSimpleName());

                if (!isNull(errorProp)) {
                    return errorProp;
                }
            }
            _class = _class.getSuperclass();
        }

        return null;
    }

}