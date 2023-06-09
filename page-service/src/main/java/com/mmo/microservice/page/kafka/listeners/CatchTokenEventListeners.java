package com.mmo.microservice.page.kafka.listeners;

import com.google.gson.Gson;
import com.mmo.microservice.page.kafka.event.TokenEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatchTokenEventListeners {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Gson gson;

    @EventListener
    public void handleSaveTokenEventListener(TokenEvent event){
        log.info("Start Handle save page event: "+ event.getType());
        kafkaTemplate.send("createTokenTopic", gson.toJson(new TokenEvent(event.getType(), event.getToken(), new Date(), event.getR_page_id(), null)));
        log.info("Start Handle save page event: "+ event.getType());
    }
}
