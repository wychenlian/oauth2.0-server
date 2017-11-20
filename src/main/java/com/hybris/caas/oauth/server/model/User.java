package com.hybris.caas.oauth.server.model;

/**
 * Created by I341534 on 11/19/2017.
 */
public class User {
    String userName;
    String telephone;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
