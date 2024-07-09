package swp.group2.swpbe.exception;

import org.springframework.http.HttpStatus;

public class ApiRequestException extends RuntimeException {
    public HttpStatus httpStatus;
    public ApiRequestException(String message, HttpStatus httpStatus ) {
        super(message);
        this.httpStatus= httpStatus;
    }
    public ApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}

  