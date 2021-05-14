package com.baiyanliu.mangareader.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Service
public class MessageFactory {
    protected final SimpMessagingTemplate webSocket;

    private final ErrorMessageRepository errorMessageRepository;

    public void createErrorMessage(String error) {
        ErrorMessage message = new ErrorMessage(error);
        errorMessageRepository.save(message);
        message.send(webSocket);
    }
}
