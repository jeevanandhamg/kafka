package com.learn.kafka.controller;

import com.learn.kafka.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    @Autowired
    private KafkaProducerService producerService;

    @PostMapping("/publish")
    public String publish(@RequestParam String message) {
        producerService.sendMessage("my-topic", message);
        return "Message sent!";
    }
}
