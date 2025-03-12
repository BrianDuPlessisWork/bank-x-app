package za.co.entelect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.entelect.TransactionProcessorRepository;

@Service
public class TransactionProcessorService {

    private final TransactionProcessorRepository transactionProcessorRepository;

    @Autowired
    public TransactionProcessorService(TransactionProcessorRepository transactionProcessorRepository) {
        this.transactionProcessorRepository = transactionProcessorRepository;
    }

    
}
