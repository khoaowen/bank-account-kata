package kata.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kata.demo.dto.Account;
import kata.demo.dto.Statement;
import kata.demo.exception.AccountInsufficientBalance;
import kata.demo.exception.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    //FIXME Requirement is NO PERSISTENCE, so I have to manually manage the database here with these boilerplate codes....
    private final Map<UUID, Account> accountsStorage = new ConcurrentHashMap<>();

    @Value("classpath:sample/account_demo.json")
    Resource accountDemo;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @PostConstruct
    private void initExampleAccount() throws IOException {
        objectMapper.setDateFormat(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"));
        Account account = objectMapper.readValue(accountDemo.getFile(), Account.class);
        accountsStorage.put(account.getId(), account);
    }

    /**
     * Update or create the account. An Id will be generated if it's a new account, otherwise the existing account will be replaced with the new one
     *
     * @param account account to be created or updated
     * @return the account
     */
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

    /**
     * Search for the account by the id
     *
     * @param id the account id
     * @return the account
     */
    public Account findById(UUID id) {
        Account value = accountsStorage.get(id);
        if (value == null) {
            throw new AccountNotFoundException(id);
        }
        return value;
    }

    /**
     * Make a statement/operation to the account, be a withdrawal or a deposit. The account balance will be updated and the statement will be added to its list
     *
     * @param accountId account Id to be updated with the statement
     * @param statement statement to be appliedy to the account, which could be a {@link kata.demo.dto.StatementType#DEPOSIT} or a {@link kata.demo.dto.StatementType#WITHDRAWAL}
     * @return the updated account
     */
    public Account update(UUID accountId, Statement statement) {

        if (!accountsStorage.containsKey(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return accountsStorage.computeIfPresent(accountId, (k, v) -> {
            List<Statement> updatedStatements = new ArrayList<>(v.getStatements());
            updatedStatements.add(statement);
            BigDecimal updatedBalance = statement.applyStatement(v.getBalance());
            if (updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AccountInsufficientBalance();
            }
            return Account.builder()
                    .type(v.getType())
                    .id(v.getId())
                    .statements(updatedStatements)
                    .balance(updatedBalance)
                    .build();
        });
    }
}
