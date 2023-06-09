package com.mmo.microservice.page.service.impl;

import com.mmo.microservice.page.dto.PageRespDTO;
import com.mmo.microservice.page.kafka.event.TokenEvent;
import com.mmo.microservice.page.model.Page;
import com.mmo.microservice.page.repository.PageRepository;
import com.mmo.microservice.page.service.PageService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final ModelMapper modelMapper;
    private final WebClient.Builder webClientBuilder;
    private final WebClient webClient;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public PageRespDTO getPageByID(Long id) {
        return modelMapper.map(pageRepository.getOne(id), PageRespDTO.class);
    }

    @Override
    public void saveListPageByAccountToken(String token, Long account_id) {
        String getPAResp = webClient.get()
                .uri("https://graph.facebook.com/v16.0/me/accounts",uriBuilder
                        -> uriBuilder.queryParam("access_token", token)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        JSONObject jsPAResp = new JSONObject(getPAResp);
        if(jsPAResp.has("data")) {
            JSONArray dataArr = jsPAResp.getJSONArray("data");
            for (int i=0; i < dataArr.length(); i++) {
                JSONObject pageATInfor = dataArr.getJSONObject(i);
                String page_name = pageATInfor.getString("name");
                String page_token = pageATInfor.getString("access_token");
                String page_id = pageATInfor.getString("id");
                Page rp = pageRepository.save(Page.builder().page_name(page_name).u_fb_id(account_id).facebook_id(page_id).created_date(new Date()).build());


                applicationEventPublisher.publishEvent(new TokenEvent(this, "PAGE", token, new Date(), rp.getId(), null));
            }
        }

    }
}
