package com.bonitasoft.presales.connector;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.KafkaContainer;

import java.util.Properties;

public abstract class AbstractKafkaTest implements IKafkaConsumer {
    public final Logger logger = LoggerFactory.getLogger(AbstractKafkaTest.class);

    protected void createKafkaMessage(KafkaContainer kafka, String key, String value) {
        // Set up producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getHost() + ":" + kafka.getMappedPort(9093));
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        KafkaProducer<String, String> producer = getProducer(key, value, properties);

        // Flush and close producer
        producer.flush();
        producer.close();
    }

    private @NotNull KafkaProducer<String, String> getProducer(String key, String value, Properties properties) {
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Create a producer record
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, value);

        // Send data - asynchronous
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                logger.info("Received new metadata: {} Topic: {} Partition: {} Offset: {} Timestamp: {} ",
                        metadata.timestamp(), metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
            } else {
                logger.error("error:", exception);
            }
        });
        return producer;
    }
}
