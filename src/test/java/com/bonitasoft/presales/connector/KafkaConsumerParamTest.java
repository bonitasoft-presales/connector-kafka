package com.bonitasoft.presales.connector;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KafkaConsumerParamTest implements IKafkaConsumer {

    Logger logger = LoggerFactory.getLogger(KafkaConsumerParamTest.class);

    public static final String KAFKA_RESPONSE = "kafkaResponse";
    KafkaConsumer connector;


    @BeforeEach
    public void setUp() {
        connector = new KafkaConsumer();
    }

    @Test
    void should_throw_exception_if_mandatory_input_is_missing() {
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_mandatory_input_is_empty() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KafkaConsumer.KAFKA_SERVERS, "");
        parameters.put(KafkaConsumer.KAFKA_GROUP_ID, GROUP_ID);
        parameters.put(KafkaConsumer.KAFKA_USER, USER);
        parameters.put(KafkaConsumer.KAFKA_PASSWORD, PASSWORD);
        parameters.put(KafkaConsumer.KAFKA_TOPIC, TOPIC);
        parameters.put(KafkaConsumer.KAFKA_TIMEOUT, TIMEOUT);
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_mandatory_input_is_null() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KafkaConsumer.KAFKA_SERVERS, null);
        parameters.put(KafkaConsumer.KAFKA_GROUP_ID, GROUP_ID);
        parameters.put(KafkaConsumer.KAFKA_USER, USER);
        parameters.put(KafkaConsumer.KAFKA_PASSWORD, PASSWORD);
        parameters.put(KafkaConsumer.KAFKA_TOPIC, TOPIC);
        parameters.put(KafkaConsumer.KAFKA_TIMEOUT, TIMEOUT);
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_mandatory_input_is_not_a_string() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KafkaConsumer.KAFKA_SERVERS, 38);
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }
}