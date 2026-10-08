package org.dev.eclipse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EclipseApplication {

    public static void main(String[] args) {
        SpringApplication.run(EclipseApplication.class, args);
    }

}
