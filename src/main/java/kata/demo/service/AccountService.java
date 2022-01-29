package kata.demo.service;

import kata.demo.dto.Account;
import kata.demo.dto.Statement;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AccountService {

    //FIXME Requirement is NO PERSISTENCE, so I have to manually manage the database here with these boilerplate codes....
    private final HashMap<UUID, Account> accountsStorage = new HashMap<>();

    public Account save(Account account) {
        // No persistence so here I need to do all READ/WRITE operations for demo
        Account newAccount = Account.builder()
                .id(account.getId() == null ? UUID.randomUUID() : account.getId())
                .statements(account.getStatements())
                .type(account.getType())
                .balance(account.getBalance()).build();
        accountsStorage.put(newAccount.getId(), newAccount);
        return newAccount;
    }

    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(accountsStorage.get(id));
    }

    public Optional<Account> update(Account account, Statement statement) {
        Optional<Account> byId = findById(account.getId());
        if (byId.isEmpty()) {
            return Optional.empty();
        }

        Account existingAccount = byId.get();
        List<Statement> updatedStatements = new ArrayList<>(existingAccount.getStatements());
        updatedStatements.add(statement);
        BigDecimal updatedBalance = statement.applyStatement(existingAccount.getBalance());
        Account updated = Account.builder()
                .type(existingAccount.getType())
                .id(existingAccount.getId())
                .statements(updatedStatements)
                .balance(updatedBalance)
                .build();
        return Optional.of(updated);
    }
}
