package com.axway.apim.user;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axway.apim.users.UserCLIApp;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

public class UserExportTestAction extends AbstractTestAction {
	
	private static Logger LOG = LoggerFactory.getLogger(UserExportTestAction.class);

	@Override
	public void doExecute(TestContext context) {
		
		boolean useEnvironmentOnly	= false;
		boolean ignoreAdminAccount	= false;
		String stage				= null;
		
		try {
			stage 				= context.getVariable("stage");
		} catch (CitrusRuntimeException ignore) {};
		
		int expectedReturnCode = 0;
		try {
			expectedReturnCode 	= Integer.parseInt(context.getVariable("expectedReturnCode"));
		} catch (Exception ignore) {};
		
		try {
			useEnvironmentOnly 	= Boolean.parseBoolean(context.getVariable("useEnvironmentOnly"));
		} catch (Exception ignore) {};
		
		try {
			ignoreAdminAccount = Boolean.parseBoolean(context.getVariable("ignoreAdminAccount"));
		} catch (Exception ignore) {};
		
		if(stage==null) {
			stage = "NOT_SET";
		}

		List<String> args = new ArrayList<String>();
		if(useEnvironmentOnly) {
			args.add("-n");
			args.add(context.replaceDynamicContentInString("${orgName}"));
			args.add("-s");
			args.add(stage);
			args.add("-o");
			args.add("json");
		} else {
			args.add("-loginName");
			args.add(context.replaceDynamicContentInString("${loginName}"));
			args.add("-t");
			args.add(context.replaceDynamicContentInString("${targetFolder}"));
			args.add("-h");
			args.add(context.replaceDynamicContentInString("${apiManagerHost}"));
			args.add("-p");
			args.add(context.replaceDynamicContentInString("${apiManagerPass}"));
			args.add("-u");
			args.add(context.replaceDynamicContentInString("${apiManagerUser}"));
			args.add("-s");
			args.add(stage);
			args.add("-o");
			args.add("json");
			if(ignoreAdminAccount) {
				args.add("-ignoreAdminAccount");
			}
		}
		int rc = UserCLIApp.export(args.toArray(new String[args.size()]));
		if(expectedReturnCode!=rc) {
			throw new ValidationException("Expected RC was: " + expectedReturnCode + " but got: " + rc);
		}
	}

}
