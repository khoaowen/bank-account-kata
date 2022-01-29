package kata.demo.dto;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Statement {
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
