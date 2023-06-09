package com.mmo.microservice.account.event.listener;

import com.google.gson.Gson;
import com.mmo.microservice.account.event.TokenEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.SendResult;
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
        kafkaTemplate.send("createTokenTopic", gson.toJson(new TokenEvent(event.getType(), event.getToken(), new Date(), null, event.getR_user_fb_id())));
    }
}
