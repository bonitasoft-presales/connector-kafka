package com.bonitasoft.presales.connector;

import java.util.logging.Logger;

import com.bonitasoft.presales.connector.kafka.EventConsumer;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class KafkaConsumer extends AbstractConnector implements KafkaConstants {

    private static final Logger LOGGER = Logger.getLogger(KafkaConsumer.class.getName());
    EventConsumer consumer;

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
        checkMandatoryStringInput(KAFKA_GROUP_ID);
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
            Integer.parseInt(getInputParameter(inputName).toString());
        } catch (NumberFormatException e) {
            throw new ConnectorValidationException(this, String.format("'%s' parameter must be a Integer", inputName));
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
        try {
            validateInputParameters();
        } catch (ConnectorValidationException e) {
            throw new ConnectorException(e);
        }
        String server = getInputParameter(KAFKA_SERVERS).toString();
        String groupId = getInputParameter(KAFKA_GROUP_ID).toString();
        String user = getInputParameter(KAFKA_USER).toString();
        String password = getInputParameter(KAFKA_PASSWORD).toString();
        String topic = getInputParameter(KAFKA_TOPIC).toString();
        Integer timeout = (Integer) getInputParameter(KAFKA_TIMEOUT);

        this.consumer = new EventConsumer();

        LOGGER.info(String.format("KAFKA_SERVER: %s", server));
        LOGGER.info(String.format("KAFKA_GROUP_ID: %s", groupId));
        LOGGER.info(String.format("KAFKA_USER: %s", user));
        LOGGER.info(String.format("KAFKA_TOPIC: %s", topic));
        LOGGER.info(String.format("KAFKA_TIMEOUT: %s", timeout));
        LOGGER.info("Getting messages...");
        connect();
        setOutputParameter(KAFKA_RESPONSE, consumer.get(topic, timeout));
    }

    /**
     * [Optional] Open a connection to remote server
     */
    @Override
    public void connect() throws ConnectorException {
        LOGGER.info("Creating consumer...");
        consumer.createConsumer((String) getInputParameter(KAFKA_SERVERS), (String) getInputParameter(KAFKA_USER),
                (String) getInputParameter(KAFKA_PASSWORD), getInputParameter(KAFKA_GROUP_ID).toString());
    }

    /**
     * [Optional] Close connection to remote server
     */
    @Override
    public void disconnect() throws ConnectorException {
    }
}
