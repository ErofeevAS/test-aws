package dev.stratospheric.cdk;

import static dev.stratospheric.cdk.Validations.requireNonEmpty;
import static java.util.Collections.singletonList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;


public class ServiceApp {

	public static void main(final String[] args) {
		App app = new App();

		String environmentName = (String) app.getNode().tryGetContext("environmentName");
		requireNonEmpty(environmentName, "context variable 'environmentName' must not be null");

		String applicationName = (String) app.getNode().tryGetContext("applicationName");
		requireNonEmpty(applicationName, "context variable 'applicationName' must not be null");

		String accountId = (String) app.getNode().tryGetContext("accountId");
		requireNonEmpty(accountId, "context variable 'accountId' must not be null");

		String springProfile = (String) app.getNode().tryGetContext("springProfile");
		requireNonEmpty(springProfile, "context variable 'springProfile' must not be null");

//		String dockerImageUrl = (String) app.getNode().tryGetContext("dockerImageUrl");
//		requireNonEmpty(dockerImageUrl, "context variable 'dockerImageUrl' must not be null");

		String region = (String) app.getNode().tryGetContext("region");
		requireNonEmpty(region, "context variable 'region' must not be null");

		String dockerRepositoryName = (String) app.getNode().tryGetContext("dockerRepositoryName");
		Validations.requireNonEmpty(dockerRepositoryName, "context variable 'dockerRepositoryName' must not be null");

		String dockerImageTag = (String) app.getNode().tryGetContext("dockerImageTag");
		Validations.requireNonEmpty(dockerImageTag, "context variable 'dockerImageTag' must not be null");

		Environment awsEnvironment = makeEnv(accountId, region);

		ApplicationEnvironment applicationEnvironment = new ApplicationEnvironment(
				applicationName,
				environmentName
		);

		// This stack is just a container for the parameters below, because they need a Stack as a scope.
		// We're making this parameters stack unique with each deployment by adding a timestamp, because updating an existing
		// parameters stack will fail because the parameters may be used by an old service stack.
		// This means that each update will generate a new parameters stack that needs to be cleaned up manually!
		long timestamp = System.currentTimeMillis();
		Stack parametersStack = new Stack(app, "ServiceParameters-" + timestamp, StackProps.builder()
				.stackName(applicationEnvironment.prefix("Service-Parameters-" + timestamp))
				.env(awsEnvironment)
				.build());

		CognitoStack.CognitoOutputParameters cognitoOutputParameters =
				CognitoStack.getOutputParametersFromParameterStore(parametersStack, applicationEnvironment);

		Stack serviceStack = new Stack(app, "ServiceStack", StackProps.builder()
				.stackName(applicationEnvironment.prefix("Service"))
				.env(awsEnvironment)
				.build());

		Service.DockerImageSource dockerImageSource = new Service.DockerImageSource(dockerRepositoryName, dockerImageTag);
		Network.NetworkOutputParameters networkOutputParameters = Network.getOutputParametersFromParameterStore(serviceStack, applicationEnvironment.getEnvironmentName());
		Service.ServiceInputParameters serviceInputParameters =  new Service.ServiceInputParameters(dockerImageSource, Collections.emptyList(), environmentVariables(springProfile, cognitoOutputParameters))
				.withDesiredInstances(2)
				.withHealthCheckIntervalSeconds(30)
				.withTaskRolePolicyStatements(List.of(
						PolicyStatement.Builder.create()
								.effect(Effect.ALLOW)
								.resources(singletonList("*"))
								.actions(singletonList("cognito-idp:*"))
								.build()));

		Service service = new Service(
				serviceStack,
				"Service",
				awsEnvironment,
				applicationEnvironment,
				serviceInputParameters,
				networkOutputParameters);

		app.synth();
	}

	static Map<String, String> environmentVariables(String springProfile, CognitoStack.CognitoOutputParameters cognitoOutputParameters) {
		Map<String, String> vars = new HashMap<>();
		vars.put("SPRING_PROFILES_ACTIVE", springProfile);
		vars.put("COGNITO_CLIENT_ID", cognitoOutputParameters.getUserPoolClientId());
		vars.put("COGNITO_CLIENT_SECRET", cognitoOutputParameters.getUserPoolClientSecret());
		vars.put("COGNITO_USER_POOL_ID", cognitoOutputParameters.getUserPoolId());
		vars.put("COGNITO_LOGOUT_URL", cognitoOutputParameters.getLogoutUrl());
		vars.put("COGNITO_PROVIDER_URL", cognitoOutputParameters.getProviderUrl());
		return vars;
	}


	static Environment makeEnv(String account, String region) {
		return Environment.builder()
				.account(account)
				.region(region)
				.build();
	}
}