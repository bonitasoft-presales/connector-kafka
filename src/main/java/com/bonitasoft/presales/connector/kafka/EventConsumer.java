package com.bonitasoft.presales.connector.kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

public class EventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    private KafkaConsumer<String, String> consumer;

    public EventConsumer() {

    }

    public void createConsumer(String kafkaServer, String kafkaUser, String kafkaPassword, String kafkaGroupId) {
        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);

        // create consumer
        this.consumer = new KafkaConsumer<>(properties);
    }

    public ConsumerRecords<String, String> get(String topic, int timeout) {
        ConsumerRecords<String, String> records = null;
        try {
            this.consumer.subscribe(Arrays.asList(topic));
            records = this.consumer.poll(Duration.ofMillis(timeout));
            for (ConsumerRecord<String, String> record : records) {
                LOGGER.info("Key: {}, Value: {}", record.key(), record.value());
                LOGGER.info("Partition: {}, Offset: {}", record.partition(), record.offset());
            }
            this.consumer.close();
        } catch (WakeupException e) {
            LOGGER.info("Wake up exception!");
            // we ignore this as this is an expected exception when closing a consumer
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw (e);
        }
        return records;
    }

}