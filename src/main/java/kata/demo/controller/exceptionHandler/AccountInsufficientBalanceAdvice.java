package kata.demo.controller.exceptionHandler;

import kata.demo.exception.AccountInsufficientBalance;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AccountInsufficientBalanceAdvice {
    @ResponseBody
    @ExceptionHandler(AccountInsufficientBalance.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String employeeNotFoundHandler(AccountInsufficientBalance ex) {
        return ex.getMessage();
    }
}
