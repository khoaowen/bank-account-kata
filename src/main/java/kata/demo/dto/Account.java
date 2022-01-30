package kata.demo.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Value
public class Account {
    UUID id;
    @NotNull
    AccountType type;
    @NotNull
    BigDecimal balance;
    List<Statement> statements;

}
