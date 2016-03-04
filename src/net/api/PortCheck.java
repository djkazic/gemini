package net.api;

import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import atrium.Core;

public class PortCheck extends ServerResource {
	
	@Get
	public String process() {
		this.getResponse().setAccessControlAllowOrigin("*");
		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("value", Core.config.cacheEnabled);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseJSON.toString();
	}
}
