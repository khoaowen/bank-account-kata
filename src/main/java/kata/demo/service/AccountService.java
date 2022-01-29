package kata.demo.service;

import kata.demo.dto.Account;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    //FIXME Requirement is NO PERSISTENCE, so I have to manually manage the database here with these boilerplate codes....
    private HashMap<UUID, Account> accountsStorage = new HashMap<>();

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
}
