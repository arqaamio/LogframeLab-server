package com.arqaam.logframelab.util;

import java.io.IOException;

import com.arqaam.logframelab.exception.FailedToOpenFileException;
import com.arqaam.logframelab.model.Progress;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
        logger().info("Preparing to send message through web socket. Value {}", value);
        messageSendingOperations.convertAndSend(webSocketURI, new Progress((int)value));
    }

    public String retrieveTextFromDocument(MultipartFile file) {
        String text = "";
        try {
            if(file.getOriginalFilename().matches(".+\\.docx$")) {
                logger().info("Retrieving text from a .docx file with file name: {}", file.getOriginalFilename());
                XWPFDocument doc = new XWPFDocument(file.getInputStream());
                text = new XWPFWordExtractor(doc).getText();
                doc.close();
            } else {
                // Read .doc
                logger().info("Retrieving text from a .doc file with file name: {}", file.getOriginalFilename());            
                HWPFDocument doc = new HWPFDocument(file.getInputStream());
                text = new WordExtractor(doc).getText();
                doc.close();
            }
        } catch (IOException e) {
            logger().error("Failed to open word file. Name of the file: {}", file.getOriginalFilename(), e);
            throw new FailedToOpenFileException();
        }
        return text;
    }
}
