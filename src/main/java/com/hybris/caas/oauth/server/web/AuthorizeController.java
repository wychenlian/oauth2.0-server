package com.hybris.caas.oauth.server.web;


import com.hybris.caas.oauth.server.model.AccessTokenGetRequest;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/oauth/server")
public class AuthorizeController extends WebMvcConfigurerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizeController.class);

    @Autowired
    private RedisTemplate redisTemplate;

    private Uri uri ;


    public AuthorizeController() throws MalformedURLException{
        this.uri = new Uri("localhost:8082/oauth/server/login");
    }

    /**
     * Get authorization code.
     * */
    @GetMapping("/authorize")
    public ResponseEntity getAuthorizationCode(@RequestParam("response_type") String responseType ,
                                        @RequestParam("client_id") String clientId,
                                        @RequestParam("redirect_uri") String redirect_uri,
                                        HttpServletRequest request) throws IOException{

        System.out.println("----------服务端/return login page url to client begin--------------------------------------------------------------");
        try{
            //构建OAuth授权请求
            OAuthAuthzRequest oauthRequest =new OAuthAuthzRequest(request);
            if(validateAuthorizeRequest(oauthRequest)){   //Check responseType & clientId
                //得到到客户端重定向地址
                String redirectURI =  uri.toString().concat("?redirectUrl="+redirect_uri);
                MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                List<String> Locations = new ArrayList<>();
                Locations.add(redirectURI);
                headers.put("Location",Locations);
                System.out.println("----------服务端/return login page url to client end--------------------------------------------------------------");

                return new ResponseEntity(headers,HttpStatus.FOUND);
            }
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ModelAndView getLoginPage(HttpServletRequest request,@RequestParam String redirectUrl) {
        System.out.println("--------服务端/Request For Login Page-----------------------------------------------------------");
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("redirectUrl",redirectUrl);
        return mv;
    }


    /**
     * Get AccessToken by authorization code or freshToken,
     */
    @PostMapping(value = "/login")
    public ModelAndView doLogin(HttpServletRequest request, HttpServletResponse servletResponse,
                                String redirectUrl,String username,String password
                       ,ModelMap modelMap) throws IOException {
        System.out.println("--------服务端/Do Login Action Begin-----------------------------------------------------------");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("redirectUrl",redirectUrl);
            mv.addObject("error","UserName and Password can not be empty.");
            return mv;
        }

        //todo  check userName and password
        System.out.println("Check user and password");

        String authorizeCode = UUID.randomUUID().toString();
        //check  if user has login already.
        if (redisTemplate.hasKey(username)){
            //if user already logined ,return a code directly
            redirectUrl = redirectUrl.concat("?code=").concat(authorizeCode);
            servletResponse.sendRedirect(redirectUrl);
            return null;
        }
        //if user  not login
        redisTemplate.opsForValue().set(username,true);
        redisTemplate.expire(username,30, TimeUnit.MINUTES);
        System.out.println("login success");
        System.out.println("--------服务端/Do Login Action End-----------------------------------------------------------");

        //save code
        redisTemplate.opsForValue().set(authorizeCode.toString(),username);
        redisTemplate.expire(authorizeCode,1,TimeUnit.MINUTES);

        redirectUrl = redirectUrl.concat("?code=").concat(authorizeCode);
        servletResponse.sendRedirect(redirectUrl);
        return null;
    }

    private boolean validateAuthorizeRequest(final OAuthAuthzRequest request){
       return (request.getClientId()!=null&&request.getClientId()!="");
    }


}
