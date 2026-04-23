package com.glotrush.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushSubscriptionRequest {
    private String endpoint;
    private String publicKey;
    private String auth;
}
