package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderAsyncClientBuilder;


@Configuration
@Profile("!dev")
public class AwsConfig {

	@Bean
	public AWSCognitoIdentityProvider awsCognitoIdentityProvider(@Value("${cloud.aws.region.static}") String region,
			AWSCredentialsProvider awsCredentialsProvider) {
		return AWSCognitoIdentityProviderAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).withRegion(region)
				.build();
	}

}
