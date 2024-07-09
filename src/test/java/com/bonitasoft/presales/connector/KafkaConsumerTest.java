package com.bonitasoft.presales.connector;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.bonitasoft.engine.connector.ConnectorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static com.bonitasoft.presales.connector.KafkaConstants.KAFKA_RESPONSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KafkaConsumerTest extends AbstractKafkaTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE_NAME));

    @BeforeEach
    public void setUp() {
        kafka.start();
        connector = new KafkaConsumer();
    }

    @AfterEach
    public void cleanUp() {
        kafka.close();
    }

    KafkaConsumer connector;

    @Test
    void should_create_output_for_valid_input() throws ConnectorException {
        //given
        createKafkaMessage(kafka, "key", "value");

        Map<String, Object> parameters = new HashMap<>();
        String server = String.format("%s:%s", SERVER, kafka.getMappedPort(9093));
        parameters.put(KafkaConsumer.KAFKA_SERVERS, server);
        parameters.put(KafkaConsumer.KAFKA_GROUP_ID, GROUP_ID);
        parameters.put(KafkaConsumer.KAFKA_USER, USER);
        parameters.put(KafkaConsumer.KAFKA_PASSWORD, PASSWORD);
        parameters.put(KafkaConsumer.KAFKA_TOPIC, TOPIC);
        parameters.put(KafkaConsumer.KAFKA_TIMEOUT, TIMEOUT);
        connector.setInputParameters(parameters);
        Map<String, Object> outputs = connector.execute();
        assertThat(outputs).containsKey(KAFKA_RESPONSE);
        ConsumerRecords<Long, String> response = (ConsumerRecords<Long, String>) outputs.get(KAFKA_RESPONSE);
        assertThat(response).hasSize(1);

    }


}