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
								String tableHTML = "<h4>SEARCH RESULTS</h4><div class=\"panel panel-default search-panel\">"
												+ "<table class=\"table table-hover\" style=color:#333>"
													+ "<thead><tr><th>#<th><th>TRACK<th>ARTIST<th>TIME<th></thead>"
													+ "</table>"
												+ "</div>"
												+ "<tbody>";

												for(int i=0; i < searchResults.size(); i++) {
													tableHTML += 
															"<tr>"
														   		+ "<td class=\"td-minus\">" + (i + 1) + "</td>"
														   		+ "<td class=\"td-plus\">"
														   			+ "<a href=\"#\">"
														   				+ "<i class=\"fa fa-play-circle-o\"></i>"
														   			+ "</a>"
														   		+ "</td>"
														   		+ "<td>"
														   			+ "Test Title"
														   		+ "</td>"
														   		+ "<td>"
														   			+ "Test Artist"
														   		+ "</td>"
														   		+ "<td class=\"td-dubplus\">"
														   			+ "1:23"
														   		+ "</td>"
														   		+ "<td class=\"td-plus\">"
														   			+ "<a href=\"#\""
														   				+ "<i class=\"fa fa-check-circle-o\"></i>"
														   			+ "</a"
														   		+ "</td>"
														   + "</tr>";
												}
								
								     tableHTML += "</tbody>"
												+ "</table>";
								responseJSON.put("value", tableHTML);
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