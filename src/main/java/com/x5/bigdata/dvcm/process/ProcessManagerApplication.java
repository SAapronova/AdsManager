package com.x5.bigdata.dvcm.process;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableProcessApplication
@SpringBootApplication
public class ProcessManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessManagerApplication.class, args);
    }

}
