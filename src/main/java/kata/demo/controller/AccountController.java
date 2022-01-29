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
import kata.demo.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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
        return accountService.findById(id)
                .map(acc -> {
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .build()
                            .toUri();
                    return ResponseEntity.created(location)
                            .body(acc);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Make a statement to the account")
    @ApiResponse(responseCode = "200", description = "Operation successful",
            content = {@Content(mediaType = "application/json")})
    @Parameter(name = "id", in = ParameterIn.PATH, description = "Account Id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @PostMapping("/{id}/statements")
    public ResponseEntity<Account> makeStatement(@RequestBody Statement statement,
                                                 @PathVariable UUID id
//                                           @RequestHeader("If-Match") Integer ifMatch
    ) {
        // get existing account
        Optional<Account> existingAccount = accountService.findById(id);

        @SuppressWarnings("unchecked")
        ResponseEntity<Account> accountResponseEntity = (ResponseEntity<Account>) existingAccount.map(acc -> {
            Optional<Account> saved = accountService.update(acc, statement);
            if (saved.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .build()
                    .toUri();
            return ResponseEntity.created(location)
                    .body(saved.get());
        }).orElse(ResponseEntity.notFound().build());
        return accountResponseEntity;

    }

}
