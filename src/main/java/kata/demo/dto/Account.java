package kata.demo.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Value
public class Account {
    UUID id;
    AccountType type;
    BigDecimal balance;
    List<Statement> statements;

}
