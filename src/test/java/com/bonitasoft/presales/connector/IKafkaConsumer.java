package com.bonitasoft.presales.connector;

public interface IKafkaConsumer {
    String SERVER = "localhost";
    String USER = "user";
    String PASSWORD = "bpm";
    String TOPIC = "bonita-topic";
    String GROUP_ID = "bonita-group";
    int TIMEOUT = 10000;
    String KAFKA_IMAGE_NAME = "confluentinc/cp-kafka:7.6.1";
}
