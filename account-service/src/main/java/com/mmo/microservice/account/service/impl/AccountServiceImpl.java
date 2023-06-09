package com.mmo.microservice.account.service.impl;

import com.mmo.microservice.account.config.ApplicationProperties;
import com.mmo.microservice.account.dto.AccountRequest;
import com.mmo.microservice.account.dto.AccountResponse;
import com.mmo.microservice.account.event.PageEvent;
import com.mmo.microservice.account.event.TokenEvent;
import com.mmo.microservice.account.model.Account;
import com.mmo.microservice.account.repository.AccountRepository;
import com.mmo.microservice.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final WebClient.Builder webClientBuilder;

    private final WebClient webClient;

    private final ApplicationProperties applicationProperties;

    private final AccountRepository accountRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest in) {
        Optional.ofNullable(in.getAccess_token())
                .orElseThrow(() -> new IllegalArgumentException("Access token can not null"));
        String token = webClientBuilder.build().get()
                .uri("http://token-service/api/token/v1/exchange", uriBuilder
                        -> uriBuilder.queryParam("grantType", applicationProperties.APP_PROP_EXCHANGE_GRANT_TYPE)
                        .queryParam("clientId", applicationProperties.APP_PROP_EXCHANGE_CLIENT_ID)
                        .queryParam("clientSecret", applicationProperties.APP_PROP_EXCHANGE_CLIENT_SECRET)
                        .queryParam("token", in.getAccess_token()).build())
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    if (response.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.error(new ResourceNotFoundException("Resource not found"));
                    } else if (response.statusCode() == HttpStatus.UNAUTHORIZED) {
                        return Mono.error(new AuthenticationException("Unauthorized"));
                    } else {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new IllegalArgumentException(body)));
                    }
                })
                .bodyToMono(String.class)
                .block();
        String fb_id = getFacebookID(token);

        Optional.ofNullable(accountRepository.getFbUserByFBID(fb_id))
                .ifPresent(v -> {
                    throw new IllegalArgumentException("This facebook account are already!");
                });

        Account ufb = accountRepository.save(Account.builder()
                .username(in.getUsername())
                .created_date(new Date())
                .type(in.getType())
                .facebook_id(fb_id)
                .build());

        log.info("Asynchorus save token");
//        webClientBuilder.build().post()
//                .uri("http://token-service/api/token/v1")
//                .body(BodyInserters.fromValue(TokenDTO.builder()
//                        .type("USER")
//                        .token(token)
//                        .created_date(new Date())
//                        .r_user_fb_id(ufb.getId())
//                        .build()))
//                .retrieve()
//                .bodyToMono(Void.class);
        applicationEventPublisher.publishEvent(new TokenEvent(this, "USER", token, new Date(), null, ufb.getId()));
        log.info("Asynchorus save page");
        applicationEventPublisher.publishEvent(new PageEvent(this, token, ufb.getId()));
//        kafkaTemplate.send("createTokenTopic", SaveTokenEvent.builder()
//                .type("USER")
//                .token(token)
//                .created_date(new Date())
//                .r_user_fb_id(ufb.getId())
//                .build());
        return null;
    }

    @Override
    public String getFacebookID(String token) {
        AtomicReference<String> rs = new AtomicReference<>("");
        webClient.get()
            .uri("https://graph.facebook.com/v16.0/me", uriBuilder
                    -> uriBuilder.queryParam("fields", "id")
                    .queryParam("access_token", token)
                    .build())
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(response -> {
                JSONObject jsresp = new JSONObject(response);
                if(jsresp.has("id")) {
                    rs.set(jsresp.getString("id"));
                }
            })
            .block();

        return rs.get();
    }
}
