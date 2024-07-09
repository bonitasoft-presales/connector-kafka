package com.bonitasoft.presales.connector.kafka;

import com.bonitasoft.presales.connector.AbstractKafkaTest;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class EventConsumerTest extends AbstractKafkaTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE_NAME));

    @BeforeEach
    public void setUp() {
        kafka.start();
    }

    @AfterEach
    public void cleanUp() {
        kafka.close();
    }

    @Test
    void createConsumer() {
        //given
        createKafkaMessage(kafka, "key1", "Hello, Kafka!");
        createKafkaMessage(kafka, "key2", "Hello, Bonita!");
        EventConsumer eventConsumer = new EventConsumer();

        //when
        String server = String.format("%s:%s", SERVER, kafka.getMappedPort(9093));
        eventConsumer.createConsumer(server, USER, PASSWORD, GROUP_ID);

        //then
        ConsumerRecords<String, String> consumerRecords = eventConsumer.get(TOPIC, TIMEOUT);
        assertThat(consumerRecords.count()).isEqualTo(2);

    }

    @Test
    void testKafka() throws Exception {
        testKafkaFunctionality(kafka.getBootstrapServers());
    }

    void testKafkaFunctionality(String bootstrapServers) throws Exception {
        try (
                AdminClient adminClient = AdminClient.create(
                        ImmutableMap.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                );

                KafkaProducer<String, String> producer = new KafkaProducer<>(
                        ImmutableMap.of(
                                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                bootstrapServers,
                                ProducerConfig.CLIENT_ID_CONFIG,
                                UUID.randomUUID().toString()
                        ),
                        new StringSerializer(),
                        new StringSerializer()
                );

                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(
                        ImmutableMap.of(
                                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                bootstrapServers,
                                ConsumerConfig.GROUP_ID_CONFIG,
                                "tc-" + UUID.randomUUID(),
                                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                                "earliest"
                        ),
                        new StringDeserializer(),
                        new StringDeserializer()
                )
        ) {

            Collection<NewTopic> topics = Collections.singletonList(new NewTopic(TOPIC, 1, (short) 1));
            adminClient.createTopics(topics).all().get(30, TimeUnit.SECONDS);

            consumer.subscribe(Collections.singletonList(TOPIC));

            producer.send(new ProducerRecord<>(TOPIC, "myKey", "myValue")).get();

            Unreliables.retryUntilTrue(
                    10,
                    TimeUnit.SECONDS,
                    () -> {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                        assertThat(records.count()).isEqualTo(1);
                        return !records.isEmpty();
                    }
            );

            consumer.unsubscribe();
        }
    }


}