package net.api;

import org.json.JSONObject;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import atrium.Core;

public class PeerCount extends ServerResource {

	@Get
	public String process() {
		this.getResponse().setAccessControlAllowOrigin("*");
		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("value", Core.peers.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return responseJSON.toString();
	}
}
