package kata.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kata.demo.dto.Account;
import kata.demo.dto.AccountType;
import kata.demo.dto.Statement;
import kata.demo.dto.StatementType;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    private ObjectMapper objectMapper = new ObjectMapper();

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
        when(accountService.findById(id)).thenReturn(Optional.of(mockedAccount));
        mockMvc.perform(get("/account/" +id ))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrlPattern("http://*/account/" + id));

    }

    @Test
    @DisplayName("GET /account - NotFound")
    void testGetNotValidAccount() throws Exception {
        UUID id = UUID.randomUUID();
        when(accountService.findById(id)).thenReturn(Optional.empty());
        mockMvc.perform(get("/account/" + id))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("POST Deposit /account/{id}/statements - Success")
    void testDispositToAccount() throws Exception {
        UUID id = UUID.randomUUID();
        Account existingAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of())
                .balance(BigDecimal.ONE).build();
        when(accountService.findById(id)).thenReturn(Optional.of(existingAcc));
        Statement statement = Statement.builder()
                .amount(BigDecimal.valueOf(11))
                .type(StatementType.DEPOSIT)
                .build();
        Account updatedAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of(statement))
                .balance(BigDecimal.valueOf(11)).build();
        when(accountService.update(existingAcc, statement)).thenReturn(Optional.of(updatedAcc));


        mockMvc.perform(post("/account/" + id + "/statements")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(statement))
//                        .header("If-Match", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(11)))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(1)))
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
        when(accountService.findById(id)).thenReturn(Optional.of(existingAcc));
        Statement statement = Statement.builder()
                .amount(BigDecimal.valueOf(11))
                .type(StatementType.WITHDRAWAL)
                .build();
        Account updatedAcc = Account.builder()
                .id(id)
                .type(AccountType.CHECKING)
                .statements(List.of(statement))
                .balance(BigDecimal.valueOf(39)).build();
        when(accountService.update(existingAcc, statement)).thenReturn(Optional.of(updatedAcc));


        mockMvc.perform(post("/account/" + id + "/statements")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(statement))
//                        .header("If-Match", "1")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.type").value(AccountType.CHECKING.toString()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(39)))
                .andExpect(jsonPath("$.statements", Matchers.hasSize(1)))
        ;
    }

}