package com.bonitasoft.presales.connector;

import com.bonitasoft.presales.connector.kafka.EventProducer;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

import java.util.logging.Logger;

public class KafkaProducer extends AbstractConnector implements KafkaConstants {

    private static final Logger LOGGER = Logger.getLogger(KafkaProducer.class.getName());

    private EventProducer producer;

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
        checkMandatoryStringInput(KAFKA_ID);
        checkMandatoryStringInput(KAFKA_MESSAGE);
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

    /**
     * Core method:
     * - Execute all the business logic of your connector using the inputs (connect
     * to an external service, compute some values ...).
     * - Set the output of the connector execution. If outputs are not set,
     * connector fails.
     */
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        String server = getInputParameter(KAFKA_SERVERS).toString();
        String user = getInputParameter(KAFKA_USER).toString();

        LOGGER.info(String.format("KAFKA_SERVER: %s", server));
        LOGGER.info(String.format("KAFKA_USER: %s", user));
        LOGGER.info(String.format("KAFKA_PASSWORD: %s", "***"));
        LOGGER.info(String.format("KAFKA_TOPIC: %s", getInputParameter(KAFKA_TOPIC)));
        LOGGER.info(String.format("KAFKA_ID: %s", getInputParameter(KAFKA_ID)));
        LOGGER.info(String.format("KAFKA_MESSAGE: %s", getInputParameter(KAFKA_MESSAGE)));
        LOGGER.info("Sending message...");

        setOutputParameter(KAFKA_RESPONSE, producer.send((String) getInputParameter(KAFKA_TOPIC),
                getInputParameter(KAFKA_ID).toString(), (String) getInputParameter(KAFKA_MESSAGE)));
    }

    /**
     * [Optional] Open a connection to remote server
     */
    @Override
    public void connect() throws ConnectorException {
        LOGGER.info("Creating producer...");
        String server = getInputParameter(KAFKA_SERVERS).toString();
        String user = getInputParameter(KAFKA_USER).toString();
        String password = getInputParameter(KAFKA_PASSWORD).toString();

        producer = new EventProducer();
        producer.createProducer(server);

    }

    /**
     * [Optional] Close connection to remote server
     */
    @Override
    public void disconnect() throws ConnectorException {
    }
}
