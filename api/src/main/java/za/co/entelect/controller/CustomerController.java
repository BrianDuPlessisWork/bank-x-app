package za.co.entelect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.entelect.dto.Account;
import za.co.entelect.dto.Customer;
import za.co.entelect.entity.CustomerEntity;
import za.co.entelect.service.AccountService;
import za.co.entelect.service.CustomerService;

import java.util.List;

@RestController
@RequestMapping("api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;

    @Autowired
    public CustomerController(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<Customer> onboardCustomer(@RequestBody CustomerEntity customer) {
        Customer newCustomer = customerService.onboardCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
    }

    @GetMapping("/accounts/{customerId}")
    public ResponseEntity<List<Account>> getCustomerAccounts(@PathVariable(name="customerId") Long customerId){
        List<Account> accounts = accountService.findAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }
}
