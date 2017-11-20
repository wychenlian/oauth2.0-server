package com.hybris.caas.oauth.server.web;


import com.hybris.caas.oauth.server.model.AccessTokenGetRequest;
import lombok.NoArgsConstructor;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/oauth/server")
@NoArgsConstructor
@Configuration
public class AuthorizeController extends WebMvcConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizeController.class);

    /**
     * Get authorization code.
     * */
    @GetMapping("/authorize")
    public void getAuthorizationCode(@RequestParam("response_type") String responseType ,
                                        @RequestParam("client_id") String clientId,
                                        HttpServletRequest request,
                                     HttpServletResponse servletResponse) throws IOException{
        System.out.println("----------服务端/responseCode--------------------------------------------------------------");


        try {
            //构建OAuth授权请求
            OAuthAuthzRequest oauthRequest =new OAuthAuthzRequest(request);

            if(oauthRequest.getClientId()!=null&&oauthRequest.getClientId()!="")
            {
                //设置授权码
                String authorizationCode ="authorizationCode";
                //利用oauth授权请求设置responseType，目前仅支持CODE，另外还有TOKEN
//                String responseType =oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);
                //进行OAuth响应构建
                OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                        OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
                //设置授权码
                builder.setCode(authorizationCode);
                //得到到客户端重定向地址
                String redirectURI =oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
                //构建响应
                final OAuthResponse response =builder.location(redirectURI).buildQueryMessage();
                System.out.println("服务端/responseCode内，返回的回调路径："+response.getLocationUri());
                System.out.println("----------服务端/responseCode--------------------------------------------------------------");
                String responceUri =response.getLocationUri();

                //根据OAuthResponse返回ResponseEntity响应
                HttpHeaders headers =new HttpHeaders();
                try {
                    headers.setLocation(new URI(response.getLocationUri()));
                } catch (URISyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                servletResponse.sendRedirect(responceUri);
            }

        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        System.out.println("----------服务端/responseCode--------------------------------------------------------------");
    }

    /**
     * Get AccessToken by authorization code or freshToken,
     */
    @PostMapping(value = "/token")
    public ResponseEntity getAccessTokenByAuthorizationCode(HttpServletRequest request,@Validated AccessTokenGetRequest getRequest, BindingResult bindingResult) {
        System.out.println("--------服务端/responseAccessToken-----------------------------------------------------------");
        OAuthIssuer oauthIssuerImpl=null;
        OAuthResponse response=null;
        //构建OAuth请求
        try {
            OAuthTokenRequest oauthRequest =new OAuthTokenRequest(request);
            String clientSecret = oauthRequest.getClientSecret();
            if(clientSecret!=null||clientSecret!=""){
                //生成Access Token
                oauthIssuerImpl =new OAuthIssuerImpl(new MD5Generator());
                final String accessToken =oauthIssuerImpl.accessToken();



                System.out.println(accessToken);
                System.out.println("--oooo---");
                //生成OAuth响应
                response = OAuthASResponse
                        .tokenResponse(HttpServletResponse.SC_OK)
                        .setAccessToken(accessToken)
                        .buildJSONMessage();
            }


            System.out.println("--------服务端/responseAccessToken-----------------------------------------------------------");

            //根据OAuthResponse生成ResponseEntity
            return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
        } catch (OAuthSystemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("--------服务端/responseAccessToken-----------------------------------------------------------");
        return null;
    }


    /**
     * Get AccessToken by authorization code or freshToken,
     */
    @GetMapping(value = "/login")
    public ModelAndView getLoginPage(HttpServletRequest request) {
        System.out.println("--------服务端/Request For Login Page-----------------------------------------------------------");
        ModelAndView mav = new ModelAndView("resources/static/login.html");
        return mav;
    }

}
