package com.mmo.microservice.account.event;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class PageEvent extends ApplicationEvent {
    private String token;
    private Long p_id;

    public PageEvent(Object source, String token, Long p_id) {
        super(source);
        this.token = token;
        this.p_id = p_id;
    }

    public PageEvent(String token, Long p_id) {
        super(token);
        this.token = token;
        this.p_id = p_id;
    }
}
