package com.axway.apim.test.setup;

import org.springframework.http.HttpStatus;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.design.TestDesignerBeforeSuiteSupport;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.consol.citrus.message.MessageType;

public class InitializationTestIT extends TestDesignerBeforeSuiteSupport {
	
	@Override
	public void beforeSuite(TestDesigner designer) {
		designer.echo("Do a quick healthcheck");
		
		designer.http().client("gatewayPlain")
			.send()
			.get("/healthcheck");
		
		designer.http().client("gatewayPlain")
			.receive()
			.response(HttpStatus.OK)
			.payload("<status>ok</status>");

		designer.createVariable("orgNumber", RandomNumberFunction.getRandomNumber(4, true));
		designer.createVariable("orgNumber2", RandomNumberFunction.getRandomNumber(4, true));
		designer.createVariable("orgName", "API Development ${orgNumber}");
		designer.createVariable("orgName2", "API Test-Org ${orgNumber2}");
		
		designer.echo("####### Setup Test Org: 'API Development ${orgNumber}' #######");
		
		designer.http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"${orgName}\", \"description\": \"Test Org ${orgNumber}\", \"enabled\": true, \"development\": true }");
		
		designer.echo("####### Validating Test-Organisation: API Development ${orgNumber} has been created #######");
		
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "API Development ${orgNumber}")
			.extractFromPayload("$.id", "orgId");
		
		designer.echo("####### Extracted organization id: ${orgId} as attribute: orgId #######");
		
		designer.echo("####### Setup Test Org: 'API Development ${orgNumber2}' with Non-Development flag #######");
		
		designer.http().client("apiManager")
			.send()
			.post("/organizations")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\": \"${orgName2}\", \"description\": \"Non-Dev Org ${orgNumber2}\", \"enabled\": true, \"development\": false }");
	
		designer.echo("####### Validating Non-Development Test-Organisation: 'API Test-Org ${orgNumber2}' has been created #######");
		
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.validate("$.name", "API Test-Org ${orgNumber2}")
			.extractFromPayload("$.id", "orgId2");
	
		designer.echo("####### Extracted Non-Dev organization id: ${orgId2} as attribute: orgId2 #######");

		designer.echo("####### Create a test application #######");
		
		designer.http().client("apiManager")
			.send()
			.post("/applications")
			.name("orgCreatedRequest")
			.header("Content-Type", "application/json")
			.payload("{\"name\":\"Test App ${orgNumber}\",\"apis\":[],\"organizationId\":\"${orgId}\"}");
	
		designer.http().client("apiManager")
			.receive()
			.response(HttpStatus.CREATED)
			.messageType(MessageType.JSON)
			.extractFromPayload("$.id", "testAppId")
			.extractFromPayload("$.name", "testAppName");
		
		designer.echo("####### Created a application: '${testAppName}' ID: '${testAppId}' #######");
		
		designer.action(new AbstractTestAction() {
            @Override public void doExecute(TestContext testContext) {
                testContext.getGlobalVariables().put("testAppId", testContext.getVariable("testAppId"));
                testContext.getGlobalVariables().put("testAppName", testContext.getVariable("testAppName"));
                testContext.getGlobalVariables().put("orgId", testContext.getVariable("orgId"));
                testContext.getGlobalVariables().put("orgId2", testContext.getVariable("orgId2"));
                testContext.getGlobalVariables().put("orgNumber", testContext.getVariable("orgNumber"));
                testContext.getGlobalVariables().put("orgNumber2", testContext.getVariable("orgNumber2"));
                testContext.getGlobalVariables().put("orgName", testContext.getVariable("orgName"));
                testContext.getGlobalVariables().put("orgName2", testContext.getVariable("orgName2"));
            }
        });
		
	}
}
