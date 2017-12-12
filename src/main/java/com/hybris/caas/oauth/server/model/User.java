package com.hybris.caas.oauth.server.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by I341534 on 11/19/2017.
 */
@Data
public class User {
    @NotBlank
    String userName;
    String telephone;
    @NotBlank
    String password;
}
