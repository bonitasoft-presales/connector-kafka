package com.bonitasoft.presales.connector.kafka;

import com.bonitasoft.presales.connector.AbstractKafkaTest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EventProducerTest extends AbstractKafkaTest {
    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE_NAME));

    @BeforeEach
    public void setUp() throws Exception {
        kafka.start();
        createTopics(kafka.getBootstrapServers());
    }

    @AfterEach
    public void cleanUp() {
        kafka.close();
    }

    private void createTopics(String bootstrapServers) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        //kafka.execInContainer("/bin/sh", "-c", "/usr/bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic "+TOPIC);
        //kafka.execInContainer("/bin/sh", "-c", "/usr/bin/kafka-topics --create --replication-factor 1 --partitions 1 --topic "+TOPIC);

        //String server = String.format("%s:%s", SERVER, kafka.getMappedPort(9093));
        AdminClient adminClient = AdminClient.create(
                ImmutableMap.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        );
        Collection<NewTopic> topics = Collections.singletonList(new NewTopic(TOPIC, 1, (short) 1));
        adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);
        logger.info("topic {} created", topics);
//        var newTopics =
//                Arrays.stream(topics)
//                        .map(topic -> new NewTopic(topic, 1, (short) 1))
//                        .collect(Collectors.toList());
//        try (var admin = AdminClient.create(Map.of(BOOTSTRAP_SERVERS_CONFIG, server))) {
//            admin.createTopics(newTopics);
//        }
    }

    @Test
    void send() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        String server = String.format("%s:%s", SERVER, kafka.getMappedPort(9093));
        EventProducer eventProducer = new EventProducer();
        eventProducer.createProducer(kafka.getBootstrapServers());

        //when
        String key = "key";
        String message = "message";
        Future<RecordMetadata> send = eventProducer.send(TOPIC, key, message);

        //then
        RecordMetadata recordMetadata = send.get(30, TimeUnit.SECONDS);
        logger.info("send record topic:{} - message{}", recordMetadata.topic(), recordMetadata.toString());
        assertThat(recordMetadata).as("should publish").isNotNull();

        EventConsumer eventConsumer = new EventConsumer();
        eventConsumer.createConsumer(server, USER, PASSWORD, GROUP_ID);
        ConsumerRecords<String, String> consumerRecords = eventConsumer.get(TOPIC, TIMEOUT);
        assertThat(consumerRecords.count()).isEqualTo(1);
        ConsumerRecord<String, String> next = consumerRecords.records(TOPIC).iterator().next();
        assertThat(next.key()).isEqualTo(key);
        assertThat(next.value()).isEqualTo(message);
    }
}