package com.foilen.smalltools.net.communication;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.ConnectionMessageConstants;

/**
 * Gets the messages on the {@link CommunicationMessageReceiver} and execute their message of type {@link CommunicationCommand}.
 */
public class CommunicationCommandExecutor extends Thread {

    private final static Logger log = LoggerFactory.getLogger(CommunicationCommandExecutor.class);

    private CommunicationMessageReceiver communicationMessageReceiver;

    /**
     * Create and start the thread.
     * 
     * @param communicationMessageReceiver
     *            the communication message receiver
     */
    public CommunicationCommandExecutor(CommunicationMessageReceiver communicationMessageReceiver) {
        this.communicationMessageReceiver = communicationMessageReceiver;
        start();
    }

    @Override
    public void run() {

        log.info("Starting to execute messages when available");

        while (true) {
            try {
                Map<String, Object> message = communicationMessageReceiver.getNextMessage();
                Object mainMessage = message.get(ConnectionMessageConstants.OBJECT);

                // Check null
                if (mainMessage == null) {
                    log.error("The message was null");
                    continue;
                }

                // Check right type
                if (!(mainMessage instanceof CommunicationCommand)) {
                    log.error("The message of type {} is not a CommunicationCommand.", mainMessage.getClass().getName());
                    continue;
                }

                // Execute
                CommunicationCommand command = (CommunicationCommand) mainMessage;
                Connection connection = (Connection) message.get(ConnectionMessageConstants.CONNECTION);
                command.execute(connection, message);

            } catch (Exception e) {
                log.error("Problem executing the command", e);
            }
        }
    }
}
