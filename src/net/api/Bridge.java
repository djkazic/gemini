package net.api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import atrium.Core;
import atrium.NetHandler;
import atrium.Utilities;

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
						case "port_check":
							responseJSON.put("value", Core.config.cacheEnabled);
							break;

						case "peer_count":
							responseJSON.put("value", Core.peers.size());
							break;
							
						case "search":
							try {
								String query = json.getString("query");
								ArrayList<String[]> searchResults = NetHandler.doSearch(query);
								JSONArray results = new JSONArray();
								for(int i=0; i < searchResults.size(); i++) {
									results.put(0, searchResults.get(i));
								}
								responseJSON.put("value", results);
							} catch (Exception ex) {
								Utilities.log(this, "rpc_search error: " + ex.getMessage(), false);
								responseJSON.put("error", ex.getMessage());
							}
							break;

						default:
							Utilities.log(this, "Unknown RPC called: " + methodCall, false);
							responseJSON.put("error", "unknown_rpc");
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