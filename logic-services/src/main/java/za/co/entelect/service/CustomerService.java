package za.co.entelect.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.entelect.AccountRepository;
import za.co.entelect.CustomerRepository;
import za.co.entelect.annotation.testAnnotation;
import za.co.entelect.dto.Account;
import za.co.entelect.dto.Customer;
import za.co.entelect.entity.AccountEntity;
import za.co.entelect.entity.CustomerEntity;
import za.co.entelect.utility.Mapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, AccountRepository accountRepository, AccountService accountService, TransactionService transactionService) {
        this.customerRepository = customerRepository;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @testAnnotation("test")
    public Customer onboardCustomer(CustomerEntity customer){
        CustomerEntity customerEntity = customerRepository.save(customer);
        AccountEntity currentAccount = createAccount(customerEntity, "CURRENT");
        AccountEntity savingsAccount = createAccount(customerEntity, "SAVINGS");

        accountService.createNewAccount(currentAccount);
        accountService.createNewAccount(savingsAccount);

        Account account = transactionService.createSignUpBonusTransaction(savingsAccount);
        if (account == null){
            throw new RuntimeException("Could not create the signup-bonus transaction");
        }

        return Mapping.toCustomer(customerEntity);
    }

    public AccountEntity createAccount(CustomerEntity customerEntity, String accountType){
        AccountEntity account = new AccountEntity();
        account.setAccountNumber(generateRandomAccountNumber());
        account.setAccountType(accountType);
        account.setBranchCode("632005");
        account.setBalance(BigDecimal.ZERO);
        account.setCustomer(customerEntity);
        return account;
    }

    public Customer getCustomerByCustomerId(Long customerId){
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + customerId));

        return Mapping.toCustomer(customer);
    }

    private String generateRandomAccountNumber() {
        Random random = new Random();
        String accountNumber = "";

        for (int i = 0; i < 11; i++) {
            accountNumber += String.valueOf(random.nextInt(10));
        }
        return accountNumber;
    }
}
