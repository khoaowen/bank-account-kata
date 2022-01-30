package kata.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kata.demo.dto.Account;
import kata.demo.dto.AccountType;
import kata.demo.dto.Statement;
import kata.demo.dto.StatementType;
import kata.demo.exception.AccountInsufficientBalance;
import kata.demo.exception.AccountNotFoundException;
import kata.demo.service.AccountService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    @Test
    @DisplayName("POST /account - Success")
    void testCreateAccount() throws Exception {
        UUID id = UUID.randomUUID();
        Account postAccount = Account.builder()
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.ZERO).build();
        Account mockedAccount = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.ZERO).build();
        when(accountService.save(ArgumentMatchers.any(Account.class))).thenReturn(mockedAccount);

        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(postAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                .andExpect(redirectedUrlPattern("http://*/account/" + id));
    }

    @Test
    @DisplayName("GET /account - Success")
    void testGetExistingAccount() throws Exception {
        UUID id = UUID.randomUUID();
        Account mockedAccount = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.ZERO).build();
        when(accountService.findById(id)).thenReturn(mockedAccount);
        mockMvc.perform(get("/account/" + id))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrlPattern("http://*/account/" + id));

    }

    @Test
    @DisplayName("GET /account - NotFound")
    void testGetNotValidAccount() throws Exception {
        UUID id = UUID.randomUUID();
        when(accountService.findById(id)).thenThrow(new AccountNotFoundException(id));
        mockMvc.perform(get("/account/" + id))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("POST Deposit /account/{id}/statements - Success")
    void testDespositToAccount() throws Exception {
        UUID id = UUID.randomUUID();
        Account existingAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.ONE).build();
        when(accountService.findById(id)).thenReturn(existingAcc);
        Statement statementPostBody = Statement.builder()
                .amount(BigDecimal.valueOf(11))
                .type(StatementType.DEPOSIT)
                .build();
        Statement timeStampStatement = Statement.builder()
                .date(LocalDateTime.of(2022, 01, 03, 11, 30, 29))
                .amount(statementPostBody.getAmount())
                .type(statementPostBody.getType())
                .build();
        Account updatedAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of(timeStampStatement))
                .balance(BigDecimal.valueOf(11)).build();
        when(accountService.update(any(Account.class), any(Statement.class))).thenReturn(updatedAcc);


        mockMvc.perform(post("/account/" + id + "/statements")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(statementPostBody))
//                        .header("If-Match", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(11)))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.statements[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.statements[0].amount").value("11"))
                .andExpect(jsonPath("$.statements[0].date").value("03/01/2022 11:30:29"))
        ;

    }

    @Test
    @DisplayName("POST Withdrawal /account/{id}/statements - Success")
    void testWithdrawalFromAccount() throws Exception {
        UUID id = UUID.randomUUID();
        Account existingAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.valueOf(50)).build();
        when(accountService.findById(id)).thenReturn(existingAcc);
        Statement statementPostBody = Statement.builder()
                .amount(BigDecimal.valueOf(11))
                .type(StatementType.WITHDRAWAL)
                .build();
        Statement timeStampStatement = Statement.builder()
                .date(LocalDateTime.of(2022, 01, 03, 11, 30, 29))
                .amount(statementPostBody.getAmount())
                .type(statementPostBody.getType())
                .build();
        Account updatedAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of(timeStampStatement))
                .balance(BigDecimal.valueOf(39)).build();
        when(accountService.update(any(Account.class), any(Statement.class))).thenReturn(updatedAcc);


        mockMvc.perform(post("/account/" + id + "/statements")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(statementPostBody))
//                        .header("If-Match", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(39)))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.statements[0].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.statements[0].amount").value("11"))
                .andExpect(jsonPath("$.statements[0].date").value("03/01/2022 11:30:29"))
        ;
    }

    @Test
    @DisplayName("POST Withdrawal /account/{id}/statements - Not sufficient Balance")
    void testWithdrawalFromAccountUnsuccessful() throws Exception {
        UUID id = UUID.randomUUID();
        Account existingAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.valueOf(50)).build();
        when(accountService.findById(id)).thenReturn(existingAcc);
        Statement statementPostBody = Statement.builder()
                .amount(BigDecimal.valueOf(500))
                .type(StatementType.WITHDRAWAL)
                .build();

        when(accountService.update(any(Account.class), any(Statement.class))).thenThrow(new AccountInsufficientBalance());

        mockMvc.perform(post("/account/" + id + "/statements")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(statementPostBody))
//                        .header("If-Match", "1")
                )
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("GET printStatements with pagesize 2 /account/{id}/statements - Success")
    void testGetStatementsPagesSize2() throws Exception {
        UUID id = UUID.randomUUID();
        List<Statement> statements = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            statements.add(Statement.builder()
                    .date(LocalDateTime.now())
                    .type(i % 2 == 0 ? StatementType.DEPOSIT : StatementType.WITHDRAWAL)
                    .amount(BigDecimal.valueOf(i))
                    .build());
        }
        Account existingAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(statements)
                .balance(BigDecimal.valueOf(50)).build();
        when(accountService.findById(id)).thenReturn(existingAcc);
        mockMvc.perform(get("/account/" + id + "/statements")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountBalance").value(BigDecimal.valueOf(50)))
                .andExpect(jsonPath("$.currentPage").value(BigDecimal.valueOf(0)))
                .andExpect(jsonPath("$.totalPages").value(BigDecimal.valueOf(5)))
                .andExpect(jsonPath("$.totalStatements").value(10))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(2)))
        ;
    }

    @Test
    @DisplayName("GET printStatements with order by date /account/{id}/statements - Success")
    void testGetStatementsPagesOrderByDate() throws Exception {
        UUID id = UUID.randomUUID();
        List<Statement> statements = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            statements.add(Statement.builder()
                    .date(LocalDateTime.now())
                    .type(i % 2 == 0 ? StatementType.DEPOSIT : StatementType.WITHDRAWAL)
                    .amount(BigDecimal.valueOf(i))
                    .build());
        }
        Account existingAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(statements)
                .balance(BigDecimal.valueOf(50)).build();
        when(accountService.findById(id)).thenReturn(existingAcc);
        mockMvc.perform(get("/account/" + id + "/statements")
                        .param("size", "5")
                        .param("sort", "date,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountBalance").value(BigDecimal.valueOf(50)))
                .andExpect(jsonPath("$.currentPage").value(BigDecimal.valueOf(0)))
                .andExpect(jsonPath("$.totalPages").value(BigDecimal.valueOf(2)))
                .andExpect(jsonPath("$.totalStatements").value(10))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(5)))
                .andExpect(jsonPath("$.statements[0].amount").value(10))
        ;
    }

}