package org.example.uploadservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ProcessingStatusController {

    private final Map<String, String> processingStatus = new ConcurrentHashMap<>();

    @GetMapping("/status/{id}")
    public Mono<String> getStatus(@PathVariable String id) {
        return Mono.justOrEmpty(processingStatus.get(id))
                .defaultIfEmpty("Processing not found");
    }

    public void updateStatus(String id, String status) {
        processingStatus.put(id, status);
    }
}