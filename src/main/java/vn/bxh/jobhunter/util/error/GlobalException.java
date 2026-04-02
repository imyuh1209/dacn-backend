package vn.bxh.jobhunter.util.error;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import vn.bxh.jobhunter.domain.response.RestResponse;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = {UsernameNotFoundException.class,
            BadCredentialsException.class,
            IdInvalidException.class,
            MethodArgumentTypeMismatchException.class}
    )
    public ResponseEntity<RestResponse<Object>> handleBlogAlreadyExistsException(Exception idException) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(idException.getMessage());
        
        // Match Selenium expectations for authentication errors
        if (idException instanceof BadCredentialsException || idException.getMessage().contains("Email không hợp lệ")) {
            res.setMessage("sai email hoặc mật khẩu hoặc không hợp lệ!");
        } else {
            res.setMessage(idException.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
    @ExceptionHandler(value = {FileInvalidException.class}
    )
    public ResponseEntity<RestResponse<Object>> handleFileException(Exception idException) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(idException.getMessage());
        res.setMessage("File Invalid Exception");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<RestResponse<Object>> validationError(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        final List<FieldError> fieldErrors = result.getFieldErrors();

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getBody().getDetail());

        // Join multiple error messages if they exist, or just take the first one
        List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).toList();
        String finalMessage = errors.size() > 1 ? String.join(", ", errors) : (errors.isEmpty() ? "Validation error" : errors.get(0));
        res.setMessage(finalMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }



}
