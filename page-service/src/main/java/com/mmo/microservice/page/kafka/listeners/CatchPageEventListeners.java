package com.mmo.microservice.page.kafka.listeners;

import com.google.gson.Gson;
import com.mmo.microservice.page.kafka.event.PageEvent;
import com.mmo.microservice.page.service.PageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CatchPageEventListeners {

    private final PageService pageService;
    private final Gson gson;
    @KafkaListener(topics = "createPageTopic")
    public void handleSaveToken(String msg) {
        PageEvent pageEvent = gson.fromJson(msg, PageEvent.class);
        log.info("Start Handle save page event: "+ pageEvent.getP_id());

        pageService.saveListPageByAccountToken(pageEvent.getToken(), pageEvent.getP_id());

        log.info("End Handle save page event: "+ pageEvent.getP_id());
        // send out an email notification
    }

}
