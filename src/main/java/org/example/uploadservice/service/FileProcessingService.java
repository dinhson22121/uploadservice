package org.example.uploadservice.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.example.uploadservice.controller.ProcessingStatusController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Service
public class FileProcessingService {

    @Autowired
    private ProcessingStatusController statusController;

    public Mono<String> processFileAsync(String id, InputStream inputStream) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                statusController.updateStatus(id, "Processing started");
                String result = processFile(inputStream);
                statusController.updateStatus(id, "Completed: " + result);
                return result;
            } catch (Exception e) {
                statusController.updateStatus(id, "Failed: " + e.getMessage());
                throw new RuntimeException("File processing failed", e);
            }
        })).subscribeOn(Schedulers.boundedElastic());
    }

    private String processFile(InputStream inputStream) throws Exception {
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);

        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Expected content to be an array");
        }

        long count = 0;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            processJsonObject(parser);
            count++;

            if (count % 1000 == 0) {
                System.out.println("Processed " + count + " objects");
            }
        }

        return "Successfully processed " + count + " JSON objects";
    }

    private void processJsonObject(JsonParser parser) throws Exception {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken();
            String value = parser.getValueAsString();

            switch (fieldName) {
                case "id":
                    System.out.println("Processing ID: " + value);
                    break;
                case "name":
                    System.out.println("Processing Name: " + value);
                    break;
            }
        }
    }
}