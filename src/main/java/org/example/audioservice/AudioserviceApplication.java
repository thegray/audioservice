package org.example.audioservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.example.audioservice")
public class AudioserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioserviceApplication.class, args);
    }

}
