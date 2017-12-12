package com.hybris.caas.oauth.server.dto;

import lombok.Data;

/**
 * Created by I341534 on 12/6/2017.
 */
@Data
public class UserLoginDto {
    private String userName;
    private String password;
    private String redirectUrl;
}
