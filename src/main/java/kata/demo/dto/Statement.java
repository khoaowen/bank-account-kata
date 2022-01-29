package kata.demo.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class Statement {
    UUID id;
    StatementType type;
    BigDecimal amount;

    public BigDecimal applyStatement(BigDecimal balance) {
        switch (type) {
            case DEPOSIT:
                return balance.add(amount);
            case WITHDRAWAL:
                return balance.subtract(amount);
            default:
                throw new IllegalStateException("Statement type is not recognized: " + type);
        }
    }
}
