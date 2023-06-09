package com.mmo.microservice.account.event.listener;

import com.google.gson.Gson;
import com.mmo.microservice.account.event.PageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatchPageEventListeners {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final Gson gson;

    @EventListener
    public void handleSaveTokenEventListener(PageEvent event){
        kafkaTemplate.send("createPageTopic", gson.toJson(new PageEvent(event.getToken(), event.getP_id())));
    }
}
