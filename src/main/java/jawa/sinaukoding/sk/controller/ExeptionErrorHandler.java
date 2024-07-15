package jawa.sinaukoding.sk.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import jawa.sinaukoding.sk.model.Response;

@ControllerAdvice
public class ExeptionErrorHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<?>> handleError(Throwable e, WebRequest request){
        ResponseEntity<Response<?>> response = new ResponseEntity<>(Response.badRequest(),HttpStatusCode.valueOf(400));
        return response;
    }
}
