package za.co.entelect.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void NotifyAccountHolder(String message) {
        System.out.println(message);
    }
}
