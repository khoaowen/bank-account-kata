package kata.demo.controller;

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

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account saved = accountService.save(account);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(account);
    }

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

    @PostMapping("/{id}/statements")
    public ResponseEntity<Account> makeStatement(@RequestBody Statement statement,
                                                 @PathVariable UUID id,
                                                 @RequestHeader("If-Match") Integer ifMatch) {
        // get existing account
        Optional<Account> existingAccount = accountService.findById(id);
        return existingAccount.map(acc -> {
            acc.getStatements().add(statement);
            acc.setBalance(statement.applyStatement(acc.getBalance()));
            accountService.save(acc);
            return ResponseEntity.ok()
                    .body(acc);
        }).orElse(ResponseEntity.notFound().build());

    }

}
