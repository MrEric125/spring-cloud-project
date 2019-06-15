package com.louis.oauth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;

/**
 * @author Eric
 * @date create in 2019/6/15
 */
@Slf4j
@Configuration
@EnableAuthorizationServer
public class SysAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {


//    @Autowired
//    RestClientDetailsServiceImpl restClientDetailsService;



    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        log.info(">>>>>>>>>  security auto config");
//        clients.withClientDetails(restClientDetailsService);
    }
}
