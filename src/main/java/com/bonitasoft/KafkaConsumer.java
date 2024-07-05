package com.bonitasoft;

import java.util.logging.Logger;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class KafkaConsumer extends AbstractConnector {

    private static final Logger LOGGER = Logger.getLogger(KafkaConsumer.class.getName());

    static final String KAFKA_SERVERS = "kafkaServers";
    static final String KAFKA_USER = "kafkaUser";
    static final String KAFKA_PASSWORD = "kafkaPassword";
    static final String KAFKA_TOPIC = "kafkaTopic";
    static final String KAFKA_TIMEOUT = "kafkaTimeout";
    static final String KAFKA_ID = "kafkaId";
    static final String KAFKA_MESSAGE = "kafkaMessage";
    static final String KAFKA_RESPONSE = "kafkaResponse";

    private EventConsumer event = new EventConsumer();

    /**
     * Perform validation on the inputs defined on the connector definition
     * (src/main/resources/connector-kafka-producer.def)
     * You should:
     * - validate that mandatory inputs are presents
     * - validate that the content of the inputs is coherent with your use case
     * (e.g: validate that a date is / isn't in the past ...)
     */
    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        LOGGER.info("Validating parameters...");
        checkMandatoryStringInput(KAFKA_SERVERS);
        checkMandatoryStringInput(KAFKA_USER);
        checkMandatoryStringInput(KAFKA_PASSWORD);
        checkMandatoryStringInput(KAFKA_TOPIC);
        checkMandatoryIntegerInput(KAFKA_TIMEOUT);
    }

    protected void checkMandatoryStringInput(String inputName) throws ConnectorValidationException {
        try {
            String value = (String) getInputParameter(inputName);
            if (value == null || value.isEmpty()) {
                throw new ConnectorValidationException(this,
                        String.format("Mandatory parameter '%s' is missing.", inputName));
            }
        } catch (ClassCastException e) {
            throw new ConnectorValidationException(this, String.format("'%s' parameter must be a String", inputName));
        }
    }

    protected void checkMandatoryIntegerInput(String inputName) throws ConnectorValidationException {
        try {
            Integer value = Integer.parseInt((String) getInputParameter(inputName));
            if (value == null || value == 0) {
                throw new ConnectorValidationException(this,
                        String.format("Mandatory parameter '%s' is missing.", inputName));
            }
        } catch (NumberFormatException e) {
            throw new ConnectorValidationException(this, String.format("'%s' parameter must be a String", inputName));
        }
    }

    /**
     * Core method:
     * - Execute all the business logic of your connector using the inputs (connect
     * to an external service, compute some values ...).
     * - Set the output of the connector execution. If outputs are not set,
     * connector fails.
     */
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        LOGGER.info(String.format("KAFKA_SERVER: %s", getInputParameter(KAFKA_SERVERS)));
        LOGGER.info(String.format("KAFKA_USER: %s", getInputParameter(KAFKA_USER)));
        LOGGER.info(String.format("KAFKA_PASSWORD: %s", getInputParameter(KAFKA_PASSWORD)));
        LOGGER.info(String.format("KAFKA_TOPIC: %s", getInputParameter(KAFKA_TOPIC)));
        LOGGER.info(String.format("KAFKA_TIMEOUT: %s", getInputParameter(KAFKA_TIMEOUT)));
        LOGGER.info("Getting messages...");
        connect();
        setOutputParameter(KAFKA_RESPONSE, event.get((String) getInputParameter(KAFKA_TOPIC), (Integer) getInputParameter(KAFKA_TIMEOUT)));
    }

    /**
     * [Optional] Open a connection to remote server
     */
    @Override
    public void connect() throws ConnectorException {
        LOGGER.info("Creating consumer...");
        event.createConsumer((String) getInputParameter(KAFKA_SERVERS), (String) getInputParameter(KAFKA_USER),
                (String) getInputParameter(KAFKA_PASSWORD));
    }

    /**
     * [Optional] Close connection to remote server
     */
    @Override
    public void disconnect() throws ConnectorException {}
}
