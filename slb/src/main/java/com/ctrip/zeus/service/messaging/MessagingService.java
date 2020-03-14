package com.ctrip.zeus.service.messaging;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface MessagingService {

    void sendMessage(String topic, String msg);

    void sendUnifiedMessage(String topic, String type, Object data, Map<String, String> properties, List<String> tags);

    void sendUnifiedMessage(String topic, String type, Object data, Map<String, String> properties, List<String> tags, Map<String, String> extraData);
}