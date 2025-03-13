package za.co.entelect.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.entelect.dto.Customer;
import za.co.entelect.entity.CustomerEntity;
import za.co.entelect.service.CustomerService;

@RestController
@RequestMapping("api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<Customer> onboardCustomer(@RequestBody CustomerEntity customer) {
        Customer newCustomer = customerService.onboardCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newCustomer);
    }
}
