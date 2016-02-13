package net.api;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Specific REST hook for evaluation input
 *
 * @author Kevin Cai
 */
public class Bridge extends ServerResource {

	public void setHeaders() {
		getResponse().setAccessControlAllowOrigin("*");
	}

	@Post("application/text")
	public String process(JsonRepresentation entity) {
		JSONObject json = null;
		JSONObject responseJSON = new JSONObject();
		String consoleOutput = "";
		try {
			json = entity.getJsonObject();
			if(json.length() > 0) {
				Object oMethodCall = json.get("rpc");
				if(oMethodCall instanceof String) {
					String methodCall = (String) oMethodCall;

					switch(methodCall) {
						case "exec_init":
							
							break;

						default:
							
							break;
					}
				}
			} else {
				responseJSON.put("error", "empty_params");
			}
		} catch (Exception e) {
			try {
				responseJSON.put("error", "no_rpc_defined");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		consoleOutput = responseJSON.toString();
		setHeaders();
		return consoleOutput;
	}

	@Get
	public String toString() {
		return "INVALID: API GET ACCESS DISALLOWED";
	}
}