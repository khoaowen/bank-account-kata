package kata.demo.exception;

import java.util.UUID;

/**
 * Exception when no account is found with the id
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super("Could not find account " + id);
    }
}
