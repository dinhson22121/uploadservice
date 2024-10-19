package org.example.uploadservice.controller;

import org.example.uploadservice.service.FileProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
public class FileUploadController {

    @Autowired
    private FileProcessingService fileProcessingService;

    @PostMapping("/upload")
    public Mono<String> handleFileUpload(@RequestPart("file") Mono<FilePart> filePartMono) {
        String processId = UUID.randomUUID().toString();
        return filePartMono
                .flatMap(filePart -> filePart.content()
                        .reduce(new ByteArrayOutputStream(), (baos, dataBuffer) -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            try {
                                baos.write(bytes);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return baos;
                        }))
                .flatMap(baos -> {
                    fileProcessingService.processFileAsync(processId, new ByteArrayInputStream(baos.toByteArray()))
                            .subscribe();
                    return Mono.just("File upload started. Check status at /status/" + processId);
                })
                .onErrorResume(e -> Mono.just("File upload failed: " + e.getMessage()));
    }
}