package com.learn.kafka.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "my-topic", groupId = "my-group")
    public void consume(String message, Acknowledgment acknowledgment) {
        try {
            System.out.println("Waiting for 5 seconds...");
            Thread.sleep(5000); // 5000 milliseconds = 5 seconds
            System.out.println("Received: " + message);
            System.out.println("Proceeding after 5 seconds");
            acknowledgment.acknowledge();
            System.out.println("acknowledged: " + message);
    } catch (Exception e) {
        // don't acknowledge — message will be redelivered
        e.printStackTrace();
    }
    }
}
