package com.axway.apim.export.test.basic;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.axway.apim.export.test.ExportTestAction;
import com.axway.apim.test.ImportTestAction;
import com.consol.citrus.annotations.CitrusResource;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.functions.core.RandomNumberFunction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Test
public class ImageAPIExportTestIt extends TestNGCitrusTestRunner {

	private ExportTestAction swaggerExport;
	private ImportTestAction swaggerImport;
	
	@CitrusTest
	@Test @Parameters("context")
	public void run(@Optional @CitrusResource TestContext context) throws IOException {		
		ObjectMapper mapper = new ObjectMapper();

		swaggerExport = new ExportTestAction();
		swaggerImport = new ImportTestAction();
		description("Import an API to export it afterwards");

		variable("apiNumber", RandomNumberFunction.getRandomNumber(3, true));
		variable("apiPath", "/api/test/"+this.getClass().getSimpleName()+"-${apiNumber}");
		variable("apiName", this.getClass().getSimpleName()+"-${apiNumber}");
		variable("state", "unpublished");
		variable("exportLocation", "citrus:systemProperty('java.io.tmpdir')");
		variable(ExportTestAction.EXPORT_API,  "${apiPath}");
		
		// These are the folder and filenames generated by the export tool 
		variable("exportFolder", "api-test-${apiName}");
		variable("exportAPIName", "${apiName}.json");

		echo("####### Importing the API, which should exported in the second step #######");
		createVariable(ImportTestAction.API_DEFINITION,  "/test/export/files/basic/petstore.json");
		createVariable(ImportTestAction.API_CONFIG,  "/test/export/files/basic/api-with-image.json");
		createVariable("image", "/com/axway/apim/test/files/basic/API-Logo.jpg");
		createVariable("expectedReturnCode", "0");
		swaggerImport.doExecute(context);

		echo("####### Export the API from the API-Manager, we expect the image to be present and referenced #######");
		createVariable("expectedReturnCode", "0");
		swaggerExport.doExecute(context);
		
		String exportedAPIConfigFile = context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/api-config.json";
		
		echo("####### Reading exported API-Config file: '"+exportedAPIConfigFile+"' #######");
		JsonNode exportedAPIConfig = mapper.readTree(new FileInputStream(new File(exportedAPIConfigFile)));
		
		assertEquals(exportedAPIConfig.get("version").asText(), 			"1.0.0");
		assertEquals(exportedAPIConfig.get("organization").asText(),		"API Development "+context.getVariable("orgNumber"));
		//assertEquals(exportedAPIConfig.get("backendBasepath").asText(), 	"https://petstore.swagger.io");
		assertEquals(exportedAPIConfig.get("state").asText(), 				"unpublished");
		assertEquals(exportedAPIConfig.get("path").asText(), 				context.getVariable("apiPath"));
		assertEquals(exportedAPIConfig.get("name").asText(), 				context.getVariable("apiName"));
		assertEquals(exportedAPIConfig.get("caCerts").size(), 				3);
		assertEquals(exportedAPIConfig.get("image").asText(), 				"api-image.jpg");
		
		assertEquals(exportedAPIConfig.get("caCerts").get(0).get("certFile").asText(), 				"swagger.io.crt");
		assertEquals(exportedAPIConfig.get("caCerts").get(0).get("inbound").asBoolean(), 			false);
		assertEquals(exportedAPIConfig.get("caCerts").get(0).get("outbound").asBoolean(), 			true);
		
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/swagger.io.crt").exists(), "Certificate swagger.io.crt is missing");
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/GoDaddySecureCertificateAuthority-G2.crt").exists(), "Certificate GoDaddySecureCertificateAuthority-G2.crt is missing");
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/GoDaddyRootCertificateAuthority-G2.crt").exists(), "Certificate GoDaddyRootCertificateAuthority-G2.crt is missing");
		
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/"+context.getVariable("exportAPIName")).exists(), "Exported Swagger-File is missing");
		
		assertTrue(new File(context.getVariable("exportLocation")+"/"+context.getVariable("exportFolder")+"/api-image.jpg").exists(), "Exported Swagger-File is missing");
	}
}
