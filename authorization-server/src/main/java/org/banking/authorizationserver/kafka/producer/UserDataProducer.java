package org.banking.authorizationserver.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserDataProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public UserDataProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.user-data-topic}") String topic
    ) {

        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }


    public void sendMessage(String username) {
        kafkaTemplate.send(topic, username);
    }
}
