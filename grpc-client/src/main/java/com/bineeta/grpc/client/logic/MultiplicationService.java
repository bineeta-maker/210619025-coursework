package com.bineeta.grpc.client.logic;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface MultiplicationService {
    String getResults(MultipartFile file,Boolean synchronous);
    Boolean isValid(MultipartFile file);
    Path getFile(MultipartFile file);

}
