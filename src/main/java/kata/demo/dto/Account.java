package kata.demo.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class Account {
    UUID id;
    AccountType type;
    BigDecimal balance;
    List<Statement> statements;

}
