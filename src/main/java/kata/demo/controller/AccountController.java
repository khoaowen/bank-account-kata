package kata.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kata.demo.dto.Account;
import kata.demo.dto.Statement;
import kata.demo.dto.StatementPrinting;
import kata.demo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create an account")
    @ApiResponse(responseCode = "201", description = "Account created")
    @PostMapping
    public ResponseEntity<Account> createAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Create Account request body",
                    content = @Content(schema = @Schema(implementation = Account.class),
                            examples = {@ExampleObject(name = "An account to be created",
                                    value = "{\n" +
                                            "  \"type\": \"CHECKING\",\n" +
                                            "  \"balance\": 0,\n" +
                                            "  \"statements\": []\n" +
                                            "}")}), required = true)
            @RequestBody
            @Valid
                    Account account) {
        Account saved = accountService.save(account);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(saved);
    }

    @Operation(summary = "Get an account")
    @ApiResponse(responseCode = "200", description = "Account found",
            content = {@Content(mediaType = "application/json")})
    @Parameter(name = "id", in = ParameterIn.PATH, description = "Account Id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
        return ResponseEntity.created(location)
                .body(accountService.findById(id));
    }

    @Operation(summary = "Make a statement to the account")
    @ApiResponse(responseCode = "200", description = "Operation successful",
            content = {@Content(mediaType = "application/json")})
    @Parameter(name = "id", in = ParameterIn.PATH, description = "Account Id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @PostMapping("/{id}/statements")
    public ResponseEntity<Account> makeStatement(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Statement to apply on the account",
                    content = @Content(schema = @Schema(implementation = Statement.class),
                            examples = {@ExampleObject(name = "A deposit statement",
                                    value = "{\n" +
                                            "  \"type\": \"DEPOSIT\",\n" +
                                            "  \"amount\": 15\n" +
                                            "}"),
                                    @ExampleObject(name = "A withdrawal statement",
                                            value = "{\n" +
                                                    "  \"type\": \"WITHDRAWAL\",\n" +
                                                    "  \"amount\": 15\n" +
                                                    "}")}), required = true)
            @RequestBody @Valid Statement statement,
            @PathVariable UUID id
//                                           @RequestHeader("If-Match") Integer ifMatch
    ) {
        statement.setDate(LocalDateTime.now());
        // get existing account
        Account existingAccount = accountService.findById(id);
        Account saved = accountService.update(existingAccount, statement);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
        return ResponseEntity.created(location)
                .body(saved);
    }

    @Operation(summary = "Print statements of the account")
    @ApiResponse(responseCode = "200", description = "Operation successful",
            content = {@Content(mediaType = "application/json")})
    @Parameter(name = "id", in = ParameterIn.PATH, description = "Account Id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @Parameter(name = "pageable", in = ParameterIn.QUERY, description = "Request to retrieve the statements page by page",
            examples = {@ExampleObject(name = "Display 10 statements at once, ordered by date ascending, go to the first page",
                    value = "{\n" +
                            "  \"page\": 0,\n" +
                            "  \"size\": 10,\n" +
                            "  \"sort\": [\n" +
                            "    \"date,asc\"\n" +
                            "  ]\n" +
                            "}"),
                    @ExampleObject(name = "Display 10 statements at once, ordered by date descending, go to the first page",
                            value = "{\n" +
                                    "  \"page\": 0,\n" +
                                    "  \"size\": 10,\n" +
                                    "  \"sort\": [\n" +
                                    "    \"date,desc\"\n" +
                                    "  ]\n" +
                                    "}")})
    @GetMapping("/{id}/statements")
    public ResponseEntity<StatementPrinting> printStatements(
            @PathVariable UUID id, Pageable pageable
//                                           @RequestHeader("If-Match") Integer ifMatch
    ) {
        // get existing account
        Account existingAccount = accountService.findById(id);
        StatementPrinting print = createStatementPrinting(pageable, existingAccount);
        return ResponseEntity.ok(print);
    }

    private StatementPrinting createStatementPrinting(Pageable pageable, Account existingAccount) {
        // FIXME this kind of pagination should be done with Spring REST repository
        List<Statement> statements = existingAccount.getStatements();

        Optional<Sort.Order> order = pageable.getSort().stream().findFirst();
        if (order.isPresent()) {
            if (order.get().getProperty().equals("date")) {
                if (order.get().getDirection() == Sort.Direction.ASC) {
                    statements.sort(Comparator.comparing(Statement::getDate));
                } else {
                    statements.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
                }
            }
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), statements.size());
        Page<Statement> page
                = new PageImpl<>(statements.subList(start, end), pageable, statements.size());
        return StatementPrinting.builder()
                .accountBalance(existingAccount.getBalance())
                .statements(page.getContent())
                .currentPage(page.getNumber())
                .totalStatements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

}
