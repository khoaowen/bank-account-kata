package kata.demo.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class StatementPrinting {
    List<Statement> statements;
    int currentPage;
    int totalPages;
    long totalStatements;
    BigDecimal accountBalance;
}
