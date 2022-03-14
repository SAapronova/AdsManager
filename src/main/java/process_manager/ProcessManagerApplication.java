package process_manager;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableProcessApplication
@SpringBootApplication
public class ProcessManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProcessManagerApplication.class, args);
    }

}
