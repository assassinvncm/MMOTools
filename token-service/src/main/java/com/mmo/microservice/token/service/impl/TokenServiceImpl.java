package com.mmo.microservice.token.service.impl;

import com.mmo.microservice.token.dto.TokenDTO;
import com.mmo.microservice.token.dto.TokenReqDTO;
import com.mmo.microservice.token.dto.TokenRespDTO;
import com.mmo.microservice.token.model.Token;
import com.mmo.microservice.token.repository.TokenRepo;
import com.mmo.microservice.token.service.TokenService;
import com.mmo.microservice.token.util.URLRequestConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final TokenRepo tokenRepo;
    private final ModelMapper modelMapper;
    private final WebClient webClient;

    @Override
    public TokenRespDTO getUserAccessTokenByUID(Long u_id) {
        return modelMapper.map(tokenRepo.getFBUserToken(u_id), TokenRespDTO.class);
    }

    @Override
    public String getPageAccessTokenByPageID(TokenReqDTO inDto) {
        String page_access_token = "";
        String rs = webClient.get()
                .uri(URLRequestConstant.GET_PAGE_ACCESS_TOKEN_PAGE_ID+inDto.getFb_page_id()
                        , uriBuilder -> uriBuilder.queryParam("access_token", inDto.getToken())
                                .queryParam("fields", "name,access_token").build())
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
        JSONObject jsPAResp = new JSONObject(rs);
        if(jsPAResp.has("access_token")) {
            page_access_token = jsPAResp.getString("access_token");
        }
        return page_access_token;
    }

    @Override
    public String getExchangeToken(String token, String grantType, String clientId, String clientSecret) {
        AtomicReference<String> exchange_access_token = new AtomicReference<>("");
        webClient.get()
                .uri("https://graph.facebook.com/v16.0/oauth/access_token", uriBuilder
                        -> uriBuilder.queryParam("grant_type", grantType)
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("fb_exchange_token", token)
                        .build())
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
                .doOnNext(response -> {
                    // Phân tích cú pháp JSON và trích xuất giá trị
                    JSONObject jsPAResp = new JSONObject(response);
                    if (jsPAResp.has("access_token")) {
                        exchange_access_token.set(jsPAResp.getString("access_token"));
                    }
                })
                .block();
//        JSONObject jsPAResp = new JSONObject(rs);
//        if(jsPAResp.has("access_token")) {
//            exchange_access_token.set(jsPAResp.getString("access_token"));
//        }
//        responseMono.subscribe(value -> {
//            // Xử lý giá trị nhận được
//            System.out.println("Value: " + value);
//        });
        return exchange_access_token.get();
    }

    @Override
    public void saveToken(TokenDTO dto) {
        log.info("Start save token successfully!");
        Optional.ofNullable(tokenRepo.getTokenExisted(dto.getR_user_fb_id(), dto.getR_page_id()))
                .ifPresent(v -> {
                    throw new IllegalArgumentException("Token of this are already!");
                });
        tokenRepo.save(modelMapper.map(dto, Token.class));
        log.info("Save token successfully!");
    }
}
