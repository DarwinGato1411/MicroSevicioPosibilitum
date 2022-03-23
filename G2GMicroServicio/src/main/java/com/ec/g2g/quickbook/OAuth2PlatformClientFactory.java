package com.ec.g2g.quickbook;

import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 
 * @author dderose
 *
 */
@Service
@PropertySource(value="classpath:/application.properties", ignoreResourceNotFound=true)
public class OAuth2PlatformClientFactory {
	
	@Autowired
	org.springframework.core.env.Environment env;

	OAuth2PlatformClient client;
	OAuth2Config oauth2Config;
	
	@PostConstruct
	public void init() {
		//initialize the config
		oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(env.getProperty("OAuth2AppClientId"), env.getProperty("OAuth2AppClientSecret")) //set client id, secret
				.callDiscoveryAPI(Environment.PRODUCTION) // call discovery API to populate urls
				.buildConfig();
		//build the client
		client  = new OAuth2PlatformClient(oauth2Config);


		//initialize the config (set client id, secret)
		OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(env.getProperty("OAuth2AppClientId"), env.getProperty("OAuth2AppClientSecret"))
				.callDiscoveryAPI(Environment.PRODUCTION)
				.buildConfig();
		//generate csrf token
		String csrf = oauth2Config.generateCSRFToken();
		String redirectUri = env.getProperty("OAuth2AppRedirectUri");
	}
	
	
	public OAuth2PlatformClient getOAuth2PlatformClient()  {
		return client;
	}
	
	public OAuth2Config getOAuth2Config()  {
		return oauth2Config;
	}
	
	public String getPropertyValue(String proppertyName) {
		return env.getProperty(proppertyName);
	}

}
