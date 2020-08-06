package com.arqaam.logframelab.util;

import com.arqaam.logframelab.model.Progress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class Utils implements Logging {

    @Value("${logframelab.web-socket-progress}")
    private String webSocketURI;

    @Autowired
    private SimpMessageSendingOperations messageSendingOperations;

    /**
     * Sends a progress update to the web socket in the client.
     * @param value Percentage value of the progress
     */
    public void sendProgressMessage(float value){
        logger().debug("Preparing to send message through web socket. Value {}", value);
        messageSendingOperations.convertAndSend(webSocketURI, new Progress((int)value));
    }
}
