package com.bineeta.grpc.client.logic;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface LogicService {
    String getResults(MultipartFile file);
    Boolean isValid(MultipartFile file);
    Path getFile(MultipartFile file);

}
