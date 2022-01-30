package kata.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class Statement {
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    LocalDateTime date;
    @NotNull
    StatementType type;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
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
