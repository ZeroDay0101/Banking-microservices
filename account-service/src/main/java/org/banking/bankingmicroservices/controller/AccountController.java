package org.banking.bankingmicroservices.controller;

import jakarta.validation.Valid;
import org.banking.bankingmicroservices.dto.AccountByIdDTO;
import org.banking.bankingmicroservices.dto.AmountRequest;
import org.banking.bankingmicroservices.dto.BalanceResponse;
import org.banking.bankingmicroservices.dto.TransferDTO;
import org.banking.bankingmicroservices.model.Account;
import org.banking.bankingmicroservices.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountByIdDTO> getAccountById(@PathVariable String id,
                                                         Principal principal) {
        if (!id.equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(Principal principal) {
        Account account = accountService.getAccount(principal.getName());
        return ResponseEntity.ok(new BalanceResponse(account.getUsername(), account.getAccountBalance()));
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(
            Principal principal,
            @Valid @RequestBody AmountRequest request) {
        accountService.deposit(principal.getName(), request.amount());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            Principal principal,
            @Valid @RequestBody AmountRequest request) {
        accountService.withdraw(principal.getName(), request.amount());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            Principal principal,
            @Valid @RequestBody TransferDTO transferDTO) {
        accountService.transfer(principal.getName(), transferDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
