package kata.demo.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * A pageable representation for all statements within an account
 */
@Value
@Builder
public class StatementPrinting {
    List<Statement> statements;
    int currentPage;
    int totalPages;
    long totalStatements;
    BigDecimal accountBalance;
}
