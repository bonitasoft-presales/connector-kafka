package com.bonitasoft.presales.connector.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class EventProducer {

    private static final Logger LOGGER = Logger.getLogger(EventProducer.class.getName());

    private Producer<String, String> kafkaProducer;

    public void createProducer(String kafkaServer) {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        LOGGER.info("Producer created.");
        this.kafkaProducer = new KafkaProducer<>(props);
    }

    public Future<RecordMetadata> send(String topic, String key, String value) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                key,
                value);
        Future<RecordMetadata> send = kafkaProducer.send(record);
        kafkaProducer.flush();
        kafkaProducer.close();
        LOGGER.info("Message has been sent.");
        return send;
    }

}