package sk.eea.td.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import sk.eea.td.rest.model.RestErrorResponse;

@ControllerAdvice(basePackages = "sk.eea.td.rest.controller")
public class RestErrorHandler {

    private MessageSource messageSource;

    @Autowired
    public RestErrorHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(value = { MethodArgumentNotValidException.class, HttpMessageNotReadableException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public RestErrorResponse processValidationError(Exception ex) {
        RestErrorResponse response = new RestErrorResponse();
        response.setMessage(ex.getLocalizedMessage());
        return response;
    }
}
