package org.example.audioservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

    private static final Logger Log = LoggerFactory.getLogger(ServiceController.class);

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthCheck() {

        Log.info("healthcheck|ok");
        String status = """
                {"status": "up"}
                """;
        return ResponseEntity.ok(status);
    }
}
