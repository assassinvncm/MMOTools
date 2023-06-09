package com.mmo.microservice.account.event;

import lombok.*;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

@Getter
@Setter
public class TokenEvent extends ApplicationEvent{// extends ApplicationEvent
    private String type;
    private String token;
    private Date valid_from;
    private Date valid_to;
    private Date created_date;
    private Long r_page_id;
    private Long r_user_fb_id;

    public TokenEvent(Object source, String type, String token, Date created_date, Long r_page_id, Long r_user_fb_id) {
        super(source);
        this.type=type;
        this.token=token;
        this.created_date = created_date;
        this.r_page_id = r_page_id;
        this.r_user_fb_id = r_user_fb_id;
    }

    public TokenEvent(String type, String token, Date created_date, Long r_page_id, Long r_user_fb_id) {
        super(type);
        this.type = type;
        this.token = token;
        this.created_date = created_date;
        this.r_page_id = r_page_id;
        this.r_user_fb_id = r_user_fb_id;
    }
}
