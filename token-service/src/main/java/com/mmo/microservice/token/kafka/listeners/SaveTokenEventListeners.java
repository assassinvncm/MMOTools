package com.mmo.microservice.token.kafka.listeners;

import com.google.gson.Gson;
import com.mmo.microservice.token.dto.TokenDTO;
import com.mmo.microservice.token.kafka.event.TokenEvent;
import com.mmo.microservice.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SaveTokenEventListeners {

    private final TokenService tokenService;
    private final Gson gson;

    @KafkaListener(topics = "createTokenTopic")
    public void handleSaveToken(String msg) {
        TokenEvent saveTokenEvent = gson.fromJson(msg, TokenEvent.class);
        log.info("Start Handle save token event: "+saveTokenEvent.getType());
        tokenService.saveToken(TokenDTO.builder()
                .type(saveTokenEvent.getType())
                .token(saveTokenEvent.getToken())
                .valid_to(saveTokenEvent.getValid_to())
                .valid_from(saveTokenEvent.getValid_from())
                .r_page_id(saveTokenEvent.getR_page_id())
                .r_user_fb_id(saveTokenEvent.getR_user_fb_id())
                .created_date(saveTokenEvent.getCreated_date())
                .build());
        log.info("End Handle save token event: "+saveTokenEvent.getType());
        // send out an email notification
    }

}
