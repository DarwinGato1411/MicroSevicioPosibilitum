package com.ec.g2g.quickbook;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import com.ec.g2g.global.ValoresGlobales;
import com.intuit.oauth2.client.DiscoveryAPIClient;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.config.Environment;
import com.intuit.oauth2.config.OAuth2Config;
import com.intuit.oauth2.config.Scope;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.data.DiscoveryAPIResponse;
import com.intuit.oauth2.data.PlatformResponse;
import com.intuit.oauth2.exception.ConnectionException;
import com.intuit.oauth2.exception.InvalidRequestException;
import com.intuit.oauth2.exception.OAuthException;

@Component
public class ManejarToken {

	@Autowired
	org.springframework.core.env.Environment env;

	@Autowired
	OAuth2PlatformClientFactory factory;

	

	@Autowired
	private ValoresGlobales valoresGlobales;
	
	
	  public RedirectView connectToQuickBooks() {

	        //initialize the config (set client id, secret)
	        OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(env.getProperty("OAuth2AppClientId"), env.getProperty("OAuth2AppClientSecret"))
	                .callDiscoveryAPI(Environment.PRODUCTION)
	                .buildConfig();
	        //generate csrf token
	        String csrf = oauth2Config.generateCSRFToken();
	        String redirectUri = env.getProperty("OAuth2AppRedirectUri");

	        try {
	            //prepare scopes
	            List<Scope> scopes = new ArrayList<Scope>();
	            scopes.add(Scope.Accounting);

	            DiscoveryAPIResponse discoveryAPIResponse = new DiscoveryAPIClient().callDiscoveryAPI(Environment.PRODUCTION);

	            System.out.println("ENPOINTS DOSCPVERY: "+discoveryAPIResponse.getAuthorizationEndpoint());
	            //prepare OAuth2Platform client
	            OAuth2PlatformClient client  = new OAuth2PlatformClient(oauth2Config);

	            //retrieve access token by calling the token endpoint
	           // BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);

	            //prepare authorization url to intiate the oauth handshake
	            return new RedirectView(oauth2Config.prepareUrl(scopes, redirectUri, csrf), true, true, false);
	        } catch (InvalidRequestException | ConnectionException e) {
//	            logger.error("Exception calling connectToQuickbooks ", e);
//	            logger.error("intuit_tid: " + e.getIntuit_tid());
//	            logger.error("More info: " + e.getResponseContent());
	        }
	        return null;
	    }
	
	public String createToken() {

		// initialize the config (set client id, secret)
		OAuth2Config oauth2Config = new OAuth2Config.OAuth2ConfigBuilder(env.getProperty("OAuth2AppClientId"),
				env.getProperty("OAuth2AppClientSecret")).callDiscoveryAPI(Environment.PRODUCTION).buildConfig();
		// generate csrf token
		String csrf = "";
		try {
			csrf = oauth2Config.generateCSRFToken();
		} catch (Exception ex) {
			csrf = ex.getMessage();
		}
		return csrf;

	}
//refresh topken
	public String refreshToken(String refresh) {

		String failureMsg = "Failed";

		try {

			OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
			//String refreshToken = (String) session.getAttribute("refresh_token")
			BearerTokenResponse bearerTokenResponse = client.refreshToken(refresh);
//			session.setAttribute("access_token", bearerTokenResponse.getAccessToken());
//			session.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());
			String jsonString = new JSONObject().put("access_token", bearerTokenResponse.getAccessToken())
					.put("refresh_token", bearerTokenResponse.getRefreshToken()).toString();
			valoresGlobales.REFRESHTOKEN= bearerTokenResponse.getRefreshToken();
			return bearerTokenResponse.getAccessToken();
		} catch (Exception ex) {

			return new JSONObject().put("response", failureMsg).toString();
		}

	}

	public String revokeToken(HttpSession session) {

		String failureMsg = "Failed";

		try {

			OAuth2PlatformClient client = factory.getOAuth2PlatformClient();
			String refreshToken = (String) session.getAttribute("refresh_token");
			PlatformResponse response = client.revokeToken(refreshToken);
//			logger.info("raw result for revoke token request= " + response.getStatus());
			return new JSONObject().put("response", "Revoke successful").toString();
		} catch (Exception ex) {
//			logger.error("Exception while calling revokeToken ", ex);
			return new JSONObject().put("response", failureMsg).toString();
		}

	}
	
	
	/*colocar eb un servicio adicional*/
	
	 public String callBackFromOAuth(@RequestParam("code") String authCode, @RequestParam("state") String state, @RequestParam(value = "realmId", required = false) String realmId, HttpSession session) {   
//	        logger.info("inside oauth2redirect of sample"  );
	        try {
		        String csrfToken = (String) session.getAttribute("csrfToken");
		        if (csrfToken.equals(state)) {
		            session.setAttribute("realmId", realmId);
		            session.setAttribute("auth_code", authCode);
		
		            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
		            String redirectUri = factory.getPropertyValue("OAuth2AppRedirectUri");
//		            logger.info("inside oauth2redirect of sample -- redirectUri " + redirectUri  );
		            
		            BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);
					 
		            session.setAttribute("access_token", bearerTokenResponse.getAccessToken());
		            session.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());
		    
		            // Update your Data store here with user's AccessToken and RefreshToken along with the realmId

		            return "connected";
		        }
//		        logger.info("csrf token mismatch " );
	        } catch (OAuthException e) {
//	        	logger.error("Exception in callback handler ", e);
			} 
	        return null;
	    }

}
