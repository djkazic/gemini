package net.api;

import java.util.ArrayList;

import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import atrium.NetHandler;
import atrium.Utilities;
import filter.FilterUtils;
import io.FileUtils;
import io.block.BlockedFile;

public class Search extends ServerResource {

	@Post("application/text")
	public String process(JsonRepresentation entity) {
		this.getResponse().setAccessControlAllowOrigin("*");
		JSONObject responseJSON = new JSONObject();
		try {
			JSONObject json = entity.getJsonObject();
			if (json.length() > 0) {
				try {
					try {
						String query = json.getString("query");
						if (query.equals("")) {
							responseJSON.put("value", "You cannot search for a blank query.");
							return responseJSON.toString();
						} else if (query.startsWith(".") || FilterUtils.extensionOnly(query)
								|| FilterUtils.extensionOnly(query.substring(1))) {
							responseJSON.put("value", "You cannot search for a query that starts with an extension.");
							return responseJSON.toString();
						} else if (query.length() < 3) {
							responseJSON.put("value", "You cannot search for a query shorter than 3 characters.");
							return responseJSON.toString();
						} else {
							ArrayList<String[]> searchResults = NetHandler.doSearch(query);
							// TODO: field count pre-flight check
							// TODO: filter pre-flight checks
							StringBuilder sb = new StringBuilder();
							sb.append("<h4>SEARCH RESULTS</h4>");
							sb.append("<div class=\"panel panel-default search-panel\">");
							sb.append("<table class=\"table table-hover\" style=color:#333>");
							sb.append("<thead><tr><th>#</th><th>TRACK</th><th>FILENAME</th></tr></thead>");

							sb.append("<tbody>");

							for (int i = 0; i < searchResults.size(); i++) {
								String id = searchResults.get(i)[1];
								
								sb.append("<tr>");
								sb.append("<td class=\"td-minus\">" + (i + 1) + "</td>");
								sb.append("<td class=\"td-plus res-play\" id=\"" + id + "\">");
								sb.append("<a href=\"#\">");
								
								BlockedFile resBf = FileUtils.getBlockedFile(id);
								String icon = "fa fa-play-circle-o";
								if (!resBf.isComplete()) {
									icon = "fa fa-download";
								}

								sb.append("<i class=\"" + icon + "\"></i>");
								sb.append("</a>");
								sb.append("</td>");
								sb.append("<td>");
								sb.append(searchResults.get(i)[0]);
								sb.append("</td>");
								sb.append("</tr>");
							}

							sb.append("</tbody>");
							sb.append("</table>");
							sb.append("</div>");
							responseJSON.put("value", sb.toString());
						}
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
