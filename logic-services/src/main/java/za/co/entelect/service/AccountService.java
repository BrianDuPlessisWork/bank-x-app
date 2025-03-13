package za.co.entelect.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.entelect.AccountRepository;
import za.co.entelect.dto.Account;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.utility.Mapping;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createNewAccount(AccountEntity account){
        try {
            if (account == null) {
                throw new IllegalArgumentException("Account entity cannot be null");
            }

            if (accountRepository.findByAccountNumber(account.getAccountNumber()).isPresent()) {
                throw new RuntimeException("Account with number " + account.getAccountNumber() + " already exists");
            }

            AccountEntity accountEntity = accountRepository.save(account);
            return Mapping.toAccount(accountEntity);
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while creating account", e);
        }
    }

    public Account updateAccount(AccountEntity account){
        try {
            if (account == null) {
                throw new IllegalArgumentException("Account entity cannot be null");
            }

            accountRepository.findById(account.getAccountID())
                    .orElseThrow(() -> new EntityNotFoundException("Cannot update non-existent account with ID: " + account.getAccountID()));

            AccountEntity accountEntity = accountRepository.save(account);
            return Mapping.toAccount(accountEntity);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error occurred while updating account", e);
        }
    }

    public Account findAccountByAccountNumber(String accountNumber){
        AccountEntity accountEntity = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with number: " + accountNumber));

        return Mapping.toAccount(accountEntity);
    }

    public AccountEntity findAccountEntityByAccountNumber(String accountNumber){
        AccountEntity accountEntity = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found with number: " + accountNumber));

        return accountEntity;
    }
}
