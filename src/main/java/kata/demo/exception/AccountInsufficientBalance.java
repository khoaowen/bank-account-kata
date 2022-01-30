package kata.demo.exception;

/**
 * Exception when a statement on an account can make its balance negative
 */
public class AccountInsufficientBalance extends RuntimeException {
    public AccountInsufficientBalance() {
        super("Account has no sufficient balance to do the statement");
    }
}
