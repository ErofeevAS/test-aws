package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;


@Configuration
public class LogoutSuccessHandlerConfig {

	@Bean
	@Profile("aws")
	public LogoutSuccessHandler cognitoOidcLogoutSuccessHandler(@Value("${COGNITO_CLIENT_ID}") String clientId,
			@Value("${COGNITO_LOGOUT_URL}") String userPoolLogoutUrl) {
		return new CognitoOidcLogoutSuccessHandler(userPoolLogoutUrl, clientId);
	}

	@Bean
	@Profile({ "dev"})
	public LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
		OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(
				clientRegistrationRepository);
		successHandler.setPostLogoutRedirectUri("{baseUrl}");
		return successHandler;
	}
}
