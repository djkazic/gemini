package net.api;

import java.util.ArrayList;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import atrium.NetHandler;
import atrium.Utilities;

public class Search extends ServerResource {
	
	@Post("application/text")
	public String process(JsonRepresentation entity) {
		this.getResponse().setAccessControlAllowOrigin("*");
		JSONObject responseJSON = new JSONObject();
		try {
			JSONObject json = entity.getJsonObject();
			if(json.length() > 0) {
				try {
					try {
						String query = json.getString("query");
						ArrayList<String[]> searchResults = NetHandler.doSearch(query);
						StringBuilder sb = new StringBuilder();
										sb.append("<h4>SEARCH RESULTS</h4>");
										sb.append("<div class=\"panel panel-default search-panel\">");
										sb.append("  <table class=\"table table-hover\" style=color:#333>");
										sb.append("  <thead><tr><th>#</th><th>TRACK</th><th>ARTIST</th><th>TIME</th></tr></thead>");
										
										sb.append("<tbody>");
			
										for(int i=0; i < searchResults.size(); i++) {
											sb.append("<tr>");
												sb.append("<td class=\"td-minus\">" + (i + 1) + "</td>");
												sb.append("<td class=\"td-plus\">");
													sb.append("<a href=\"#\">");
														sb.append("<i class=\"fa fa-play-circle-o\"></i>");
													sb.append("</a>");
												sb.append("</td>");
												sb.append("<td>");
													sb.append("Test Title");
												sb.append("</td>");
												sb.append("<td>");
													sb.append("Test Artist");
												sb.append("</td>");
												sb.append("<td class=\"td-dubplus\">");
													sb.append("1:23");
												sb.append("</td>");
												sb.append("<td class=\"td-plus\">");
													sb.append("<a href=\"#\">");
														sb.append("<i class=\"fa fa-check-circle-o\"></i>");
													sb.append("</a>");
												sb.append("</td>");
											sb.append("</tr>");
										}
						
										sb.append("</tbody>");
										sb.append("</table>");
										sb.append("</div>");
						responseJSON.put("value", sb.toString());
					} catch (Exception ex) {
						Utilities.log(this, "Search API error: " + ex.getMessage(), false);
						responseJSON.put("error", ex.getMessage());
					}
				} catch (Exception ex) {
					Utilities.log(this, "Outer executive error: " + ex.getMessage(), false);
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			Utilities.log(this, "Search entity error: " + ex.getMessage(), false);
			ex.printStackTrace();
		}
		return responseJSON.toString();
	}
}
