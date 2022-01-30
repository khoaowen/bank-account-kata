package kata.demo.exception;

public class AccountInsufficientBalance extends RuntimeException {
    public AccountInsufficientBalance() {
        super("Account has no sufficient balance to do the statement");
    }
}
