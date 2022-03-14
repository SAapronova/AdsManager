package process_manager.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {
    @Bean
    @SuppressWarnings("java:S1452")
    public ProducerFactory<String, ?> producerFactory(KafkaProperties kafkaProperties, ObjectMapper objectMapper) {
        DefaultKafkaProducerFactory<String, ?> customKafkaProducerFactory =
                new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());

        customKafkaProducerFactory.setValueSerializer(new JsonSerializer<>(objectMapper));

        return customKafkaProducerFactory;
    }
}
