package kata.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kata.demo.dto.Account;
import kata.demo.dto.AccountType;
import kata.demo.dto.Statement;
import kata.demo.dto.StatementType;
import kata.demo.service.AccountService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {
    @Autowired
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    @DisplayName("POST /account - Success")
    void testCreateAccount() throws Exception {
        Account postAccount = Account.builder()
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.TEN).build();
        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(postAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(org.hamcrest.core.Is.isA(String.class)))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.TEN));
    }

    @Test
    @DisplayName("GET /account - Success")
    void testGetExistingAccount() throws Exception {
        Account saved = populateAnAccount();
        mockMvc.perform(get("/account/" + saved.getId()))
                .andExpect(status().isOk());

    }

    private Account populateAnAccount() {
        Account anAccount = Account.builder()
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.TEN).build();
        return accountService.save(anAccount);
    }

    private Account populateAnAccountWithStatement() {
        Statement statement = Statement.builder()
                .type(StatementType.DEPOSIT)
                .amount(BigDecimal.TEN)
                .build();
        Account anAccount = Account.builder()
                .type(AccountType.CHECKING)
                .statements(List.of(statement))
                .balance(BigDecimal.TEN).build();
        return accountService.save(anAccount);
    }

    @Test
    @DisplayName("GET /account - NotFound")
    void testGetNotValidAccount() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/account/" + id))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("POST Deposit /account/{id}/statements - Success")
    void testDepositToAccount() throws Exception {
        Account account = populateAnAccountWithStatement();
        Statement statement = Statement.builder()
                .amount(BigDecimal.valueOf(11))
                .type(StatementType.DEPOSIT)
                .build();


        mockMvc.perform(post("/account/" + account.getId() + "/statements")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(statement))
//                        .header("If-Match", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(account.getId().toString()))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(21)))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(2)))
        ;

    }

    @Test
    @DisplayName("POST Withdrawal /account/{id}/statements - Success")
    void testWithdrawalFromAccount() throws Exception {
        Account account = populateAnAccountWithStatement();
        Statement statement = Statement.builder()
                .amount(BigDecimal.valueOf(10))
                .type(StatementType.WITHDRAWAL)
                .build();


        mockMvc.perform(post("/account/" + account.getId() + "/statements")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(statement))
//                        .header("If-Match", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(account.getId().toString()))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(2)))
        ;

    }


    @Test
    @DisplayName("POST Deposit concurrently /account/{id}/statements - Success")
    void testDepositToAccountConcurrently() throws Exception {
        Account account = populateAnAccountWithStatement();
        List<Statement> statements = new ArrayList<>();
        for (int i = 10; i < 101; i += 10) {
            statements.add(Statement.builder()
                    .type(StatementType.DEPOSIT)
                    .amount(BigDecimal.valueOf(i))
                    .build());
        }
        statements.parallelStream().forEach(statement -> {
            try {
                mockMvc.perform(post("/account/" + account.getId() + "/statements")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(objectMapper.writeValueAsString(statement))
                                //                        .header("If-Match", "1")
                        )
                        .andExpect(status().isCreated());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        mockMvc.perform(get("/account/" + account.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(560));

    }
}
